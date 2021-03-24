package com.github.pacificengine.simple.identity.impl;

import com.github.pacificengine.simple.identity.Identifiable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class UUIDIdentifiable implements Identifiable {
    @EqualsAndHashCode.Exclude
    private final UUID uuid = UUIDUtils.getRandomUUID();
    @ToString.Exclude
    private final String identifier = UUIDUtils.toCompressedString(uuid);
}
