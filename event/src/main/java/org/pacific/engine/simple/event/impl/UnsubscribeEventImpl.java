package org.pacific.engine.simple.event.impl;

import org.pacific.engine.simple.event.Event;
import org.pacific.engine.simple.event.UnsubscribeEvent;

public class UnsubscribeEventImpl extends EventImpl implements UnsubscribeEvent {
    public UnsubscribeEventImpl(Integer priority, Event parentEvent) {
        super(priority, parentEvent);
    }
}
