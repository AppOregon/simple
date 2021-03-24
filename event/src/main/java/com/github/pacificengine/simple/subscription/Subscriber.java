package com.github.pacificengine.simple.subscription;

import com.github.pacificengine.simple.event.Event;
import com.github.pacificengine.simple.event.exception.EventFailureException;

public interface Subscriber {
    boolean recieveEvent(Subscription subscription, Event event) throws EventFailureException;
}
