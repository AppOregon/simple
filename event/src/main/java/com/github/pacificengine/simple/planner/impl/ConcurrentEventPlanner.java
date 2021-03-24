package com.github.pacificengine.simple.planner.impl;

import com.github.pacificengine.simple.subscription.Subscribable;
import com.github.pacificengine.simple.subscription.Subscription;
import com.github.pacificengine.simple.subscription.impl.SubscriptionCollectionImpl;
import lombok.Getter;
import com.github.pacificengine.simple.event.Event;
import com.github.pacificengine.simple.event.exception.EventFailureException;
import com.github.pacificengine.simple.event.impl.EventImpl;
import com.github.pacificengine.simple.subscription.Subscriber;
import com.github.pacificengine.simple.subscription.SubscriptionCollection;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConcurrentEventPlanner extends AbstractEventPlanner implements Subscribable {
    public static final String EMPTY_QUEUE_TYPE = "EmptyEventQueue";
    public static final String EMPTY_PLANNER_TYPE = "EmptyEventPlanner";
    public static final String THREAD_POOL_LIMIT_HIT = "ThreadPoolLimitReached";

    protected final SubscriptionCollection subscriptions;
    private final ExecutorService threadLimitNotifier = Executors.newCachedThreadPool();
    private final ThreadPoolExecutor threadPool;
    private final long emptyDelayMilliSeconds;
    private final int emptyDelayNanoSeconds;
    private final long threadPoolLimitPollDelayMilliseconds;
    private final int threadPoolLimitPollDelayNanoseconds;
    private final long threadPoolLimitReachedNotifyIntervalMilliseconds;

    private AtomicInteger runningEvents = new AtomicInteger(0);

    private AtomicLong nextAllowedLimitNotification = new AtomicLong(System.currentTimeMillis());
    private AtomicBoolean isQuiet = new AtomicBoolean(true);
    private AtomicBoolean isEmpty = new AtomicBoolean(true);

    @Getter
    private boolean shutdown = false;

    public ConcurrentEventPlanner(String registryIdentifier, int threadPoolSize, long emptyDelayinNanoseconds, long threadPoolLimitPollingNanoseconds, long threadPoolLimitReachedNotifyIntervalMilliseconds) {
        if (threadPoolSize < 1) {
            throw new IllegalArgumentException("Thread pool size must be at least 1");
        }

        this.threadPool = new ThreadPoolExecutor(1, threadPoolSize + 1, 60, TimeUnit.SECONDS, new SynchronousQueue<>(true));
        this.subscriptions = new SubscriptionCollectionImpl(registryIdentifier, Stream.of(
                new AbstractMap.SimpleImmutableEntry<>(EMPTY_QUEUE_TYPE, Event.class),
                new AbstractMap.SimpleImmutableEntry<>(EMPTY_PLANNER_TYPE, Event.class),
                new AbstractMap.SimpleImmutableEntry<>(THREAD_POOL_LIMIT_HIT, Event.class))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        this.emptyDelayMilliSeconds = emptyDelayinNanoseconds / 100000;
        this.emptyDelayNanoSeconds = (int)(emptyDelayinNanoseconds % 100000);
        this.threadPoolLimitPollDelayMilliseconds = threadPoolLimitPollingNanoseconds / 100000;
        this.threadPoolLimitPollDelayNanoseconds = (int)(threadPoolLimitPollingNanoseconds % 100000);
        this.threadPoolLimitReachedNotifyIntervalMilliseconds = threadPoolLimitReachedNotifyIntervalMilliseconds;
        threadPool.submit(this::executeEvents);
    }

    protected void executeEvents() {
        while(!shutdown
            || getQueueSize() > 0) {
            try {
                PendingEvent event = nextEvent();
                if (event == null) {
                    if (!isEmpty.get()) {
                        submitEvent(createEvent(EMPTY_QUEUE_TYPE));
                        isEmpty.set(true);
                    }
                    if (!isQuiet.get() && runningEvents.get() == 0) {
                        submitEvent(createEvent(EMPTY_PLANNER_TYPE));
                        isQuiet.set(true);
                    }
                    if (emptyDelayMilliSeconds > 0 || emptyDelayNanoSeconds > 0) {
                        Thread.sleep(emptyDelayMilliSeconds, emptyDelayNanoSeconds);
                    }
                } else {
                    isQuiet.set(false);
                    isEmpty.set(false);
                    submitEvent(event);
                }
            } catch (Throwable t) {
                // TODO: Better logging required
                t.printStackTrace();
            }
        }
    }

    private void submitEvent(PendingEvent event) throws InterruptedException {
        try {
            threadPool.submit(() -> executeEvent(event));
        } catch (RejectedExecutionException exception) {
            if (nextAllowedLimitNotification.get() < System.currentTimeMillis()) {
                // Spin up a new temporary thread to send event, as we are out of threads and want this notification to still go out
                threadLimitNotifier.submit(() -> executeEvent(createEvent(THREAD_POOL_LIMIT_HIT)));
                nextAllowedLimitNotification.set(System.currentTimeMillis() + threadPoolLimitReachedNotifyIntervalMilliseconds);
            }
            while (true) {
                try {
                    if (threadPoolLimitPollDelayMilliseconds > 0 || threadPoolLimitPollDelayNanoseconds > 0) {
                        Thread.sleep(threadPoolLimitPollDelayMilliseconds, threadPoolLimitPollDelayNanoseconds);
                    }
                    threadPool.submit(() -> executeEvent(event));
                    break;
                } catch (RejectedExecutionException ex) {
                    continue;
                }
            }
        }
    }

    protected void executeEvent(PendingEvent pendingEvent) {
        runningEvents.incrementAndGet();
        Map<String, Object> values = new ConcurrentHashMap<>();
        Event event = pendingEvent.getEvent();
        pendingEvent.getSubscriptions().forEach((subscription) -> {
            Object result;
            try {
                result = notifySubscriber(subscription, event);
            } catch (Throwable throwable) {
                result = throwable;
            }
            values.put(subscription.getIdentifier(), result);
        });

        BiConsumer<Event, Map<String, Object>> onComplete = pendingEvent.getOnComplete();
        if (onComplete != null) {
            onComplete.accept(event, values);
        }
        runningEvents.decrementAndGet();
    }

    protected Object notifySubscriber(Subscription subscription, Event event) throws EventFailureException {
        Subscriber subscriber = subscription.getSubscriber();
        if (subscriber != null) {
            return subscriber.recieveEvent(subscription, event);
        }
        return null;
    }

    @Override
    public boolean shutdown() {
        subscriptions.removeAllSubscriptions();
        shutdown = true;
        try {
            while (getQueueSize() > 0) {
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {}
        threadPool.shutdown();
        return true;
    }

    @Override
    public boolean subscribe(Subscription subscription) {
        return subscriptions.addSubscription(subscription);
    }

    @Override
    public Subscription unsubscribe(String identifier) {
        return subscriptions.removeSubscription(identifier);
    }

    @Override
    public Map<String, Class<? extends Event>> getSubscriptionIdentifiers() {
        return subscriptions.getSubscriptionIdentifiers();
    }

    protected PendingEvent createEvent(String type) {
        return new PendingEvent(subscriptions.getSubscriptions(type), new EventImpl(type, Integer.MIN_VALUE, null), null);
    }
}
