package org.pacific.engine.simple.subscription;

import java.util.Map;

public interface Subscribable {
    boolean subscribe(Subscription subscription);
    Subscription unsubscribe(String identifier);
    Map<String, Class> getSubscriptionIdentifiers();
}
