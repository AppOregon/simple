package com.github.pacificengine.simple.subscription;

import com.github.pacificengine.simple.event.Event;

import java.util.Map;

public interface Subscribable {
    boolean subscribe(Subscription subscription);
    Subscription unsubscribe(String identifier);
    Map<String, Class<? extends Event>> getSubscriptionIdentifiers();
}
