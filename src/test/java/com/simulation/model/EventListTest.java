package com.simulation.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EventList class
 */
@DisplayName("EventList Tests")
class EventListTest {

    private EventList eventList;
    private Task task1, task2, task3;

    @BeforeEach
    void setUp() {
        eventList = new EventList();
        task1 = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        task2 = new Task(TaskType.GPU, UserType.PERSONAL_VIP, 0.0);
        task3 = new Task(TaskType.CPU, UserType.ENTERPRISE_VIP, 0.0);
    }

    @Test
    @DisplayName("New EventList should be empty")
    void testEmptyList() {
        assertFalse(eventList.hasEvents(), "New event list should be empty");
        assertNull(eventList.getNextEvent(), "Getting event from empty list should return null");
    }

    @Test
    @DisplayName("Should add and retrieve a single event")
    void testAddSingleEvent() {
        Event event = new Event(EventType.ARRIVAL, 10.0, task1);
        eventList.addEvent(event);

        assertTrue(eventList.hasEvents(), "Event list should have events");
        
        Event retrieved = eventList.getNextEvent();
        assertNotNull(retrieved, "Retrieved event should not be null");
        assertEquals(EventType.ARRIVAL, retrieved.getType());
        assertEquals(10.0, retrieved.getTime(), 0.001);
    }

    @Test
    @DisplayName("Events should be ordered by time (earliest first)")
    void testEventOrdering() {
        Event event1 = new Event(EventType.ARRIVAL, 15.0, task1);
        Event event2 = new Event(EventType.DEP_DATA_STORAGE, 5.0, task2);
        Event event3 = new Event(EventType.DEP_CLASSIFICATION, 10.0, task3);

        // Add in non-chronological order
        eventList.addEvent(event1);
        eventList.addEvent(event2);
        eventList.addEvent(event3);

        // Should retrieve in chronological order
        Event first = eventList.getNextEvent();
        assertEquals(5.0, first.getTime(), 0.001, "First event should be at time 5.0");

        Event second = eventList.getNextEvent();
        assertEquals(10.0, second.getTime(), 0.001, "Second event should be at time 10.0");

        Event third = eventList.getNextEvent();
        assertEquals(15.0, third.getTime(), 0.001, "Third event should be at time 15.0");

        assertFalse(eventList.hasEvents(), "Event list should be empty after retrieving all events");
    }

    @Test
    @DisplayName("Should handle events with same timestamp")
    void testSimultaneousEvents() {
        Event event1 = new Event(EventType.ARRIVAL, 10.0, task1);
        Event event2 = new Event(EventType.DEP_DATA_STORAGE, 10.0, task2);
        Event event3 = new Event(EventType.DEP_CLASSIFICATION, 10.0, task3);

        eventList.addEvent(event1);
        eventList.addEvent(event2);
        eventList.addEvent(event3);

        // All events should be retrieved (order may vary for same timestamp)
        Event first = eventList.getNextEvent();
        Event second = eventList.getNextEvent();
        Event third = eventList.getNextEvent();

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);
        
        assertEquals(10.0, first.getTime(), 0.001);
        assertEquals(10.0, second.getTime(), 0.001);
        assertEquals(10.0, third.getTime(), 0.001);

        assertFalse(eventList.hasEvents());
    }

    @Test
    @DisplayName("Clear should remove all events")
    void testClear() {
        eventList.addEvent(new Event(EventType.ARRIVAL, 5.0, task1));
        eventList.addEvent(new Event(EventType.ARRIVAL, 10.0, task2));
        eventList.addEvent(new Event(EventType.ARRIVAL, 15.0, task3));

        assertTrue(eventList.hasEvents(), "Event list should have events");

        eventList.clear();

        assertFalse(eventList.hasEvents(), "Event list should be empty after clear");
        assertNull(eventList.getNextEvent(), "Getting event after clear should return null");
    }

    @Test
    @DisplayName("Should handle large number of events")
    void testManyEvents() {
        // Add 1000 events with random times
        for (int i = 0; i < 1000; i++) {
            double time = Math.random() * 1000;
            Task task = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
            eventList.addEvent(new Event(EventType.ARRIVAL, time, task));
        }

        // Verify they come out in order
        double previousTime = -1.0;
        while (eventList.hasEvents()) {
            Event event = eventList.getNextEvent();
            assertTrue(event.getTime() >= previousTime, 
                "Events should be in chronological order");
            previousTime = event.getTime();
        }
    }

    @Test
    @DisplayName("Should handle retrieving from empty list multiple times")
    void testMultipleRetrievalsFromEmptyList() {
        assertNull(eventList.getNextEvent());
        assertNull(eventList.getNextEvent());
        assertNull(eventList.getNextEvent());
        assertFalse(eventList.hasEvents());
    }
}

