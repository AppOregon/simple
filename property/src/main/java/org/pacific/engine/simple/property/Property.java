package org.pacific.engine.simple.property;

import org.pacific.engine.simple.identity.Identifiable;

public interface Property<T> extends Identifiable {
    T getProperty();
}
