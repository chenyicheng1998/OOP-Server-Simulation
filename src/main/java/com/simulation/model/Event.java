package com.simulation.model;
public class Event implements Comparable<Event> {
    private final EventType type;
    private final double time;
    private final Task task;
    public Event(EventType type, double time, Task task) {
        this.type = type;
        this.time = time;
        this.task = task;
    }
    @Override
    public int compareTo(Event other) {
        return Double.compare(this.time, other.time);
    }
    public EventType getType() { return type; }
    public double getTime() { return time; }
    public Task getTask() { return task; }
}
