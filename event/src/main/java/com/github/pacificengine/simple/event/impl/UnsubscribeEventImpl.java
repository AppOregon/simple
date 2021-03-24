package com.github.pacificengine.simple.event.impl;

import com.github.pacificengine.simple.event.Event;
import com.github.pacificengine.simple.event.UnsubscribeEvent;

public class UnsubscribeEventImpl extends DatedEventImpl implements UnsubscribeEvent {
    public static final String UNSUBSCRIBE_TYPE = "Unsubscribe";

    public UnsubscribeEventImpl(Integer priority, Event parentEvent) {
        super(UNSUBSCRIBE_TYPE, priority, parentEvent);
    }
}
