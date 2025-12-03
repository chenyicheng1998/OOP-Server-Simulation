package com.simulation.model;
public class Clock {
    private static Clock instance;
    private double time;
    private Clock() { this.time = 0.0; }
    public static Clock getInstance() {
        if (instance == null) instance = new Clock();
        return instance;
    }
    public double getTime() { return time; }
    public void setTime(double time) { this.time = time; }
    public void reset() { this.time = 0.0; }
}
