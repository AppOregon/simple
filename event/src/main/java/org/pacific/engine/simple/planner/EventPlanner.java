package org.pacific.engine.simple.planner;

import org.pacific.engine.simple.event.Event;
import org.pacific.engine.simple.subscription.Subscription;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface EventPlanner {
    boolean sendEvent(Stream<Subscription> subscriptions, Event event);
    boolean sendEvent(Stream<Subscription> subscriptions, Event event, BiConsumer<Event, Map<String, Object>> onComplete);
    boolean cancelEvent(String eventIdentifier);
}
