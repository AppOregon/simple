package org.pacific.engine.simple.subscription;

import org.pacific.engine.simple.event.Event;
import org.pacific.engine.simple.event.exception.EventFailureException;

public class MockSubscriber implements Subscriber {
    @Override
    public boolean recieveEvent(Subscription subscription, Event event) throws EventFailureException {
        return false;
    }
}
