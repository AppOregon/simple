package com.github.pacificengine.simple.property;

public interface SettableProperty<T> extends Property<T> {
    T setProperty(T newValue);
}
