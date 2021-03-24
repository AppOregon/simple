package com.github.pacificengine.simple.subscription.impl;

import com.github.pacificengine.simple.event.Event;
import com.github.pacificengine.simple.event.impl.UnsubscribeEventImpl;
import com.github.pacificengine.simple.planner.EventPlannerRegistry;
import com.github.pacificengine.simple.subscription.Subscription;
import com.github.pacificengine.simple.subscription.SubscriptionCollection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

public class SubscriptionCollectionImpl implements SubscriptionCollection {
    private static final Integer DEFAULT_PRIORITY = Integer.MAX_VALUE;
    private final Map<String, InternalSubscription> subscriptionMap = new ConcurrentHashMap<>();
    private final Map<String, Set<InternalSubscription>> typeMap = new ConcurrentHashMap<>();

    private final String registryIdentifier;
    private final Map<String, Class<? extends Event>> eventTypeMap;

    public SubscriptionCollectionImpl(String registryIdentifier, Map<String, Class<? extends Event>> eventTypeMap) {
        this.registryIdentifier = registryIdentifier;
        this.eventTypeMap = Collections.unmodifiableMap(new HashMap<>(eventTypeMap));
    }

    @Override
    public boolean addSubscription(Subscription subscription) {
        if (subscription == null
                || subscription.getSubscriber() == null
                || subscription.getSubscribable() == null) {
            throw new IllegalArgumentException("Subscription must contain subscriber and subscribable");
        }

        InternalSubscription internalSubscription = new InternalSubscription(subscription);

        if (!eventTypeMap.containsKey(internalSubscription.type)) {
            throw new IllegalArgumentException("Cannot subscribe to type");
        }

        subscriptionMap.putIfAbsent(internalSubscription.identifier, internalSubscription);
        if (subscriptionMap.get(internalSubscription.identifier) != internalSubscription) {
            throw new IllegalArgumentException("Subscription already exists");
        }

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

    @Override
    public boolean sendEvent(Event event) {
        Class type = eventTypeMap.get(event.getType());
        if (type != null) {
            throw new IllegalArgumentException(String.format("Cannot send event of type, %s, as it is not a valid event type for this subscribable.", event.getType()));
        } else if (type.isInstance(event)) {
            throw new IllegalArgumentException(String.format("Cannot send event of type, %s, as %s is not castable to %s.", event.getType(), event.getClass().getSimpleName(), type.getSimpleName()));
        }

        Stream<Subscription> subscriptions = getSubscriptions(event.getType());
        if (subscriptions != null) {
            return EventPlannerRegistry.getPlanner(registryIdentifier).sendEvent(subscriptions, event);
        }
        return true;
    }

    @Override
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

    @Override
    public boolean removeAllSubscriptions() {
        subscriptionMap.forEach((key, value) -> removeSubscription(key));
        return subscriptionMap.isEmpty();
    }

    public Subscription getSubscription(String identifier) {
        return Optional.ofNullable(subscriptionMap.get(identifier)).map(subscription -> subscription.subscription).orElse(null);
    }

    public Stream<Subscription> getSubscriptions(String type) {
        return Optional.ofNullable(typeMap.get(type)).filter(subscribers -> !subscribers.isEmpty()).map(subscribers -> subscribers.stream().filter(Objects::nonNull).map(subscription -> subscription.subscription)).orElse(Stream.empty());
    }

    public Map<String, Class<? extends Event>> getSubscriptionIdentifiers() {
        return eventTypeMap;
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
