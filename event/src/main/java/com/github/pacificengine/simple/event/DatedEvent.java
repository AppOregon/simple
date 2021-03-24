package com.github.pacificengine.simple.event;

public interface DatedEvent extends Event {
    Long nanosecondsWhenOccurred();
}
