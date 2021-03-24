package com.github.pacificengine.simple.identity.impl;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UUIDUtils {
    public static UUID getRandomUUID() {
        return UUID.randomUUID();
    }

    public static String toUncompressedString(UUID uuid) {
        return uuid.toString();
    }

    public static UUID fromUncompressedString(String uncompressedString) {
        return UUID.fromString(uncompressedString);
    }

    public static byte[] toCompressedByteArray(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static String toCompressedString(UUID uuid) {
        byte[] bytes = toCompressedByteArray(uuid);
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    public static UUID fromCompressedByteArray(byte[] compressedByteArray) {
        if (compressedByteArray.length != 16) {
            throw new IllegalArgumentException("Can only accept condensed uuids");
        }

        ByteBuffer bb = ByteBuffer.wrap(compressedByteArray);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }


    public static UUID fromCompressedString(String compressedString) {
        return fromCompressedByteArray(compressedString.getBytes(StandardCharsets.ISO_8859_1));
    }
}
