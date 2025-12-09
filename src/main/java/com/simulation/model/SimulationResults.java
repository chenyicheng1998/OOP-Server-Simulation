package com.simulation.model;
import java.util.*;

/**
 * Collects and aggregates simulation statistics.
 * 
 * <p>SimulationResults tracks various performance metrics during simulation execution,
 * including:
 * <ul>
 *   <li>Task arrival and completion counts</li>
 *   <li>Average, min, and max system times</li>
 *   <li>Throughput (tasks per second)</li>
 *   <li>Breakdown by task type (CPU/GPU)</li>
 *   <li>Breakdown by user type (Normal/VIP)</li>
 * </ul>
 * 
 * <p>Thread Safety: Methods are synchronized to support concurrent access from
 * simulation thread and UI thread.
 * 
 * @author Cloud Simulation Team
 * @version 2.0
 * @see SimulationEngine
 * @see Task
 */
public class SimulationResults {
    private int totalTasksArrived, totalTasksCompleted, cpuTasksCompleted, gpuTasksCompleted;
    private double totalSystemTime, maxSystemTime, minSystemTime = Double.MAX_VALUE;
    private int normalUserTasksCompleted, personalVipTasksCompleted, enterpriseVipTasksCompleted;
    private double totalSimulationTime;
    private List<Task> completedTasks = new ArrayList<>();

    /**
     * Records a task arrival.
     * 
     * <p>Increments the total arrived tasks counter.
     */
    public synchronized void recordArrival() { totalTasksArrived++; }
    
    /**
     * Records a task completion and updates all relevant statistics.
     * 
     * @param task the completed task
     */
    public synchronized void recordCompletion(Task task) {
        totalTasksCompleted++;
        completedTasks.add(task);
        if (task.getTaskType() == TaskType.CPU) cpuTasksCompleted++; else gpuTasksCompleted++;
        switch (task.getUserType()) {
            case NORMAL: normalUserTasksCompleted++; break;
            case PERSONAL_VIP: personalVipTasksCompleted++; break;
            case ENTERPRISE_VIP: enterpriseVipTasksCompleted++; break;
        }
        double systemTime = task.getTotalSystemTime();
        totalSystemTime += systemTime;
        maxSystemTime = Math.max(maxSystemTime, systemTime);
        minSystemTime = Math.min(minSystemTime, systemTime);
    }
    /**
     * Calculates the average system time for completed tasks.
     * 
     * @return the average system time in seconds, or 0 if no tasks completed
     */
    public synchronized double getAverageSystemTime() { return totalTasksCompleted > 0 ? totalSystemTime / totalTasksCompleted : 0; }
    
    /**
     * Calculates the throughput (tasks completed per second).
     * 
     * @param simTime the total simulation time
     * @return throughput in tasks/second, or 0 if simTime is 0
     */
    public synchronized double getThroughput(double simTime) { return simTime > 0 ? totalTasksCompleted / simTime : 0; }
    public synchronized int getTotalTasksArrived() { return totalTasksArrived; }
    public synchronized int getTotalTasksCompleted() { return totalTasksCompleted; }
    public synchronized int getCpuTasksCompleted() { return cpuTasksCompleted; }
    public synchronized int getGpuTasksCompleted() { return gpuTasksCompleted; }
    public synchronized double getMaxSystemTime() { return maxSystemTime; }
    public synchronized double getMinSystemTime() { return minSystemTime == Double.MAX_VALUE ? 0 : minSystemTime; }
    public synchronized int getNormalUserTasksCompleted() { return normalUserTasksCompleted; }
    public synchronized int getPersonalVipTasksCompleted() { return personalVipTasksCompleted; }
    public synchronized int getEnterpriseVipTasksCompleted() { return enterpriseVipTasksCompleted; }
    /**
     * Resets all statistics to initial state.
     */
    public synchronized void reset() {
        totalTasksArrived = totalTasksCompleted = cpuTasksCompleted = gpuTasksCompleted = 0;
        totalSystemTime = maxSystemTime = 0;
        minSystemTime = Double.MAX_VALUE;
        normalUserTasksCompleted = personalVipTasksCompleted = enterpriseVipTasksCompleted = 0;
        totalSimulationTime = 0;
        completedTasks.clear();
    }

    // New methods for database integration
    public synchronized double getTotalSimulationTime() { return totalSimulationTime; }
    public synchronized void setTotalSimulationTime(double time) { this.totalSimulationTime = time; }
    public synchronized List<Task> getCompletedTasks() { return new ArrayList<>(completedTasks); }
    public synchronized int getTotalArrivedTasks() { return totalTasksArrived; }
    public synchronized int getTotalCompletedTasks() { return totalTasksCompleted; }
    public synchronized double getThroughput() { return getThroughput(totalSimulationTime); }
}
