package org.pacific.engine.simple.subscription.impl;

import lombok.RequiredArgsConstructor;
import org.pacific.engine.simple.event.impl.UnsubscribeEventImpl;
import org.pacific.engine.simple.planner.EventPlannerRegistry;
import org.pacific.engine.simple.subscription.Subscription;
import org.pacific.engine.simple.subscription.SubscriptionCollection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class SubscriptionCollectionImpl implements SubscriptionCollection {
    private static final Integer DEFAULT_PRIORITY = Integer.MAX_VALUE;
    private final Map<String, InternalSubscription> subscriptionMap = new ConcurrentHashMap<>();
    private final Map<String, Set<InternalSubscription>> typeMap = new ConcurrentHashMap<>();

    private final String registryIdentifier;

    public boolean addSubscription(Subscription subscription) {
        if (subscription == null
                || subscription.getSubscriber() == null
                || subscription.getSubscribable() == null) {
            throw new IllegalArgumentException("Subscription must contain subscriber and subscribable");
        }

        InternalSubscription internalSubscription = new InternalSubscription(subscription);
        if (subscriptionMap.containsKey(internalSubscription.identifier)) {
            return false;
        }

        subscriptionMap.put(internalSubscription.identifier, internalSubscription);

        Set<InternalSubscription> type = typeMap.computeIfAbsent(internalSubscription.type, k -> new ConcurrentSkipListSet<>(
                (InternalSubscription s1, InternalSubscription s2) -> {
                    int val1 = s1.priority;
                    int val2 = s2.priority;
                    if (val1 == val2) {
                        return s1.hashCode() < s2.hashCode() ? -1 : 1;
                    } else {
                        try {
                            return Math.subtractExact(val1, val2);
                        } catch (ArithmeticException e) {
                            return val1 < val2 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                        }
                    }
                }
        ));
        type.add(internalSubscription);

        return true;
    }

    public Subscription removeSubscription(String identifier) {
        InternalSubscription subscription;

        subscription = subscriptionMap.remove(identifier);
        if (subscription == null) {
            return null;
        }
        typeMap.get(subscription.type).remove(subscription);

        EventPlannerRegistry.getPlanner(registryIdentifier)
                .sendEvent(Stream.of(subscription.subscription), new UnsubscribeEventImpl(0, null));

        return subscription.subscription;
    }

    public boolean removeAllSubscriptions() {
        subscriptionMap.forEach((key, value) -> removeSubscription(key));
        return subscriptionMap.isEmpty();
    }

    @Override
    public Subscription getSubscription(String identifier) {
        return Optional.ofNullable(subscriptionMap.get(identifier)).map(subscription -> subscription.subscription).orElse(null);
    }

    @Override
    public Stream<Subscription> getSubscriptions(String type) {
        return typeMap.get(type).stream().filter(Objects::nonNull).map(subscription -> subscription.subscription);
    }

    private static class InternalSubscription {
        final String identifier;
        final String type;
        final int priority;
        final Subscription subscription;

        InternalSubscription(Subscription subscription) {
            this.identifier = subscription.getIdentifier();
            this.type = subscription.getSubscriptionType();
            this.priority = getValueOrDefault(subscription.getPriority());
            this.subscription = subscription;
        }

        int getValueOrDefault(Integer value) {
            return value == null ? DEFAULT_PRIORITY : value;
        }
    }
}
