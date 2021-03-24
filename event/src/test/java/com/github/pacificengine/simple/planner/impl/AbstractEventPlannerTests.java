package com.github.pacificengine.simple.planner.impl;

import com.github.pacificengine.simple.event.Event;
import com.github.pacificengine.simple.event.impl.EventImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractEventPlannerTests {
    AbstractEventPlanner planner;

    @BeforeEach
    void setup() {
        planner = new AbstractEventPlanner() {
            @Override
            public boolean isShutdown() {
                return false;
            }

            @Override
            public boolean shutdown() {
                return false;
            }
        };
    }

    @Test
    void noFirstEvent() {
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void singleEvent() {
        Event event = createEvent(0);
        assertTrue(planner.sendEvent(null, event));
        Assertions.assertEquals(event.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void duplicateEvent() {
        Event event = createEvent(0);
        assertTrue(planner.sendEvent(null, event));
        assertFalse(planner.sendEvent(null, event));
        Assertions.assertEquals(event.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void nullPriority() {
        Event event1 = createEvent(1);
        Event event2 = createEvent(null);
        Event event3 = createEvent(0);
        assertTrue(planner.sendEvent(null, event1));
        assertTrue(planner.sendEvent(null, event2));
        assertTrue( planner.sendEvent(null, event3));
        Assertions.assertEquals(event3.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        Assertions.assertEquals(event1.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        Assertions.assertEquals(event2.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void verifyPriority() {
        List<Event> events = IntStream.range(0, 1000).mapToObj(val -> createEvent(1000 - val)).collect(Collectors.toList());
        events.forEach(event -> assertTrue(planner.sendEvent(null, event)));
        Collections.reverse(events);
        events.forEach(event -> Assertions.assertEquals(event.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null)));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void verifyFIFO() {
        List<Event> events = IntStream.range(0, 1000).mapToObj(val -> createEvent(0)).collect(Collectors.toList());
        events.forEach(event -> assertTrue(planner.sendEvent(null, event)));
        events.forEach(event -> Assertions.assertEquals(event.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null)));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void cancelEvent() {
        Event event = createEvent(0);
        assertTrue(planner.sendEvent(null, event));
        assertTrue(planner.cancelEvent(event.getIdentifier()));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void cancelNonExistent() {
        Event event = createEvent(0);
        assertFalse(planner.cancelEvent(event.getIdentifier()));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void cancelCompleted() {
        Event event = createEvent(0);
        assertTrue(planner.sendEvent(null, event));
        Assertions.assertEquals(event.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        assertFalse(planner.cancelEvent(event.getIdentifier()));
        assertEquals(null, planner.nextEvent());
    }

    @Test
    void cancelInCollection() {
        Event event1 = createEvent(1);
        Event event2 = createEvent(null);
        Event event3 = createEvent(0);
        assertTrue(planner.sendEvent(null, event1));
        assertTrue(planner.sendEvent(null, event2));
        assertTrue(planner.sendEvent(null, event3));
        assertTrue(planner.cancelEvent(event1.getIdentifier()));
        assertFalse(planner.cancelEvent(event1.getIdentifier()));
        assertFalse(planner.cancelEvent(event1.getIdentifier()));
        assertFalse(planner.cancelEvent(event1.getIdentifier()));
        Assertions.assertEquals(event3.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        Assertions.assertEquals(event2.getIdentifier(), Optional.ofNullable(planner.nextEvent()).map(AbstractEventPlanner.PendingEvent::getIdentifier).orElse(null));
        assertEquals(null, planner.nextEvent());
    }

    Event createEvent(Integer priority) {
        return new EventImpl("TestEvent", priority, null);
    }
}
