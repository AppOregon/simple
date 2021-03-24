package com.github.pacificengine.simple.planner.impl;

import com.github.pacificengine.simple.event.Event;
import com.github.pacificengine.simple.planner.FutureEventPlanner;
import com.github.pacificengine.simple.planner.TickEventPlanner;
import com.github.pacificengine.simple.subscription.Subscription;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class DelayableConcurrentEventPlanner extends ConcurrentEventPlanner implements FutureEventPlanner, TickEventPlanner {
    public static final String AUTO_TICK = "NextTickOccurred";

    private long tickMinimumDelayMillseconds;
    private int tickMinimumDelayNanoseconds;
    private boolean allowAutoTicking;
    private long currentTick = Long.MIN_VALUE;

    // TODO
    public DelayableConcurrentEventPlanner(String registryIdentifier, int threadPoolSize, long emptyDelayinNanoseconds, long threadPoolLimitPollingNanoseconds, long threadPoolLimitReachedNotifyIntervalMilliseconds, boolean allowAutoTicking, long tickMinimumDelayNanoseconds) {
        super(registryIdentifier, threadPoolSize, emptyDelayinNanoseconds, threadPoolLimitPollingNanoseconds, threadPoolLimitReachedNotifyIntervalMilliseconds);
        setAutoTicking(allowAutoTicking, tickMinimumDelayNanoseconds);
    }

    @Override
    public boolean sendEvent(Stream<Subscription> subscriptions, Event event, Long delayTime, TimeUnit delayUnit) {
        return false;
    }

    @Override
    public boolean sendEvent(Stream<Subscription> subscriptions, Event event, BiConsumer<Event, Map<String, Object>> onComplete, Long delayTime, TimeUnit delayUnit) {
        return false;
    }

    @Override
    public boolean sendEvent(Stream<Subscription> subscriptions, Event event, Long tickDelay) {
        return false;
    }

    @Override
    public boolean sendEvent(Stream<Subscription> subscriptions, Event event, BiConsumer<Event, Map<String, Object>> onComplete, Long tickDelay) {
        return false;
    }

    public void nextTick() {

    }

    public void setAutoTicking(boolean enabled, long tickMinimumDelayNanoseconds) {
        this.tickMinimumDelayMillseconds = tickMinimumDelayNanoseconds / 100000;
        this.tickMinimumDelayNanoseconds = (int)(tickMinimumDelayNanoseconds % 100000);
        this.allowAutoTicking = enabled;
    }
}
