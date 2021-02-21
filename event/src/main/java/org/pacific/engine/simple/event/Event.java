package org.pacific.engine.simple.event;

import org.pacific.engine.simple.identity.Identifiable;

public interface Event extends Identifiable {
    Integer getPriority();
    String getType();
    Event getParentEvent();
}
