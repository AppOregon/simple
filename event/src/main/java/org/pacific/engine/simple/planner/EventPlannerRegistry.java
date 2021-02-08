package org.pacific.engine.simple.planner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventPlannerRegistry {
    private final static Map<String, EventPlanner> registry = new ConcurrentHashMap<>();

    public static EventPlanner register(String registryIdentifier, EventPlanner planner) {
        return registry.put(registryIdentifier, planner);
    }

    public static EventPlanner getPlanner(String registryIdentifier) {
        return registry.get(registryIdentifier);
    }
}
