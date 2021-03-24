package com.github.pacificengine.simple.identity.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import com.github.pacificengine.simple.identity.Identifiable;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Getter
@EqualsAndHashCode
@ToString
public class CompositeIdentifiable implements Identifiable {
    private final String identifier;

    public CompositeIdentifiable(Object... identifier) {
        this.identifier = getCompositeIdentifier(identifier);
    }

    public CompositeIdentifiable(Iterator<?> identifier) {
        this.identifier = getCompositeIdentifier(identifier);
    }

    public CompositeIdentifiable(Stream<?> identifier) {
        this.identifier = getCompositeIdentifier(identifier);
    }

    protected long getMaxCompositeSize() {
        return Long.MAX_VALUE;
    }

    protected Collector<CharSequence, ?, String> getJoiner() {
        return Collectors.joining("','", "{'", "'}");
    }

    protected String getCompositeIdentifier(Object identifier) {
        return getFlattenedStream(identifier).limit(getMaxCompositeSize()).collect(getJoiner());
    }

    protected Stream<String> getFlattenedStream(Object o) {
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
            return ((Stream<?>)o).flatMap(this::getFlattenedStream);
        } else if (o instanceof Iterator) {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator<?>)o, Spliterator.ORDERED), false).flatMap(this::getFlattenedStream);
        } else if (o instanceof Iterable) {
            return StreamSupport.stream(((Iterable<?>)o).spliterator(), false).flatMap(this::getFlattenedStream);
        } else if (o.getClass().isArray()) {
            return Arrays.stream((Object[])o).flatMap(this::getFlattenedStream);
        } else {
            throw new UnsupportedOperationException(String.format("Cannot extract identifier from type: %s", o.getClass().getName()));
        }
    }
}
