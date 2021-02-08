package org.pacific.engine.simple.identity.impl;

import org.pacific.engine.simple.identity.Identifiable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CompositeIdentifiable extends IdentifiableImpl {
    public CompositeIdentifiable(Object... identifier) {
        super(getCompositeIdentifier(identifier));
    }

    public CompositeIdentifiable(Iterator<?> identifier) {
        super(getCompositeIdentifier(identifier));
    }

    public CompositeIdentifiable(Stream<?> identifier) {
        super(getCompositeIdentifier(identifier));
    }

    private static String getCompositeIdentifier(Object identifier) {
        return getFlattenedStream(identifier).collect(Collectors.joining("','", "{'", "'}"));
    }

    private static Stream<String> getFlattenedStream(Object o) {
        if (o == null) {
            return Stream.of(new String[]{null});
        } else if (o instanceof Identifiable) {
            return Stream.of(((Identifiable)o).getIdentifier());
        } else if (o instanceof String) {
            return Stream.of((String)o);
        } else if (o instanceof UUID) {
            return Stream.of(o.toString());
        } else if (o instanceof Number) {
            return Stream.of(o.toString());
        } else if (o instanceof Stream) {
            return ((Stream<?>)o).flatMap(CompositeIdentifiable::getFlattenedStream);
        } else if (o instanceof Iterator) {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator<?>)o, Spliterator.ORDERED), false).flatMap(CompositeIdentifiable::getFlattenedStream);
        } else if (o instanceof Iterable) {
            return StreamSupport.stream(((Iterable<?>)o).spliterator(), false).flatMap(CompositeIdentifiable::getFlattenedStream);
        } else if (o.getClass().isArray()) {
            return Arrays.stream((Object[])o).flatMap(CompositeIdentifiable::getFlattenedStream);
        } else {
            throw new UnsupportedOperationException(String.format("Cannot extract identifier from type: %s", o.getClass().getName()));
        }
    }
}
