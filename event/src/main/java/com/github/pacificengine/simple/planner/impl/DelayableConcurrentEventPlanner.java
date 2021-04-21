package com.github.pacificengine.simple.planner.impl;

import com.github.pacificengine.simple.event.Event;
import com.github.pacificengine.simple.planner.FutureEventPlanner;
import com.github.pacificengine.simple.planner.TickEventPlanner;
import com.github.pacificengine.simple.subscription.Subscription;
import com.github.pacificengine.simple.subscription.impl.SubscriptionImpl;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class DelayableConcurrentEventPlanner extends ConcurrentEventPlanner implements FutureEventPlanner, TickEventPlanner {
    public static final String AUTO_TICK = "NextTickOccurred";

    private final Map<String, ScheduledFuture<?>> scheduledEvents = new ConcurrentHashMap<>();
    private final Map<String, Map.Entry<PendingEvent, Long>> tickEvents = new ConcurrentHashMap<>();
    private final Map<Long, Set<PendingEvent>> tickOrdering = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private long tickMinimumDelayNanoseconds;
    private boolean allowAutoTicking;
    private boolean onlyAutoTickWhenEmpty;
    private ScheduledFuture<?> autoTickSchedule;
    private AtomicLong currentTick = new AtomicLong(Long.MIN_VALUE);

    public DelayableConcurrentEventPlanner(String registryIdentifier, int threadPoolSize, int schedulerThreadPool, long emptyDelayinNanoseconds, long threadPoolLimitPollingNanoseconds, long threadPoolLimitReachedNotifyIntervalMilliseconds, boolean allowAutoTicking, boolean onlyAutoTickWhenEmpty, long tickMinimumDelayNanoseconds) {
        super(registryIdentifier, threadPoolSize, emptyDelayinNanoseconds, threadPoolLimitPollingNanoseconds, threadPoolLimitReachedNotifyIntervalMilliseconds);

        if (schedulerThreadPool < 1) {
            throw new IllegalArgumentException("Scheduler thread pool size must be at least 1");
        }
        scheduler = Executors.newScheduledThreadPool(schedulerThreadPool);

        setAutoTicking(allowAutoTicking, onlyAutoTickWhenEmpty, tickMinimumDelayNanoseconds);
    }

    @Override
    public boolean shutdown(boolean force) {
        synchronized(this) {
            if (autoTickSchedule != null) {
                autoTickSchedule.cancel(force);
            }
            autoTickSchedule = null;
        }
        scheduledEvents.forEach((key, value) -> value.cancel(force));
        scheduledEvents.clear();
        tickEvents.clear();
        tickOrdering.clear();
        return super.shutdown(force);
    }

    @Override
    public boolean sendEvent(Stream<Subscription> subscriptions, Event event, long delayTime, TimeUnit delayUnit) {
        return sendEvent(subscriptions, event, null, delayTime, delayUnit);
    }

    @Override
    public boolean sendEvent(Stream<Subscription> subscriptions, Event event, BiConsumer<Event, Map<String, Object>> onComplete, long delayTime, TimeUnit delayUnit) {
        if (delayTime < 1) {
            return super.sendEvent(subscriptions, event, onComplete);
        }
        AtomicBoolean exists = new AtomicBoolean(true);
        scheduledEvents.computeIfAbsent(event.getIdentifier(), key -> {
            exists.set(false);
            return scheduler.schedule(() -> {
                scheduledEvents.remove(key);
                super.sendEvent(subscriptions, event, onComplete);
            }, delayTime, delayUnit);
        });

        return !exists.get();
    }

    @Override
    public boolean sendEvent(Stream<Subscription> subscriptions, Event event, long tickDelay) {
        return sendEvent(subscriptions, event, null, tickDelay);
    }

    @Override
    public boolean sendEvent(Stream<Subscription> subscriptions, Event event, BiConsumer<Event, Map<String, Object>> onComplete, long tickDelay) {
        if (tickDelay < 1) {
            return super.sendEvent(subscriptions, event, onComplete);
        }

        PendingEvent pendingEvent = new PendingEvent(subscriptions, event, onComplete);
        long tickValue = currentTick.longValue() + tickDelay;

        AtomicBoolean exists = new AtomicBoolean(true);
        tickEvents.computeIfAbsent(pendingEvent.getIdentifier(), key -> {
            exists.set(false);
            return new AbstractMap.SimpleEntry<>(pendingEvent, tickValue);
        });

        if (exists.get()) {
            return false;
        }

        Set<PendingEvent> eventList = tickOrdering.computeIfAbsent(tickValue, key -> new ConcurrentSkipListSet<>(pendingEventComparator));
        eventList.add(pendingEvent);

        // Make sure tick value didn't increase on us while adding. If it did, then we should just send the event
        if (tickValue <= currentTick.longValue()) {
            sendTickEvents(tickOrdering.remove(tickValue));
        }

        return true;
    }

    @Override
    public boolean cancelEvent(String eventIdentifier) {
        ScheduledFuture<?> future = scheduledEvents.remove(eventIdentifier);
        if (future != null) {
            if (future.getDelay(TimeUnit.NANOSECONDS) < 1) {
                return super.cancelEvent(eventIdentifier);
            }
            future.cancel(false);
            return true;
        }

        Map.Entry<PendingEvent, Long> tickEntry = tickEvents.remove(eventIdentifier);
        if (tickEntry != null) {
            AtomicBoolean couldRemove = new AtomicBoolean(false);
            tickOrdering.computeIfPresent(tickEntry.getValue(), (key, value) -> {
                couldRemove.set(value.remove(tickEntry.getKey()));
                return value;
            });
            if (!couldRemove.get()) {
                return super.cancelEvent(eventIdentifier);
            }
            return true;
        }

        return super.cancelEvent(eventIdentifier);
    }

    public int getWaitingSize() {
        return scheduledEvents.size() + tickEvents.size();
    }

    public int size() {
        return getEventSize() + getWaitingSize();
    }

    private void sendTickEvents(Iterable<PendingEvent> events) {
        if (events != null) {
            events.forEach(event -> {
                tickEvents.remove(event.getIdentifier());
                sendEvent(event);
            });
        }
    }

    public void nextTick() {
        // TODO: Probably never care about long overflowing
        sendTickEvents(tickOrdering.remove(currentTick.incrementAndGet()));
    }

    private void autoTick() {
        sendEvent(createEvent(AUTO_TICK));
        nextTick();
    }

    public void setAutoTicking(boolean enabled, boolean onlyWhenEmpty, long tickMinimumDelayNanoseconds) {
        synchronized(this) {
            this.tickMinimumDelayNanoseconds = tickMinimumDelayNanoseconds;
            this.allowAutoTicking = enabled;
            this.onlyAutoTickWhenEmpty = onlyWhenEmpty;

            if (autoTickSchedule != null) {
                autoTickSchedule.cancel(false);
                autoTickSchedule = null;
            }

            if (enabled && tickMinimumDelayNanoseconds > 0) {
                if (!onlyWhenEmpty) {
                    autoTickSchedule = scheduler.scheduleAtFixedRate(() -> autoTick(), tickMinimumDelayNanoseconds, tickMinimumDelayNanoseconds, TimeUnit.NANOSECONDS);
                } else if (tickMinimumDelayNanoseconds < 1) {
                    Subscription sub = new SubscriptionImpl(ConcurrentEventPlanner.EMPTY_PLANNER_TYPE, 0, (subscription, event) -> {
                        if (!isShutdown()) {
                            autoTick();
                        }
                        return true;
                    }, this);
                    subscribe(sub);
                } else {
                    autoTickSchedule = scheduler.schedule(() -> nextTickSchedule(tickMinimumDelayNanoseconds), tickMinimumDelayNanoseconds, TimeUnit.NANOSECONDS);
                }
            }
        }
    }

    private void nextTickSchedule(long tickMinimumDelayNanoseconds) {
        synchronized(this) {
            if (isShutdown()) {
                return;
            }
            autoTickSchedule = null;

            if (getEventSize() < 1) {
                autoTick();
                autoTickSchedule = scheduler.schedule(() -> nextTickSchedule(tickMinimumDelayNanoseconds), tickMinimumDelayNanoseconds, TimeUnit.NANOSECONDS);
            } else {
                final AtomicBoolean wasExecuted = new AtomicBoolean(false);
                Subscription sub = new SubscriptionImpl(ConcurrentEventPlanner.EMPTY_PLANNER_TYPE, 0, (subscription, event) -> {
                    if (wasExecuted.compareAndSet(false, true)) {
                        autoTick();
                        subscription.getSubscribable().unsubscribe(subscription.getIdentifier());
                        autoTickSchedule = scheduler.schedule(() -> nextTickSchedule(tickMinimumDelayNanoseconds), tickMinimumDelayNanoseconds, TimeUnit.NANOSECONDS);
                    }
                    return true;
                }, this);
                subscribe(sub);

                if (getEventSize() < 1) {
                    unsubscribe(sub.getIdentifier());

                    if (!wasExecuted.compareAndSet(false, true)) {
                        autoTick();
                    }
                }
            }
        }
    }
}
