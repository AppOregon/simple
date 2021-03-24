package com.github.pacificengine.simple.subscription.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.github.pacificengine.simple.event.CancellableEvent;
import com.github.pacificengine.simple.event.Event;
import com.github.pacificengine.simple.planner.EventPlanner;
import com.github.pacificengine.simple.planner.EventPlannerRegistry;
import com.github.pacificengine.simple.subscription.Subscribable;
import com.github.pacificengine.simple.subscription.Subscriber;
import com.github.pacificengine.simple.subscription.Subscription;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SubscriptionCollectionImplTests {
    static final String PLANNER = "TestPlanner";

    SubscriptionCollectionImpl collection;

    @BeforeEach
    void setup() {
        EventPlannerRegistry.register(PLANNER, Mockito.mock(EventPlanner.class));

        collection = new SubscriptionCollectionImpl(PLANNER, Stream.of(
                new AbstractMap.SimpleImmutableEntry<>("Valid1", Event.class),
                new AbstractMap.SimpleImmutableEntry<>("Valid2", CancellableEvent.class))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @AfterEach
    void cleanup() {
        EventPlannerRegistry.remove(PLANNER);
    }

    @Test
    void addSubscription_null() {
        assertThrows(IllegalArgumentException.class, () -> collection.addSubscription(null));
    }

    @Test
    void addSubscription_noSubscriber() {
        assertThrows(IllegalArgumentException.class, () -> collection.addSubscription(new SubscriptionImpl("Valid1", 0, null, Mockito.mock(Subscribable.class))));
    }

    @Test
    void addSubscription_noSubscribable() {
        assertThrows(IllegalArgumentException.class, () -> collection.addSubscription(new SubscriptionImpl("Valid1", 0, Mockito.mock(Subscriber.class), null)));
    }

    @Test
    void addSubscription_duplicate() {
        Subscription sub = getSubscription("Valid1", 0);
        assertThrows(IllegalArgumentException.class, () -> { collection.addSubscription(sub); collection.addSubscription(sub); });
    }

    @Test
    void addSubscription_badType() {
        assertThrows(IllegalArgumentException.class, () -> collection.addSubscription(getSubscription("Invalid1", 0)));
    }

    @Test
    void addSubscription_single() {
        Subscription sub = getSubscription("Valid1", 0);
        assertEquals(true, collection.addSubscription(sub));

        assertEquals(sub, collection.getSubscription(sub.getIdentifier()));

        assertEquals(Arrays.asList(sub), collection.getSubscriptions("Valid1").collect(Collectors.toList()));
    }

    @Test
    void addSubscription_ordered() {
        Subscription sub1 = getSubscription("Valid1", 1);
        Subscription sub2 = getSubscription("Valid1", 2);
        assertEquals(true, collection.addSubscription(sub1));
        assertEquals(true, collection.addSubscription(sub2));

        assertEquals(sub1, collection.getSubscription(sub1.getIdentifier()));
        assertEquals(sub2, collection.getSubscription(sub2.getIdentifier()));

        assertEquals(Arrays.asList(sub1, sub2), collection.getSubscriptions("Valid1").collect(Collectors.toList()));
    }

    @Test
    void addSubscription_reverse() {
        Subscription sub1 = getSubscription("Valid1", 1);
        Subscription sub2 = getSubscription("Valid1", 2);
        assertEquals(true, collection.addSubscription(sub2));
        assertEquals(true, collection.addSubscription(sub1));

        assertEquals(sub1, collection.getSubscription(sub1.getIdentifier()));
        assertEquals(sub2, collection.getSubscription(sub2.getIdentifier()));

        assertEquals(Arrays.asList(sub1, sub2), collection.getSubscriptions("Valid1").collect(Collectors.toList()));
    }

    @Test
    void addSubscription_overflow() {
        Subscription sub1 = getSubscription("Valid1", Integer.MIN_VALUE);
        Subscription sub2 = getSubscription("Valid1", Integer.MAX_VALUE);
        assertEquals(true, collection.addSubscription(sub1));
        assertEquals(true, collection.addSubscription(sub2));

        assertEquals(sub1, collection.getSubscription(sub1.getIdentifier()));
        assertEquals(sub2, collection.getSubscription(sub2.getIdentifier()));

        assertEquals(Arrays.asList(sub1, sub2), collection.getSubscriptions("Valid1").collect(Collectors.toList()));
    }

    @Test
    void addSubscription_reverseOverflow() {
        Subscription sub1 = getSubscription("Valid1", Integer.MIN_VALUE);
        Subscription sub2 = getSubscription("Valid1", Integer.MAX_VALUE);
        assertEquals(true, collection.addSubscription(sub2));
        assertEquals(true, collection.addSubscription(sub1));

        assertEquals(sub1, collection.getSubscription(sub1.getIdentifier()));
        assertEquals(sub2, collection.getSubscription(sub2.getIdentifier()));

        assertEquals(Arrays.asList(sub1, sub2), collection.getSubscriptions("Valid1").collect(Collectors.toList()));
    }

    // TODO: sendEvent
    // TODO: removeSubscription
    // TODO: removeAllSubscriptions
    // TODO: getSubscriptionIdentifiers

    Subscription getSubscription(String type, int priority) {
        return new SubscriptionImpl(type, priority, Mockito.mock(Subscriber.class), Mockito.mock(Subscribable.class));
    }
}
