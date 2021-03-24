package com.github.pacificengine.simple.event;

public interface UpdateValueEvent<T> extends Event {
    T getValue();
    T getPreviousValue();
    boolean setValue(T newValue);
}
