package com.simulation.model;
import com.simulation.util.RandomGenerator;
import java.util.*;
public class ServicePoint {
    private final String name;
    private final int numServers;
    private final double meanServiceTime;
    private final Queue<Task> queue;
    private int busyServers, tasksServed, maxQueueLength;
    private double totalServiceTime;
    public ServicePoint(String name, int numServers, double meanServiceTime, boolean usePriorityQueue) {
        this.name = name;
        this.numServers = numServers;
        this.meanServiceTime = meanServiceTime;
        this.queue = usePriorityQueue ? new PriorityQueue<>() : new LinkedList<>();
    }
    public void addToQueue(Task task) {
        queue.add(task);
        if (queue.size() > maxQueueLength) maxQueueLength = queue.size();
    }
    public boolean isAvailable() { return busyServers < numServers; }
    public boolean isQueueEmpty() { return queue.isEmpty(); }
    public Task beginService(double currentTime) {
        if (queue.isEmpty() || !isAvailable()) return null;
        busyServers++;
        return queue.poll();
    }
    public double getServiceTime() {
        double serviceTime = RandomGenerator.exponential(meanServiceTime);
        totalServiceTime += serviceTime;
        return serviceTime;
    }
    public void endService() {
        if (busyServers > 0) { busyServers--; tasksServed++; }
    }
    public String getName() { return name; }
    public int getQueueLength() { return queue.size(); }
    public int getBusyServers() { return busyServers; }
    public int getTasksServed() { return tasksServed; }
    public int getMaxQueueLength() { return maxQueueLength; }
    public double getUtilization(double totalTime) {
        return (totalTime > 0 && numServers > 0) ? totalServiceTime / (totalTime * numServers) : 0;
    }
    public double getAverageServiceTime() { return tasksServed > 0 ? totalServiceTime / tasksServed : 0; }
    public double getAverageQueueTime() { return 0; }
    public void reset() {
        queue.clear();
        busyServers = 0;
        tasksServed = 0;
        totalServiceTime = 0;
        maxQueueLength = 0;
    }
}
