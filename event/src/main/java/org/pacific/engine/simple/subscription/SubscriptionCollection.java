package org.pacific.engine.simple.subscription;

import java.util.stream.Stream;

public interface SubscriptionCollection {
    Subscription getSubscription(String identifier);
    Stream<Subscription> getSubscriptions(String type);
}
