package org.pacific.engine.simple.identity.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IdentifiableImplTests {
    @Test
    void getIdentifierTest() {
        String expected = new IdentifiableImpl("identifier").getIdentifier();
        assertEquals("identifier", expected, "Identifier is not what is expected");
    }
}
