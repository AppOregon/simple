package com.github.pacificengine.simple.planner.impl;

import com.github.pacificengine.simple.event.Event;
import com.github.pacificengine.simple.event.impl.EventImpl;
import com.github.pacificengine.simple.planner.EventPlannerRegistry;
import com.github.pacificengine.simple.subscription.Subscribable;
import com.github.pacificengine.simple.subscription.Subscriber;
import com.github.pacificengine.simple.subscription.Subscription;
import com.github.pacificengine.simple.subscription.impl.SubscriptionImpl;
import org.junit.jupiter.api.AfterEach;

import java.util.Optional;

import static org.mockito.Mockito.mock;

public class DelayableConcurrentEventPlannerTests {
    DelayableConcurrentEventPlanner planner;

    @AfterEach
    void shutdown() {
        Optional.ofNullable(EventPlannerRegistry.remove("DelayableConcurrentEventPlannerTests")).map(planner -> planner.shutdown(false));
    }

    ConcurrentEventPlanner setup(int threadSize) {
        shutdown();
        DelayableConcurrentEventPlanner planner = new DelayableConcurrentEventPlanner("DelayableConcurrentEventPlannerTests", threadSize, 10, 100000, 100000, 0, false, false, 10000);
        EventPlannerRegistry.register("DelayableConcurrentEventPlannerTests", planner);
        return planner;
    }

    Subscription createSubscription(Subscriber sub) {
        return new SubscriptionImpl("", 0, sub, mock(Subscribable.class));
    }


    Event createEvent(Integer priority) {
        return new EventImpl("TestEvent", priority, null);
    }
}
