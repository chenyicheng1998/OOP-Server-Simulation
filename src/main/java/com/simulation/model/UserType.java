package com.simulation.model;
public enum UserType {
    NORMAL(1),
    PERSONAL_VIP(2),
    ENTERPRISE_VIP(3);
    private final int priority;
    UserType(int priority) { this.priority = priority; }
    public int getPriority() { return priority; }
}
