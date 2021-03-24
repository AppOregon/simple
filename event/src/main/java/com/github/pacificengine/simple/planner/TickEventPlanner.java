package com.github.pacificengine.simple.planner;

import com.github.pacificengine.simple.event.Event;
import com.github.pacificengine.simple.subscription.Subscription;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface TickEventPlanner extends EventPlanner {
    boolean sendEvent(Stream<Subscription> subscriptions, Event event, long tickDelay);
    boolean sendEvent(Stream<Subscription> subscriptions, Event event, BiConsumer<Event, Map<String, Object>> onComplete, long tickDelay);
}
