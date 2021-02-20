package org.pacific.engine.simple.planner.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pacific.engine.simple.event.Event;
import org.pacific.engine.simple.event.exception.EventFailureException;
import org.pacific.engine.simple.event.impl.EventImpl;
import org.pacific.engine.simple.subscription.Subscribable;
import org.pacific.engine.simple.subscription.Subscription;
import org.pacific.engine.simple.subscription.Subscriber;
import org.pacific.engine.simple.subscription.SubscriptionCollection;
import org.pacific.engine.simple.subscription.impl.SubscriptionCollectionImpl;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class ConcurrentEventPlanner extends AbstractEventPlanner implements Subscribable {
    private final ThreadPoolExecutor threadPool;
    private final SubscriptionCollection subscriptions;
    private final long emptyDelayMilliSeconds;
    private final int emptyDelayNanoSeconds;
    private final long threadPoolLimitPollDelayMilliseconds;
    private final int threadPoolLimitPollDelayNanoseconds;
    private boolean isEmpty = true;

    @Getter
    private boolean shutdown = false;

    public ConcurrentEventPlanner(String registryIdentifier, int threadPoolSize, long emptyDelayinNanoseconds, long threadPoolLimitPollingNanoseconds) {
        if (threadPoolSize < 1) {
            throw new IllegalArgumentException("Thread pool size must be at least 1");
        }

        this.threadPool = new ThreadPoolExecutor(1, threadPoolSize + 1, 60, TimeUnit.SECONDS, new SynchronousQueue<>(true));
        this.subscriptions = new SubscriptionCollectionImpl(registryIdentifier, Stream.of(
                new AbstractMap.SimpleImmutableEntry<>("EmptyEventQueue", EventImpl.class),
                new AbstractMap.SimpleImmutableEntry<>("ThreadPoolLimitReached", EventImpl.class))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        this.emptyDelayMilliSeconds = emptyDelayinNanoseconds / 100000;
        this.emptyDelayNanoSeconds = (int)(emptyDelayinNanoseconds % 100000);
        this.threadPoolLimitPollDelayMilliseconds = threadPoolLimitPollingNanoseconds / 100000;
        this.threadPoolLimitPollDelayNanoseconds = (int)(threadPoolLimitPollingNanoseconds % 100000);
        threadPool.submit(this::executeEvents);
    }

    protected void executeEvents() {
        while(!shutdown) {
            try {
                PendingEvent event = nextEvent();
                if (event == null) {
                    if (!isEmpty) {
                        subscriptions.sendEvent("EmptyEventQueue", new EventImpl(0, null));
                    }
                    isEmpty = true;
                    if (emptyDelayMilliSeconds > 0 || emptyDelayNanoSeconds > 0) {
                        Thread.sleep(emptyDelayMilliSeconds, emptyDelayNanoSeconds);
                    }
                } else {
                    isEmpty = false;
                    try {
                        threadPool.submit(() -> executeEvent(event));
                    } catch (RejectedExecutionException exception) {
                        subscriptions.sendEvent("ThreadPoolLimitReached", new EventImpl(0, null));
                        if (threadPoolLimitPollDelayMilliseconds > 0 || threadPoolLimitPollDelayNanoseconds > 0) {
                            while(threadPool.getMaximumPoolSize() <= threadPool.getActiveCount()) {
                                Thread.sleep(threadPoolLimitPollDelayMilliseconds, threadPoolLimitPollDelayNanoseconds);
                            }
                        } else {
                            while(threadPool.getMaximumPoolSize() <= threadPool.getActiveCount());
                        }
                        threadPool.submit(() -> executeEvent(event));
                    }
                }
            } catch (Throwable t) {
                // TODO: Better logging required
                t.printStackTrace();
            }
        }
    }

    protected void executeEvent(PendingEvent pendingEvent) {
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
    public Map<String, Class> getSubscriptionIdentifiers() {
        return subscriptions.getSubscriptionIdentifiers();
    }
}
