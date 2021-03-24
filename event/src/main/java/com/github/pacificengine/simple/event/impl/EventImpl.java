package com.github.pacificengine.simple.event.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.github.pacificengine.simple.event.Event;
import com.github.pacificengine.simple.identity.impl.UUIDIdentifiable;

@Getter
@RequiredArgsConstructor
public class EventImpl extends UUIDIdentifiable implements Event {
    private final String type;
    private final Integer priority;
    private final Event parentEvent;
}
