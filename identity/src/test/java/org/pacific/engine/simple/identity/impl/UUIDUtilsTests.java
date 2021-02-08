package org.pacific.engine.simple.identity.impl;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class UUIDUtilsTests {
    @Test
    void verifyUniqueness() {
        assertEquals(10000, IntStream.range(0, 10000).parallel().mapToObj(i -> UUIDUtils.getRandomUUID()).collect(Collectors.toSet()).size(), "Some UUID generation is found to be non-unique");
    }

    @Test
    void verifyToAndFromString() {
        IntStream.range(0, 1000).forEach(i -> {
            UUID uuid = UUIDUtils.getRandomUUID();
            assertEquals(uuid, UUIDUtils.fromUncompressedString(UUIDUtils.toUncompressedString(uuid)), "UUID is not writing to/from string correctly");
        });
        assertArrayEquals(new byte[]{125, 100, -63, 16, 5, -36, 68, 7, -91, 121, 36, 39, -102, -38, -72, -48}, UUIDUtils.toCompressedByteArray(UUID.fromString("7d64c110-05dc-4407-a579-24279adab8d0")), "Converting to byte array is getting incorrect result");
    }

    @Test
    void verifyCondense() {
        assertArrayEquals(new byte[]{125, 100, -63, 16, 5, -36, 68, 7, -91, 121, 36, 39, -102, -38, -72, -48}, UUIDUtils.toCompressedByteArray(UUID.fromString("7d64c110-05dc-4407-a579-24279adab8d0")), "Converting to byte array is getting incorrect result");
    }

    @Test
    void verifyEnlarge() {
        assertEquals(UUID.fromString("40328578-9da6-496f-a88e-ccee9b363baa"), UUIDUtils.fromCompressedByteArray(new byte[]{64, 50, -123, 120, -99, -90, 73, 111, -88, -114, -52, -18, -101, 54, 59, -86}), "Converting from byte array is getting incorrect result");
    }

    @Test
    void verifyCondenseThenEnlarge() {
        IntStream.range(0, 1000).forEach(i -> {
            UUID uuid = UUIDUtils.getRandomUUID();
            assertEquals(uuid, UUIDUtils.fromCompressedByteArray(UUIDUtils.toCompressedByteArray(uuid)), "UUID is not condensing then enlarging correctly");
        });
    }

    @Test
    void verifyCondenseStringThenEnlargeString() {
        IntStream.range(0, 1000).forEach(i -> {
            UUID uuid = UUIDUtils.getRandomUUID();
            assertEquals(uuid, UUIDUtils.fromCompressedString(UUIDUtils.toCompressedString(uuid)), "UUID is not condensing then enlarging correctly");
        });
    }
}
