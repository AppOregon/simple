package com.github.pacificengine.simple.event.exception;

import lombok.Getter;
import com.github.pacificengine.simple.event.Event;

@Getter
public class EventFailureException extends Exception {
    private final Event event;

    public EventFailureException(Event event) {
        super();
        this.event = event;
    }

    public EventFailureException(String message, Event event) {
        super(message);
        this.event = event;
    }

    public EventFailureException(String message, Event event, Throwable cause) {
        super(message, cause);
        this.event = event;
    }

    public EventFailureException(Event event, Throwable cause) {
        super(cause);
        this.event = event;
    }
}
