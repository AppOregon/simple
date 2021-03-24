package com.github.pacificengine.simple.planner;

import com.github.pacificengine.simple.event.Event;
import com.github.pacificengine.simple.subscription.Subscription;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface FutureEventPlanner extends EventPlanner {
    boolean sendEvent(Stream<Subscription> subscriptions, Event event, Long delayTime, TimeUnit delayUnit);
    boolean sendEvent(Stream<Subscription> subscriptions, Event event, BiConsumer<Event, Map<String, Object>> onComplete, Long delayTime, TimeUnit delayUnit);
}
