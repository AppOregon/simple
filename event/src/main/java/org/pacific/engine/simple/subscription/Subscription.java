package org.pacific.engine.simple.subscription;

import org.pacific.engine.simple.identity.Identifiable;

public interface Subscription extends Identifiable {
    String getSubscriptionType();
    Integer getPriority();
    Subscriber getSubscriber();
    Subscribable getSubscribable();
}
