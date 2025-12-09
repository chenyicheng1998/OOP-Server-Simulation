package com.simulation.model;

/**
 * Types of computational tasks.
 * 
 * <p>Tasks are classified as either CPU-intensive or GPU-intensive, which determines
 * which compute nodes they are routed to after classification.
 * 
 * @author Cloud Simulation Team
 * @version 2.0
 */
public enum TaskType {
    /** CPU-intensive task */
    CPU,
    
    /** GPU-intensive task */
    GPU
}
