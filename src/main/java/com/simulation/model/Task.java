package com.simulation.model;
public class Task implements Comparable<Task> {
    private static int nextId = 1;
    private final int id;
    private final TaskType taskType;
    private final UserType userType;
    private final double arrivalTime;
    private double completionTime;
    public Task(TaskType taskType, UserType userType, double arrivalTime) {
        this.id = nextId++;
        this.taskType = taskType;
        this.userType = userType;
        this.arrivalTime = arrivalTime;
    }
    @Override
    public int compareTo(Task other) {
        int priorityComparison = Integer.compare(other.userType.getPriority(), this.userType.getPriority());
        if (priorityComparison != 0) return priorityComparison;
        return Double.compare(this.arrivalTime, other.arrivalTime);
    }
    public int getId() { return id; }
    public TaskType getTaskType() { return taskType; }
    public UserType getUserType() { return userType; }
    public double getArrivalTime() { return arrivalTime; }
    public double getCompletionTime() { return completionTime; }
    public void setCompletionTime(double completionTime) { this.completionTime = completionTime; }
    public double getTotalSystemTime() { return completionTime - arrivalTime; }
}
