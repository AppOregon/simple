package com.github.pacificengine.simple.event;

public interface CancellableEvent extends Event {
    boolean cancel();
    boolean isCancelled();
}
