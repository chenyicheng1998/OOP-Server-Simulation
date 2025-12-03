package com.simulation.model;
import java.util.*;
public class SimulationResults {
    private int totalTasksArrived, totalTasksCompleted, cpuTasksCompleted, gpuTasksCompleted;
    private double totalSystemTime, maxSystemTime, minSystemTime = Double.MAX_VALUE;
    private int normalUserTasksCompleted, personalVipTasksCompleted, enterpriseVipTasksCompleted;
    private double totalSimulationTime;
    private List<Task> completedTasks = new ArrayList<>();

    public void recordArrival() { totalTasksArrived++; }
    public void recordCompletion(Task task) {
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
    public double getAverageSystemTime() { return totalTasksCompleted > 0 ? totalSystemTime / totalTasksCompleted : 0; }
    public double getThroughput(double simTime) { return simTime > 0 ? totalTasksCompleted / simTime : 0; }
    public int getTotalTasksArrived() { return totalTasksArrived; }
    public int getTotalTasksCompleted() { return totalTasksCompleted; }
    public int getCpuTasksCompleted() { return cpuTasksCompleted; }
    public int getGpuTasksCompleted() { return gpuTasksCompleted; }
    public double getMaxSystemTime() { return maxSystemTime; }
    public double getMinSystemTime() { return minSystemTime == Double.MAX_VALUE ? 0 : minSystemTime; }
    public int getNormalUserTasksCompleted() { return normalUserTasksCompleted; }
    public int getPersonalVipTasksCompleted() { return personalVipTasksCompleted; }
    public int getEnterpriseVipTasksCompleted() { return enterpriseVipTasksCompleted; }
    public void reset() {
        totalTasksArrived = totalTasksCompleted = cpuTasksCompleted = gpuTasksCompleted = 0;
        totalSystemTime = maxSystemTime = 0;
        minSystemTime = Double.MAX_VALUE;
        normalUserTasksCompleted = personalVipTasksCompleted = enterpriseVipTasksCompleted = 0;
        totalSimulationTime = 0;
        completedTasks.clear();
    }

    // New methods for database integration
    public double getTotalSimulationTime() { return totalSimulationTime; }
    public void setTotalSimulationTime(double time) { this.totalSimulationTime = time; }
    public List<Task> getCompletedTasks() { return new ArrayList<>(completedTasks); }
    public int getTotalArrivedTasks() { return totalTasksArrived; }
    public int getTotalCompletedTasks() { return totalTasksCompleted; }
    public double getThroughput() { return getThroughput(totalSimulationTime); }
}
