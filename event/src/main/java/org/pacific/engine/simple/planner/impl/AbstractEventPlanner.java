package org.pacific.engine.simple.planner.impl;

import lombok.Getter;
import org.pacific.engine.simple.event.Event;
import org.pacific.engine.simple.identity.impl.IdentifiableImpl;
import org.pacific.engine.simple.planner.EventPlanner;
import org.pacific.engine.simple.subscription.Subscription;
import org.pacific.engine.simple.identity.Identifiable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public abstract class AbstractEventPlanner implements EventPlanner {
    private static final Integer DEFAULT_PRIORITY = Integer.MAX_VALUE;
    private final Map<String, PendingEvent> pendingEventMap = new ConcurrentHashMap<>();
    private final ConcurrentSkipListSet<PendingEvent> pendingEventSet = new ConcurrentSkipListSet<>((PendingEvent s1, PendingEvent s2) -> {
        int val1 = s1.priority;
        int val2 = s2.priority;
        if (val1 == val2) {
            if (s1.fifoValue == s2.fifoValue) {
                return 0;
            }
            return s1.fifoValue < s2.fifoValue ? -1 : 1;
        } else {
            try {
                return Math.subtractExact(val1, val2);
            } catch (ArithmeticException e) {
                return val1 < val2 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
            }
        }
    });

    public boolean sendEvent(Stream<Subscription> subscriptions, Event event) {
        return sendEvent(subscriptions, event, null);
    }

    public boolean sendEvent(Stream<Subscription> subscriptions, Event event, BiConsumer<Event, Map<String, Object>> onComplete) {
        PendingEvent pendingEvent = new PendingEvent(subscriptions, event, onComplete);
        if (pendingEventMap.containsKey(pendingEvent.getIdentifier())) {
            return false;
        }

        pendingEventMap.computeIfAbsent(pendingEvent.getIdentifier(), (key) -> {
            pendingEventSet.add(pendingEvent);
            return pendingEvent;
        });

        return true;
    }

    public boolean cancelEvent(String eventIdentifier) {
        PendingEvent event = pendingEventMap.remove(eventIdentifier);
        if (event == null) {
            return false;
        }

        boolean value = pendingEventSet.remove(event);
        return value;
    }

    protected PendingEvent nextEvent() {
        PendingEvent event = pendingEventSet.pollFirst();
        if (event != null) {
            pendingEventMap.remove(event.getIdentifier());
        }

        return event;
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
}
