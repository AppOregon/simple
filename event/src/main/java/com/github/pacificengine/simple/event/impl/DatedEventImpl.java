package com.github.pacificengine.simple.event.impl;

import com.github.pacificengine.simple.event.DatedEvent;
import com.github.pacificengine.simple.event.Event;

public class DatedEventImpl extends EventImpl implements DatedEvent {
    private final Long nanosecondsWhenOccurred;

    public DatedEventImpl(String type, Integer priority, Event parentEvent) {
        super(type, priority, parentEvent);
        nanosecondsWhenOccurred = System.nanoTime();
    }

    @Override
    public Long nanosecondsWhenOccurred() {
        return nanosecondsWhenOccurred;
    }
}
