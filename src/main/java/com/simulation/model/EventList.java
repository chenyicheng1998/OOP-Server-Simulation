package com.simulation.model;
import java.util.PriorityQueue;
public class EventList {
    private final PriorityQueue<Event> events = new PriorityQueue<>();
    public void addEvent(Event event) { events.add(event); }
    public Event getNextEvent() { return events.poll(); }
    public boolean hasEvents() { return !events.isEmpty(); }
    public void clear() { events.clear(); }
}
