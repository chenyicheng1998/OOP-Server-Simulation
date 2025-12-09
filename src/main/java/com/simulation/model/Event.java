package com.simulation.model;

/**
 * Represents a discrete event in the simulation.
 * 
 * <p>Events are the fundamental units of the discrete-event simulation engine.
 * Each event has a type (arrival, departure from service point, etc.), a scheduled
 * time, and an associated task.
 * 
 * <p>Events implement {@link Comparable} to support chronological ordering in
 * the event queue. Events are compared by their scheduled time.
 * 
 * @author Cloud Simulation Team
 * @version 2.0
 * @see EventType
 * @see EventList
 * @see Comparable
 */
public class Event implements Comparable<Event> {
    private final EventType type;
    private final double time;
    private final Task task;
    /**
     * Constructs a new Event.
     * 
     * @param type the type of event
     * @param time the simulation time when event should occur
     * @param task the task associated with this event
     */
    public Event(EventType type, double time, Task task) {
        this.type = type;
        this.time = time;
        this.task = task;
    }
    
    /**
     * Compares this event with another event by scheduled time.
     * 
     * @param other the event to compare with
     * @return negative if this event occurs earlier, positive if later,
     *         zero if same time
     */
    @Override
    public int compareTo(Event other) {
        return Double.compare(this.time, other.time);
    }
    
    /**
     * Gets the event type.
     * 
     * @return the event type
     */
    public EventType getType() { return type; }
    
    /**
     * Gets the scheduled time of the event.
     * 
     * @return the simulation time when event should occur
     */
    public double getTime() { return time; }
    
    /**
     * Gets the task associated with this event.
     * 
     * @return the task, or null if no task associated
     */
    public Task getTask() { return task; }
}
