package com.github.pacificengine.simple.subscription;

import com.github.pacificengine.simple.event.Event;

import java.util.Map;
import java.util.stream.Stream;

public interface SubscriptionCollection {
    boolean addSubscription(Subscription subscription);
    boolean sendEvent(Event event);
    Subscription getSubscription(String identifier);
    Stream<Subscription> getSubscriptions(String type);
    Subscription removeSubscription(String identifier);
    boolean removeAllSubscriptions();
    Map<String, Class<? extends Event>> getSubscriptionIdentifiers();
}
