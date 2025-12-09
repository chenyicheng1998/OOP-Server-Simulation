package com.simulation.model;

/**
 * User types with different priority levels.
 * 
 * <p>Priority order: Enterprise VIP (3) > Personal VIP (2) > Normal (1)
 * Higher priority tasks are processed before lower priority tasks, regardless
 * of arrival time.
 * 
 * @author Cloud Simulation Team
 * @version 2.0
 */
public enum UserType {
    /** Normal user with priority 1 */
    NORMAL(1),
    
    /** Personal VIP user with priority 2 */
    PERSONAL_VIP(2),
    
    /** Enterprise VIP user with priority 3 */
    ENTERPRISE_VIP(3);
    
    private final int priority;
    
    /**
     * Constructs a UserType with the specified priority.
     * 
     * @param priority the priority level (higher = more important)
     */
    UserType(int priority) { this.priority = priority; }
    
    /**
     * Gets the priority value for this user type.
     * 
     * @return the priority (higher = more important)
     */
    public int getPriority() { return priority; }
}
