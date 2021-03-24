package com.github.pacificengine.simple.identity.impl;

import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IncrementiveIdentifierGeneratorTests {
    private final IncrementiveIdentifierGenerator generator = new IncrementiveIdentifierGenerator();

    @Test
    void verifyUniqueness() {
        assertEquals(10000, IntStream.range(0, 10000).parallel().mapToObj(i -> generator.getIdentifier("IncrementiveIdentifierGeneratorTests.verifyUniqueness")).collect(Collectors.toSet()).size(), "Values are not unique");
    }

    @Test
    void verifyOrder() {
        assertEquals(IntStream.range(1, 10001).boxed().collect(Collectors.toList()), IntStream.range(0, 10000).mapToObj(i -> generator.getIdentifier("IncrementiveIdentifierGeneratorTests.verifyOrder")).collect(Collectors.toList()), "Values are not in ascending order");
    }

    @Test
    void verifyOtherSets() {
        assertEquals(1, generator.getIdentifier("IncrementiveIdentifierGeneratorTests.verifyOtherSetsA"));
        assertEquals(1, generator.getIdentifier("IncrementiveIdentifierGeneratorTests.verifyOtherSetsB"));
        assertEquals(1, generator.getIdentifier("IncrementiveIdentifierGeneratorTests.verifyOtherSetsC"));
        assertEquals(2, generator.getIdentifier("IncrementiveIdentifierGeneratorTests.verifyOtherSetsA"));
        assertEquals(3, generator.getIdentifier("IncrementiveIdentifierGeneratorTests.verifyOtherSetsA"));
        assertEquals(2, generator.getIdentifier("IncrementiveIdentifierGeneratorTests.verifyOtherSetsC"));
        assertEquals(4, generator.getIdentifier("IncrementiveIdentifierGeneratorTests.verifyOtherSetsA"));
        assertEquals(2, generator.getIdentifier("IncrementiveIdentifierGeneratorTests.verifyOtherSetsB"));
    }
}
