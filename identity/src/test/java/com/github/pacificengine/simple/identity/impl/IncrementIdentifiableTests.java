package com.github.pacificengine.simple.identity.impl;

import org.junit.jupiter.api.Test;
import com.github.pacificengine.simple.identity.Identifiable;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IncrementIdentifiableTests {
    @Test
    void verifyUniqueness() {
        assertEquals(10000, IntStream.range(0, 10000).parallel().mapToObj(i -> new IncrementIdentifiable("IncrementIdentifiableTests.verifyUniqueness")).map(Identifiable::getIdentifier).collect(Collectors.toSet()).size(), "Values are not unique");
    }

    @Test
    void verifyUniquenessOnNoArgConstructor() {
        assertEquals(10000, IntStream.range(0, 10000).parallel().mapToObj(i -> new IncrementIdentifiable()).map(Identifiable::getIdentifier).collect(Collectors.toSet()).size(), "Values are not unique");
    }

    @Test
    void verifyOrder() {
        assertEquals(IntStream.range(1, 10001).mapToObj(Integer::toString).collect(Collectors.toList()), IntStream.range(0, 10000).mapToObj(i -> new IncrementIdentifiable("IncrementIdentifiableTests.verifyOrder")).map(Identifiable::getIdentifier).collect(Collectors.toList()), "Values are not in ascending order");
    }

    @Test
    void verifyOtherSets() {
        assertEquals("1", new IncrementIdentifiable("IncrementIdentifiableTests.verifyOtherSetsA").getIdentifier());
        assertEquals("1", new IncrementIdentifiable("IncrementIdentifiableTests.verifyOtherSetsB").getIdentifier());
        assertEquals("1", new IncrementIdentifiable("IncrementIdentifiableTests.verifyOtherSetsC").getIdentifier());
        assertEquals("2", new IncrementIdentifiable("IncrementIdentifiableTests.verifyOtherSetsA").getIdentifier());
        assertEquals("3", new IncrementIdentifiable("IncrementIdentifiableTests.verifyOtherSetsA").getIdentifier());
        assertEquals("2", new IncrementIdentifiable("IncrementIdentifiableTests.verifyOtherSetsC").getIdentifier());
        assertEquals("4", new IncrementIdentifiable("IncrementIdentifiableTests.verifyOtherSetsA").getIdentifier());
        assertEquals("2", new IncrementIdentifiable("IncrementIdentifiableTests.verifyOtherSetsB").getIdentifier());
    }
}
