package org.pacific.engine.simple.identity.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IncrementiveIdentifierGenerator {
    private final Map<String, AtomicInteger> values = new ConcurrentHashMap<>();

    public Integer getIdentifier(String identifier) {
        return values
                .computeIfAbsent(identifier, id -> new AtomicInteger(0))
                .addAndGet(1);
    }
}
