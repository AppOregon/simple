package org.pacific.engine.simple.identity.impl;

import org.junit.jupiter.api.Test;
import org.pacific.engine.simple.identity.Identifiable;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UUIDIdentifiableTests {
    @Test
    void verifyUniqueness() {
        assertEquals(10000, IntStream.range(0, 10000).parallel().mapToObj(i -> new UUIDIdentifiable()).map(Identifiable::getIdentifier).collect(Collectors.toSet()).size(), "Some UUID generation is found to be non-unique");
    }
}
