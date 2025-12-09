package com.simulation.model;

/**
 * Represents a computational task in the simulation system.
 * 
 * <p>A Task represents a single unit of work submitted by a user. Each task
 * has a unique ID, a task type (CPU or GPU), a user type (Normal, Personal VIP,
 * or Enterprise VIP), and timing information.
 * 
 * <p>Tasks implement {@link Comparable} to support priority-based scheduling.
 * Priority is determined first by user type (Enterprise VIP > Personal VIP > Normal),
 * then by arrival time (FCFS - First Come First Served) for tasks with the same priority.
 * 
 * @author Cloud Simulation Team
 * @version 2.0
 * @see TaskType
 * @see UserType
 * @see Comparable
 */
public class Task implements Comparable<Task> {
    private static int nextId = 1;
    private final int id;
    private final TaskType taskType;
    private final UserType userType;
    private final double arrivalTime;
    private double completionTime;
    /**
     * Constructs a new Task with the specified parameters.
     * 
     * @param taskType the type of task (CPU or GPU)
     * @param userType the type of user (Normal, Personal VIP, or Enterprise VIP)
     * @param arrivalTime the simulation time when task arrives
     */
    public Task(TaskType taskType, UserType userType, double arrivalTime) {
        this.id = nextId++;
        this.taskType = taskType;
        this.userType = userType;
        this.arrivalTime = arrivalTime;
    }
    
    /**
     * Compares this task with another task for priority ordering.
     * 
     * <p>Comparison is based on:
     * <ol>
     *   <li>User type priority (higher priority first)</li>
     *   <li>Arrival time (earlier arrival first, for same priority)</li>
     * </ol>
     * 
     * @param other the task to compare with
     * @return negative if this task has higher priority, positive if lower,
     *         zero if equal priority and arrival time
     */
    @Override
    public int compareTo(Task other) {
        int priorityComparison = Integer.compare(other.userType.getPriority(), this.userType.getPriority());
        if (priorityComparison != 0) return priorityComparison;
        return Double.compare(this.arrivalTime, other.arrivalTime);
    }
    
    /**
     * Gets the unique task ID.
     * 
     * @return the task ID
     */
    public int getId() { return id; }
    
    /**
     * Gets the task type (CPU or GPU).
     * 
     * @return the task type
     */
    public TaskType getTaskType() { return taskType; }
    
    /**
     * Gets the user type (Normal, Personal VIP, or Enterprise VIP).
     * 
     * @return the user type
     */
    public UserType getUserType() { return userType; }
    
    /**
     * Gets the arrival time of the task.
     * 
     * @return the simulation time when task arrived
     */
    public double getArrivalTime() { return arrivalTime; }
    
    /**
     * Gets the completion time of the task.
     * 
     * @return the simulation time when task completed, or 0 if not completed
     */
    public double getCompletionTime() { return completionTime; }
    
    /**
     * Sets the completion time of the task.
     * 
     * @param completionTime the simulation time when task completed
     */
    public void setCompletionTime(double completionTime) { this.completionTime = completionTime; }
    
    /**
     * Calculates the total time the task spent in the system.
     * 
     * @return completion time - arrival time, or 0 if not completed
     */
    public double getTotalSystemTime() { return completionTime - arrivalTime; }
}
