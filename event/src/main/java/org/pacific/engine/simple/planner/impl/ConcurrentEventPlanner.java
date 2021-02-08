package org.pacific.engine.simple.planner.impl;

import lombok.AllArgsConstructor;
import org.pacific.engine.simple.event.Event;
import org.pacific.engine.simple.event.exception.EventFailureException;
import org.pacific.engine.simple.subscription.Subscription;
import org.pacific.engine.simple.subscription.Subscriber;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@AllArgsConstructor
public class ConcurrentEventPlanner extends AbstractEventPlanner {
    private final ThreadPoolExecutor threadPool;
    private boolean isShuttingDown = false;

    public ConcurrentEventPlanner(int threadPoolSize) {
        if (threadPoolSize < 1) {
            throw new IllegalArgumentException("Thread pool size must be at least 1");
        }
        this.threadPool = new ThreadPoolExecutor(1, threadPoolSize + 1, 60, TimeUnit.SECONDS, new SynchronousQueue<>());
        threadPool.submit(this::executeEvents);
    }

    protected void executeEvents() {
        while(!isShuttingDown) {
            PendingEvent event = nextEvent();
            if (event != null) {
                threadPool.submit(() -> executeEvent(event));
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

    protected void shutdown() {
        isShuttingDown = true;
        threadPool.shutdown();
    }
}
