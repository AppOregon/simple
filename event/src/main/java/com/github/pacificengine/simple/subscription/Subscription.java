package com.github.pacificengine.simple.subscription;

import com.github.pacificengine.simple.identity.Identifiable;

public interface Subscription extends Identifiable {
    String getSubscriptionType();
    Integer getPriority();
    Subscriber getSubscriber();
    Subscribable getSubscribable();
}
