package com.simulation.model;

/**
 * Global simulation clock using singleton pattern.
 * 
 * <p>The Clock maintains the current simulation time. It uses the singleton pattern
 * to ensure there is only one clock instance throughout the simulation, providing
 * a consistent time reference for all components.
 * 
 * <p>Thread Safety: This class is not thread-safe. Access should be synchronized
 * externally if used from multiple threads.
 * 
 * @author Cloud Simulation Team
 * @version 2.0
 * @see SimulationEngine
 */
public class Clock {
    private static Clock instance;
    private double time;
    private Clock() { this.time = 0.0; }
    
    /**
     * Gets the singleton Clock instance.
     * 
     * @return the Clock instance
     */
    public static Clock getInstance() {
        if (instance == null) instance = new Clock();
        return instance;
    }
    
    /**
     * Gets the current simulation time.
     * 
     * @return the current simulation time
     */
    public double getTime() { return time; }
    
    /**
     * Sets the simulation time.
     * 
     * @param time the new simulation time
     */
    public void setTime(double time) { this.time = time; }
    
    /**
     * Resets the clock to time 0.0.
     */
    public void reset() { this.time = 0.0; }
}
