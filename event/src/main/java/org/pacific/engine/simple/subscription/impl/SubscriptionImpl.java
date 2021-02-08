package org.pacific.engine.simple.subscription.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pacific.engine.simple.identity.impl.UUIDIdentifiable;
import org.pacific.engine.simple.subscription.Subscribable;
import org.pacific.engine.simple.subscription.Subscription;
import org.pacific.engine.simple.subscription.Subscriber;

@Getter
@RequiredArgsConstructor
public class SubscriptionImpl extends UUIDIdentifiable implements Subscription {
    private final String subscriptionType;
    private final Integer priority;
    private final Subscriber subscriber;
    private final Subscribable subscribable;
}
