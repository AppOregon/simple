package com.github.pacificengine.simple.property;

import com.github.pacificengine.simple.identity.Identifiable;

public interface Property<T> extends Identifiable {
    T getProperty();
}
