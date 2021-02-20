package org.pacific.engine.simple.subscription;

import org.pacific.engine.simple.event.Event;

import java.util.Map;
import java.util.stream.Stream;

public interface SubscriptionCollection {
    boolean addSubscription(Subscription subscription);
    boolean sendEvent(String type, Event event);
    Subscription getSubscription(String identifier);
    Stream<Subscription> getSubscriptions(String type);
    Subscription removeSubscription(String identifier);
    boolean removeAllSubscriptions();
    Map<String, Class> getSubscriptionIdentifiers();
}
