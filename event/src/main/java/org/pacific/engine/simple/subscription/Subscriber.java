package org.pacific.engine.simple.subscription;

import org.pacific.engine.simple.event.Event;
import org.pacific.engine.simple.event.exception.EventFailureException;

public interface Subscriber {
    boolean recieveEvent(Subscription subscription, Event event) throws EventFailureException;
}
