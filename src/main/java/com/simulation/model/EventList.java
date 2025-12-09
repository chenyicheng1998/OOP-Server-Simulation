package com.simulation.model;
import java.util.PriorityQueue;

/**
 * Manages the priority queue of simulation events.
 * 
 * <p>EventList maintains a priority queue of events ordered by scheduled time.
 * Events are automatically sorted so that the earliest event is always retrieved first.
 * 
 * <p>This class is a wrapper around {@link PriorityQueue} that provides a clean
 * interface for event management in the simulation engine.
 * 
 * @author Cloud Simulation Team
 * @version 2.0
 * @see Event
 * @see PriorityQueue
 */
public class EventList {
    private final PriorityQueue<Event> events = new PriorityQueue<>();
    
    /**
     * Adds an event to the event list.
     * 
     * <p>The event will be inserted into the priority queue based on its scheduled time.
     * 
     * @param event the event to add
     */
    public void addEvent(Event event) { events.add(event); }
    
    /**
     * Retrieves and removes the next event (earliest scheduled time).
     * 
     * @return the next event, or null if event list is empty
     */
    public Event getNextEvent() { return events.poll(); }
    
    /**
     * Checks if there are any events in the list.
     * 
     * @return true if there are events, false otherwise
     */
    public boolean hasEvents() { return !events.isEmpty(); }
    
    /**
     * Removes all events from the list.
     */
    public void clear() { events.clear(); }
}
