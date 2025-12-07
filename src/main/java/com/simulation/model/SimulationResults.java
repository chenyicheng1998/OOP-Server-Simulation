package com.simulation.model;
import java.util.*;
public class SimulationResults {
    private int totalTasksArrived, totalTasksCompleted, cpuTasksCompleted, gpuTasksCompleted;
    private double totalSystemTime, maxSystemTime, minSystemTime = Double.MAX_VALUE;
    private int normalUserTasksCompleted, personalVipTasksCompleted, enterpriseVipTasksCompleted;
    private double totalSimulationTime;
    private List<Task> completedTasks = new ArrayList<>();

    public synchronized void recordArrival() { totalTasksArrived++; }
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
    public synchronized double getAverageSystemTime() { return totalTasksCompleted > 0 ? totalSystemTime / totalTasksCompleted : 0; }
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
