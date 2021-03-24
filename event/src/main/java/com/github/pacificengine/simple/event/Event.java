package com.github.pacificengine.simple.event;

import com.github.pacificengine.simple.identity.Identifiable;

public interface Event extends Identifiable {
    Integer getPriority();
    String getType();
    Event getParentEvent();
}
