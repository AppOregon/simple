package org.pacific.engine.simple.identity.impl;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.pacific.engine.simple.identity.Identifiable;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class IdentifiableImpl implements Identifiable {
    private final String identifier;
}
