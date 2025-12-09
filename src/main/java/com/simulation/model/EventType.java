package com.simulation.model;

/**
 * Types of events in the simulation.
 * 
 * <p>Events represent discrete occurrences in the simulation timeline. Each event
 * type corresponds to a specific stage in the task processing pipeline.
 * 
 * @author Cloud Simulation Team
 * @version 2.0
 */
public enum EventType {
    /** Task arrives in the system */
    ARRIVAL,
    
    /** Task completes data storage service */
    DEP_DATA_STORAGE,
    
    /** Task completes classification service */
    DEP_CLASSIFICATION,
    
    /** Task completes CPU compute service */
    DEP_CPU_COMPUTE,
    
    /** Task completes GPU compute service */
    DEP_GPU_COMPUTE,
    
    /** Task completes result storage service and exits system */
    DEP_RESULT_STORAGE
}
