package com.github.pacificengine.simple.planner.impl;

import com.github.pacificengine.simple.subscription.Subscription;
import lombok.Getter;
import com.github.pacificengine.simple.event.Event;
import com.github.pacificengine.simple.identity.impl.IdentifiableImpl;
import com.github.pacificengine.simple.planner.EventPlanner;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public abstract class AbstractEventPlanner implements EventPlanner {
    private static final Integer DEFAULT_PRIORITY = Integer.MAX_VALUE;
    private final Map<String, PendingEvent> pendingEventMap = new ConcurrentHashMap<>();
    private final ConcurrentSkipListSet<PendingEvent> pendingEventSet = new ConcurrentSkipListSet<>(pendingEventComparator);

    @Override
    public boolean sendEvent(Stream<Subscription> subscriptions, Event event) {
        return sendEvent(subscriptions, event, null);
    }

    @Override
    public boolean sendEvent(Stream<Subscription> subscriptions, Event event, BiConsumer<Event, Map<String, Object>> onComplete) {
        return sendEvent(new PendingEvent(subscriptions, event, onComplete));
    }

    protected boolean sendEvent(PendingEvent event) {
        if (isShutdown()) {
            return false;
        }

        AtomicBoolean exists = new AtomicBoolean(true);
        pendingEventMap.computeIfAbsent(event.getIdentifier(), key -> {
            exists.set(false);
            pendingEventSet.add(event);
            return event;
        });

        return !exists.get();
    }

    @Override
    public boolean cancelEvent(String eventIdentifier) {
        PendingEvent event = pendingEventMap.remove(eventIdentifier);
        if (event == null) {
            return false;
        }

        return pendingEventSet.remove(event);
    }

    protected PendingEvent nextEvent() {
        PendingEvent event = pendingEventSet.pollFirst();
        if (event != null) {
            pendingEventMap.remove(event.getIdentifier());
        }

        return event;
    }

    public int getQueueSize() {
        return pendingEventMap.size();
    }

    @Getter
    protected static class PendingEvent extends IdentifiableImpl {
        private static final AtomicLong nextValue = new AtomicLong(Long.MIN_VALUE);
        private final Integer priority;
        private final long fifoValue;
        private final Stream<Subscription> subscriptions;
        private final Event event;
        private final BiConsumer<Event, Map<String, Object>> onComplete;

        public PendingEvent(Stream<Subscription> subscriptions, Event event, BiConsumer<Event, Map<String, Object>> onComplete) {
            super(event.getIdentifier());
            this.priority = Optional.ofNullable(event.getPriority()).orElse(DEFAULT_PRIORITY);
            this.fifoValue = nextValue.getAndIncrement();
            this.subscriptions = subscriptions;
            this.event = event;
            this.onComplete = onComplete;
        }

        public void onComplete(Event event, Map<String, Object> results) {
            onComplete.accept(event, results);
        }
    }

    protected static final Comparator<PendingEvent> pendingEventComparator = (PendingEvent s1, PendingEvent s2) -> {
        int val1 = s1.getPriority();
        int val2 = s2.getPriority();
        if (val1 == val2) {
            if (s1.getFifoValue() == s2.getFifoValue()) {
                return 0;
            }
            return s1.getFifoValue() < s2.getFifoValue() ? -1 : 1;
        } else {
            try {
                return Math.subtractExact(val1, val2);
            } catch (ArithmeticException e) {
                return val1 < val2 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
            }
        }
    };
}
