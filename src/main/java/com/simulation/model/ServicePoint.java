package com.simulation.model;
import com.simulation.util.RandomGenerator;
import java.util.*;

/**
 * Represents a service point or queue in the simulation system.
 * 
 * <p>A ServicePoint can function in two modes:
 * <ol>
 *   <li><b>Pure Queue</b>: Just holds tasks waiting (e.g., CPU Queue, GPU Queue)
 *       - No service time (meanServiceTime = 0)
 *       - Tasks are only stored and retrieved</li>
 *   <li><b>Service Point</b>: Processes tasks with servers (e.g., Data Storage, CPU Compute)
 *       - Has service time > 0
 *       - Can have multiple servers (numServers)
 *       - Tracks utilization and busy time</li>
 * </ol>
 * 
 * <p>ServicePoints support both FIFO queues (LinkedList) and priority queues (PriorityQueue)
 * based on task priority. Thread-safe methods ensure safe concurrent access from
 * simulation thread and UI thread.
 * 
 * <p>Utilization is calculated as: Total Busy Time / (Simulation Time × Number of Servers)
 * 
 * @author Cloud Simulation Team
 * @version 2.0
 * @see Task
 * @see SimulationEngine
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
     * Constructs a new ServicePoint.
     * 
     * @param name the name of the service point
     * @param numServers the number of servers (for service points) or capacity (for queues)
     * @param meanServiceTime the mean service time (0 for pure queues)
     * @param usePriorityQueue true to use priority queue, false for FIFO queue
     */
    public ServicePoint(String name, int numServers, double meanServiceTime, boolean usePriorityQueue) {
        this.name = name;
        this.numServers = numServers;
        this.meanServiceTime = meanServiceTime;
        this.queue = usePriorityQueue ? new PriorityQueue<>() : new LinkedList<>();
        this.isPureQueue = (meanServiceTime == 0); // If service time is 0, it's a pure queue
        this.lastUpdateTime = 0;
    }

    /**
     * Adds a task to the queue.
     * 
     * <p>Thread-safe method that adds task and updates maximum queue length.
     * 
     * @param task the task to add
     */
    public synchronized void addToQueue(Task task) {
        queue.add(task);
        if (queue.size() > maxQueueLength) maxQueueLength = queue.size();
    }

    /**
     * Remove task from queue without starting service (for pure queues)
     */
    public synchronized Task removeFromQueue() {
        return queue.isEmpty() ? null : queue.poll();
    }

    /**
     * Checks if any servers are available for processing.
     * 
     * @return true if at least one server is available, false otherwise
     */
    public synchronized boolean isAvailable() {
        return busyServers < numServers;
    }

    /**
     * Checks if the queue is empty.
     * 
     * @return true if queue is empty, false otherwise
     */
    public synchronized boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    /**
     * Starts service by marking a server as busy.
     * 
     * <p>Used for service points that receive tasks from external queues.
     * 
     * @param currentTime the current simulation time
     */
    public synchronized void startService(double currentTime) {
        if (busyServers < numServers) {
            updateUtilizationTracking(currentTime);
            busyServers++;
        }
    }

    /**
     * Begins service by taking a task from the queue.
     * 
     * <p>Removes task from queue and marks a server as busy. Returns null if
     * queue is empty or no servers available.
     * 
     * @param currentTime the current simulation time
     * @return the task that began service, or null if none available
     */
    public synchronized Task beginService(double currentTime) {
        if (queue.isEmpty() || !isAvailable()) return null;
        updateUtilizationTracking(currentTime);
        busyServers++;
        return queue.poll();
    }

    /**
     * Generates a random service time from exponential distribution.
     * 
     * <p>Service time is generated using exponential distribution with the
     * configured mean service time.
     * 
     * @return a random service time (always positive)
     */
    public double getServiceTime() {
        return RandomGenerator.exponential(meanServiceTime);
    }

    /**
     * Ends service and updates statistics.
     * 
     * <p>Frees a server and increments tasks served counter. Updates utilization
     * tracking for accurate statistics.
     * 
     * @param currentTime the current simulation time
     */
    public synchronized void endService(double currentTime) {
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
     * Calculates server utilization based on actual busy time.
     * 
     * <p>Utilization = Total Busy Time / (Simulation Time × Number of Servers)
     * 
     * <p>Returns 0 for pure queues or if no servers configured.
     * 
     * @param currentTime the current simulation time
     * @return utilization value between 0.0 and 1.0 (0% to 100%)
     */
    public synchronized double getUtilization(double currentTime) {
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
    public synchronized int getQueueLength() { return queue.size(); }
    public synchronized int getBusyServers() { return busyServers; }
    public synchronized int getTasksServed() { return tasksServed; }
    public synchronized int getMaxQueueLength() { return maxQueueLength; }
    public boolean isPureQueue() { return isPureQueue; }

    public synchronized double getAverageServiceTime() {
        return tasksServed > 0 ? (totalBusyTime / tasksServed) : 0;
    }

    /**
     * Get average queue time (not yet implemented, returns 0)
     * TODO: Implement proper queue time tracking
     */
    public double getAverageQueueTime() {
        return 0.0;
    }

    public synchronized void reset() {
        queue.clear();
        busyServers = 0;
        tasksServed = 0;
        totalBusyTime = 0;
        maxQueueLength = 0;
        lastUpdateTime = 0;
    }
}
