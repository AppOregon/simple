package com.github.pacificengine.simple.planner.impl;

import com.github.pacificengine.simple.event.Event;
import com.github.pacificengine.simple.planner.FutureEventPlanner;
import com.github.pacificengine.simple.planner.TickEventPlanner;
import com.github.pacificengine.simple.subscription.Subscription;

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
    private long tickMinimumDelayMillseconds;
    private int tickMinimumDelayNanoseconds;
    private boolean allowAutoTicking;
    private AtomicLong currentTick = new AtomicLong(Long.MIN_VALUE);

    // TODO: Handle shutdown

    public DelayableConcurrentEventPlanner(String registryIdentifier, int threadPoolSize, int schedulerThreadPool, long emptyDelayinNanoseconds, long threadPoolLimitPollingNanoseconds, long threadPoolLimitReachedNotifyIntervalMilliseconds, boolean allowAutoTicking, long tickMinimumDelayNanoseconds) {
        super(registryIdentifier, threadPoolSize, emptyDelayinNanoseconds, threadPoolLimitPollingNanoseconds, threadPoolLimitReachedNotifyIntervalMilliseconds);

        if (schedulerThreadPool < 1) {
            throw new IllegalArgumentException("Scheduler thread pool size must be at least 1");
        }
        scheduler = Executors.newScheduledThreadPool(schedulerThreadPool);

        setAutoTicking(allowAutoTicking, tickMinimumDelayNanoseconds);
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
            sendEvents(tickOrdering.remove(tickValue));
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

    protected void sendEvents(Iterable<PendingEvent> events) {
        if (events != null) {
            events.forEach(event -> {
                tickEvents.remove(event.getIdentifier());
                sendEvent(event);
            });
        }
    }

    public void nextTick() {
        sendEvents(tickOrdering.remove(currentTick.incrementAndGet()));
    }

    public void setAutoTicking(boolean enabled, long tickMinimumDelayNanoseconds) {
        this.tickMinimumDelayMillseconds = tickMinimumDelayNanoseconds / 100000;
        this.tickMinimumDelayNanoseconds = (int)(tickMinimumDelayNanoseconds % 100000);
        this.allowAutoTicking = enabled;

        // TODO: handle auto ticking
    }
}
