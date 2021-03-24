package com.github.pacificengine.simple.identity.impl;

public class IncrementIdentifiable extends IdentifiableImpl {
    private static final String uniqueSet = UUIDUtils.toCompressedString(UUIDUtils.getRandomUUID());
    private static final IncrementiveIdentifierGenerator generator = new IncrementiveIdentifierGenerator();

    public IncrementIdentifiable() {
        this(uniqueSet);
    }

    public IncrementIdentifiable(String incrementerIdentifier) {
        super(generator.getIdentifier(incrementerIdentifier).toString());
    }
}
