package org.pacific.engine.simple.event;

public interface CancellableEvent extends Event {
    boolean cancel();
    boolean isCancelled();
}
