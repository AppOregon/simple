package org.pacific.engine.simple.planner.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.pacific.engine.simple.event.Event;
import org.pacific.engine.simple.event.impl.EventImpl;
import org.pacific.engine.simple.planner.EventPlannerRegistry;
import org.pacific.engine.simple.subscription.Subscribable;
import org.pacific.engine.simple.subscription.Subscriber;
import org.pacific.engine.simple.subscription.Subscription;
import org.pacific.engine.simple.subscription.impl.SubscriptionImpl;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ConcurrentEventPlannerTests {
    ConcurrentEventPlanner planner;

    @Test
    void TestSingleThreading() throws Exception {
        planner = setup(1);

        Subscriber sub1 = mock(Subscriber.class);
        when(sub1.recieveEvent(any(Subscription.class), any(Event.class))).then((Answer<Boolean>) invocation -> {
            Thread.sleep(100);
            return false;
        });

        final long start = System.currentTimeMillis();
        AtomicInteger waitTime = new AtomicInteger(- 1);
        planner.sendEvent(Stream.of(createSubscription(sub1)), new EventImpl(0, null));
        planner.sendEvent(Stream.of(createSubscription(sub1)), new EventImpl(1, null), (event, results) -> {
            waitTime.set((int)(System.currentTimeMillis() - start));
        });

        await().atMost(1, TimeUnit.SECONDS).until(() -> waitTime.get() != -1);
        assertTrue(waitTime.get() >= 200, waitTime.get() + " >= 200");
    }


    @Test
    void TestMultiThreading() throws Exception {
        planner = setup(2);

        Subscriber sub1 = mock(Subscriber.class);
        when(sub1.recieveEvent(any(Subscription.class), any(Event.class))).then((Answer<Boolean>) invocation -> {
            Thread.sleep(100);
            return false;
        });

        final long start = System.currentTimeMillis();
        AtomicInteger waitTime = new AtomicInteger(- 1);
        planner.sendEvent(Stream.of(createSubscription(sub1)), new EventImpl(0, null));
        planner.sendEvent(Stream.of(createSubscription(sub1)), new EventImpl(1, null), (event, results) -> {
            waitTime.set((int)(System.currentTimeMillis() - start));
        });

        await().atMost(1, TimeUnit.SECONDS).until(() -> waitTime.get() != -1);
        assertTrue(waitTime.get() < 200, waitTime.get() + " < 200");
    }

    // TODO Test Subscriptions

    @AfterEach
    void shutdown() {
        Optional.ofNullable(EventPlannerRegistry.remove("ConcurrentEventPlannerTests")).map(oldPlanner -> oldPlanner.shutdown());
    }

    ConcurrentEventPlanner setup(int threadSize) {
        ConcurrentEventPlanner planner = new ConcurrentEventPlanner("ConcurrentEventPlannerTests", threadSize, 100000, 100000);
        Optional.ofNullable(EventPlannerRegistry.register("ConcurrentEventPlannerTests", planner)).map(oldPlanner -> oldPlanner.shutdown());
        return planner;
    }

    Subscription createSubscription(Subscriber sub) {
        return new SubscriptionImpl("", 0, sub, mock(Subscribable.class));
    }
}
