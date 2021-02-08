package org.pacific.engine.simple.property;

public interface SettableProperty<T> extends Property<T> {
    T setProperty(T newValue);
}
