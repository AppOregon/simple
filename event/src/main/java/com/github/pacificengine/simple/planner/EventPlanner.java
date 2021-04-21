package com.github.pacificengine.simple.planner;

import com.github.pacificengine.simple.subscription.Subscription;
import com.github.pacificengine.simple.event.Event;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface EventPlanner {
    boolean sendEvent(Stream<Subscription> subscriptions, Event event);
    boolean sendEvent(Stream<Subscription> subscriptions, Event event, BiConsumer<Event, Map<String, Object>> onComplete);
    boolean cancelEvent(String eventIdentifier);
    boolean isShutdown();
    boolean shutdown(boolean force);
    int size();
}
