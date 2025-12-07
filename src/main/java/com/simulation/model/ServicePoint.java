package com.simulation.model;
import com.simulation.util.RandomGenerator;
import java.util.*;

/**
 * Represents a service point or queue in the simulation system.
 * Can function as either:
 * 1. Pure Queue: Just holds tasks waiting (e.g., CPU Queue, GPU Queue)
 * 2. Service Point: Processes tasks with servers (e.g., Data Storage, CPU Compute)
 */
public class ServicePoint {
    private final String name;
    private final int numServers;
    private final double meanServiceTime;
    private final Queue<Task> queue;
    private final boolean isPureQueue; // True for queues without service processing

    private int busyServers;
    private int tasksServed;
    private int maxQueueLength;
    private double totalBusyTime; // Total time servers have been busy
    private double lastUpdateTime; // Last time utilization was updated

    /**
     * Constructor for service points with processing capability
     */
    public ServicePoint(String name, int numServers, double meanServiceTime, boolean usePriorityQueue) {
        this.name = name;
        this.numServers = numServers;
        this.meanServiceTime = meanServiceTime;
        this.queue = usePriorityQueue ? new PriorityQueue<>() : new LinkedList<>();
        this.isPureQueue = (meanServiceTime == 0); // If service time is 0, it's a pure queue
        this.lastUpdateTime = 0;
    }

    public void addToQueue(Task task) {
        queue.add(task);
        if (queue.size() > maxQueueLength) maxQueueLength = queue.size();
    }

    /**
     * Remove task from queue without starting service (for pure queues)
     */
    public Task removeFromQueue() {
        return queue.isEmpty() ? null : queue.poll();
    }

    public boolean isAvailable() {
        return busyServers < numServers;
    }

    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    /**
     * Start service by marking a server as busy (for service points without their own queue)
     */
    public void startService(double currentTime) {
        if (busyServers < numServers) {
            updateUtilizationTracking(currentTime);
            busyServers++;
        }
    }

    /**
     * Begin service by taking task from own queue (for service points with queue)
     */
    public Task beginService(double currentTime) {
        if (queue.isEmpty() || !isAvailable()) return null;
        updateUtilizationTracking(currentTime);
        busyServers++;
        return queue.poll();
    }

    /**
     * Generate service time and track it for utilization calculation
     */
    public double getServiceTime() {
        return RandomGenerator.exponential(meanServiceTime);
    }

    /**
     * End service and update statistics
     */
    public void endService(double currentTime) {
        if (busyServers > 0) {
            updateUtilizationTracking(currentTime);
            busyServers--;
            tasksServed++;
        }
    }

    /**
     * Update the busy time tracking for accurate utilization calculation
     */
    private void updateUtilizationTracking(double currentTime) {
        if (!isPureQueue && currentTime > lastUpdateTime) {
            totalBusyTime += busyServers * (currentTime - lastUpdateTime);
            lastUpdateTime = currentTime;
        }
    }

    /**
     * Calculate utilization based on actual busy time
     */
    public double getUtilization(double currentTime) {
        if (isPureQueue || numServers == 0 || currentTime <= 0) {
            return 0.0;
        }

        // Update to current time before calculating
        updateUtilizationTracking(currentTime);

        // Utilization = total busy time / (simulation time * number of servers)
        double totalCapacityTime = currentTime * numServers;
        return totalCapacityTime > 0 ? totalBusyTime / totalCapacityTime : 0.0;
    }

    public String getName() { return name; }
    public int getQueueLength() { return queue.size(); }
    public int getBusyServers() { return busyServers; }
    public int getTasksServed() { return tasksServed; }
    public int getMaxQueueLength() { return maxQueueLength; }
    public boolean isPureQueue() { return isPureQueue; }

    public double getAverageServiceTime() {
        return tasksServed > 0 ? (totalBusyTime / tasksServed) : 0;
    }

    /**
     * Get average queue time (not yet implemented, returns 0)
     * TODO: Implement proper queue time tracking
     */
    public double getAverageQueueTime() {
        return 0.0;
    }

    public void reset() {
        queue.clear();
        busyServers = 0;
        tasksServed = 0;
        totalBusyTime = 0;
        maxQueueLength = 0;
        lastUpdateTime = 0;
    }
}
