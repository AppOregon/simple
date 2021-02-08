package org.pacific.engine.simple.planner.impl;

import lombok.Getter;
import org.pacific.engine.simple.event.Event;
import org.pacific.engine.simple.planner.EventPlanner;
import org.pacific.engine.simple.subscription.Subscription;
import org.pacific.engine.simple.identity.Identifiable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public abstract class AbstractEventPlanner implements EventPlanner {
    private final Map<String, PendingEvent> pendingEventMap = new ConcurrentHashMap<>();
    private final ConcurrentSkipListSet<PendingEvent> pendingEventSet = new ConcurrentSkipListSet<>((PendingEvent s1, PendingEvent s2) -> {
        int val1 = s1.priority;
        int val2 = s2.priority;
        if (val1 == val2) {
            return s1.hashCode() < s2.hashCode() ? -1 : 1;
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
        if (pendingEventMap.containsKey(pendingEvent.identifier)) {
            return false;
        }

        pendingEventMap.computeIfAbsent(pendingEvent.identifier, (key) -> {
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

        return pendingEventSet.remove(event);
    }

    protected PendingEvent nextEvent() {
        PendingEvent nextEvent = pendingEventSet.pollFirst();
        pendingEventMap.remove(nextEvent);

        return nextEvent;
    }

    @Getter
    protected static class PendingEvent implements Identifiable {
        private final String identifier;
        private final Integer priority;
        private final Stream<Subscription> subscriptions;
        private final Event event;
        private final BiConsumer<Event, Map<String, Object>> onComplete;

        public PendingEvent(Stream<Subscription> subscriptions, Event event, BiConsumer<Event, Map<String, Object>> onComplete) {
            this.identifier = event.getIdentifier();
            this.priority = event.getPriority();
            this.subscriptions = subscriptions;
            this.event = event;
            this.onComplete = onComplete;
        }

        public void onComplete(Event event, Map<String, Object> results) {
            onComplete.accept(event, results);
        }
    }
}
