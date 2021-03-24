package com.github.pacificengine.simple.subscription.impl;

import com.github.pacificengine.simple.subscription.Subscribable;
import com.github.pacificengine.simple.subscription.Subscription;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.github.pacificengine.simple.identity.impl.UUIDIdentifiable;
import com.github.pacificengine.simple.subscription.Subscriber;

@Getter
@RequiredArgsConstructor
public class SubscriptionImpl extends UUIDIdentifiable implements Subscription {
    private final String subscriptionType;
    private final Integer priority;
    private final Subscriber subscriber;
    private final Subscribable subscribable;
}
