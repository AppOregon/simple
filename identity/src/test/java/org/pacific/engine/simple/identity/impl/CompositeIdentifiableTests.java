package org.pacific.engine.simple.identity.impl;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CompositeIdentifiableTests {
    @Test
    void testArrayConstructor() {
        UUID uuid = UUIDUtils.getRandomUUID();
        String actualId = new CompositeIdentifiable("Val", uuid, 1, null, 6.6, new IdentifiableImpl("1000")).getIdentifier();
        assertEquals("{'Val','" + uuid.toString() + "','1','null','6.6','1000'}", actualId);
    }

    @Test
    void testIteratorConstructor() {
        UUID uuid = UUIDUtils.getRandomUUID();
        String actualId = new CompositeIdentifiable(Arrays.asList("Val", uuid, 1, null, 6.6, new IdentifiableImpl("1000")).listIterator()).getIdentifier();
        assertEquals("{'Val','" + uuid.toString() + "','1','null','6.6','1000'}", actualId);
    }

    @Test
    void testStreamConstructor() {
        UUID uuid = UUIDUtils.getRandomUUID();
        String actualId = new CompositeIdentifiable(Stream.of("Val", uuid, 1, null, 6.6, new IdentifiableImpl("1000"))).getIdentifier();
        assertEquals("{'Val','" + uuid.toString() + "','1','null','6.6','1000'}", actualId);
    }
}
