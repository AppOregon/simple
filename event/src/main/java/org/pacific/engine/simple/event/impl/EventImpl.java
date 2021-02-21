package org.pacific.engine.simple.event.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pacific.engine.simple.event.Event;
import org.pacific.engine.simple.identity.impl.UUIDIdentifiable;

@Getter
@RequiredArgsConstructor
public class EventImpl extends UUIDIdentifiable implements Event {
    private final String type;
    private final Integer priority;
    private final Event parentEvent;
}
