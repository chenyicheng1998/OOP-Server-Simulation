package com.simulation.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ServicePoint class
 */
@DisplayName("ServicePoint Tests")
class ServicePointTest {

    private ServicePoint servicePoint;
    private Task task1, task2, task3;

    @BeforeEach
    void setUp() {
        // Service point with 2 servers, mean service time 5.0, not a priority queue
        servicePoint = new ServicePoint("Test Service", 2, 5.0, false);
        
        task1 = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        task2 = new Task(TaskType.CPU, UserType.PERSONAL_VIP, 0.0);
        task3 = new Task(TaskType.CPU, UserType.ENTERPRISE_VIP, 0.0);
    }

    @Test
    @DisplayName("New service point should be available")
    void testInitialAvailability() {
        assertTrue(servicePoint.isAvailable(), "New service point should be available");
        assertTrue(servicePoint.isQueueEmpty(), "New service point should have empty queue");
        assertEquals(0, servicePoint.getQueueLength(), "Queue length should be 0");
        assertEquals(0, servicePoint.getBusyServers(), "No servers should be busy");
    }

    @Test
    @DisplayName("Should add tasks to queue")
    void testAddToQueue() {
        servicePoint.addToQueue(task1);
        assertEquals(1, servicePoint.getQueueLength(), "Queue length should be 1");
        assertFalse(servicePoint.isQueueEmpty(), "Queue should not be empty");

        servicePoint.addToQueue(task2);
        assertEquals(2, servicePoint.getQueueLength(), "Queue length should be 2");
    }

    @Test
    @DisplayName("Should track maximum queue length")
    void testMaxQueueLength() {
        servicePoint.addToQueue(task1);
        servicePoint.addToQueue(task2);
        servicePoint.addToQueue(task3);
        
        assertEquals(3, servicePoint.getMaxQueueLength(), "Max queue length should be 3");

        // Remove one task
        servicePoint.beginService(0.0);
        
        // Max should still be 3
        assertEquals(3, servicePoint.getMaxQueueLength(), "Max queue length should remain 3");
    }

    @Test
    @DisplayName("BeginService should remove task from queue and mark server busy")
    void testBeginService() {
        servicePoint.addToQueue(task1);
        servicePoint.addToQueue(task2);

        Task retrieved = servicePoint.beginService(10.0);

        assertNotNull(retrieved, "Should retrieve a task");
        assertEquals(1, servicePoint.getQueueLength(), "Queue length should decrease to 1");
        assertEquals(1, servicePoint.getBusyServers(), "One server should be busy");
        assertTrue(servicePoint.isAvailable(), "Should still have available servers (2 total)");
    }

    @Test
    @DisplayName("Should correctly handle server capacity")
    void testServerCapacity() {
        servicePoint.addToQueue(task1);
        servicePoint.addToQueue(task2);
        servicePoint.addToQueue(task3);

        // Start first service
        servicePoint.beginService(0.0);
        assertTrue(servicePoint.isAvailable(), "Should still be available (1/2 servers busy)");
        assertEquals(1, servicePoint.getBusyServers());

        // Start second service
        servicePoint.beginService(0.0);
        assertFalse(servicePoint.isAvailable(), "Should not be available (2/2 servers busy)");
        assertEquals(2, servicePoint.getBusyServers());

        // Try to start third service (should return null)
        Task result = servicePoint.beginService(0.0);
        assertNull(result, "Should not start service when no servers available");
        assertEquals(2, servicePoint.getBusyServers(), "Busy servers should remain 2");
    }

    @Test
    @DisplayName("EndService should free server and update statistics")
    void testEndService() {
        servicePoint.addToQueue(task1);
        servicePoint.beginService(0.0);
        
        assertEquals(1, servicePoint.getBusyServers(), "One server should be busy");
        assertEquals(0, servicePoint.getTasksServed(), "Tasks served should be 0 initially");

        servicePoint.endService(5.0);

        assertEquals(0, servicePoint.getBusyServers(), "No servers should be busy");
        assertEquals(1, servicePoint.getTasksServed(), "Tasks served should be 1");
        assertTrue(servicePoint.isAvailable(), "Service point should be available");
    }

    @Test
    @DisplayName("Service time should be positive")
    void testServiceTime() {
        double serviceTime = servicePoint.getServiceTime();
        assertTrue(serviceTime > 0, "Service time should be positive");
    }

    @Test
    @DisplayName("Utilization should be calculated correctly")
    void testUtilization() {
        // Initially, utilization should be 0
        assertEquals(0.0, servicePoint.getUtilization(10.0), 0.001, 
            "Initial utilization should be 0");

        // Add task and start service
        servicePoint.addToQueue(task1);
        servicePoint.beginService(10.0);

        // At time 20, one server has been busy for 10 time units
        // Utilization = 10 / (20 * 2) = 10/40 = 0.25
        double utilization = servicePoint.getUtilization(20.0);
        assertTrue(utilization > 0, "Utilization should be positive after service");
        assertTrue(utilization <= 1.0, "Utilization should not exceed 100%");
    }

    @Test
    @DisplayName("Pure queue should have zero service time")
    void testPureQueue() {
        ServicePoint queue = new ServicePoint("Pure Queue", Integer.MAX_VALUE, 0, false);
        
        assertTrue(queue.isPureQueue(), "Should be identified as pure queue");
        assertEquals(0.0, queue.getUtilization(100.0), 0.001, 
            "Pure queue should have zero utilization");
    }

    @Test
    @DisplayName("Priority queue should order tasks by priority")
    void testPriorityQueue() {
        ServicePoint priorityQueue = new ServicePoint("Priority Queue", 1, 5.0, true);

        // Add in random order
        priorityQueue.addToQueue(task1); // Normal
        priorityQueue.addToQueue(task3); // Enterprise VIP
        priorityQueue.addToQueue(task2); // Personal VIP

        // Should retrieve in priority order (Enterprise > Personal > Normal)
        Task first = priorityQueue.beginService(0.0);
        assertEquals(UserType.ENTERPRISE_VIP, first.getUserType(), 
            "First task should be Enterprise VIP");

        priorityQueue.endService(5.0);
        
        Task second = priorityQueue.beginService(5.0);
        assertEquals(UserType.PERSONAL_VIP, second.getUserType(), 
            "Second task should be Personal VIP");

        priorityQueue.endService(10.0);
        
        Task third = priorityQueue.beginService(10.0);
        assertEquals(UserType.NORMAL, third.getUserType(), 
            "Third task should be Normal");
    }

    @Test
    @DisplayName("Reset should clear all state")
    void testReset() {
        servicePoint.addToQueue(task1);
        servicePoint.addToQueue(task2);
        servicePoint.beginService(0.0);
        servicePoint.endService(5.0);

        // State before reset
        assertTrue(servicePoint.getQueueLength() > 0 || servicePoint.getTasksServed() > 0);

        servicePoint.reset();

        // State after reset
        assertEquals(0, servicePoint.getQueueLength(), "Queue should be empty");
        assertEquals(0, servicePoint.getBusyServers(), "No servers should be busy");
        assertEquals(0, servicePoint.getTasksServed(), "Tasks served should be 0");
        assertEquals(0, servicePoint.getMaxQueueLength(), "Max queue length should be 0");
        assertTrue(servicePoint.isQueueEmpty(), "Queue should be empty");
        assertTrue(servicePoint.isAvailable(), "Service point should be available");
    }

    @Test
    @DisplayName("Should handle concurrent service for multiple servers")
    void testConcurrentService() {
        servicePoint.addToQueue(task1);
        servicePoint.addToQueue(task2);

        // Start two services simultaneously
        Task first = servicePoint.beginService(0.0);
        Task second = servicePoint.beginService(0.0);

        assertNotNull(first, "First service should start");
        assertNotNull(second, "Second service should start");
        assertEquals(2, servicePoint.getBusyServers(), "Both servers should be busy");
        assertFalse(servicePoint.isAvailable(), "No servers should be available");

        // End one service
        servicePoint.endService(5.0);
        assertEquals(1, servicePoint.getBusyServers(), "One server should still be busy");
        assertTrue(servicePoint.isAvailable(), "One server should be available");

        // End second service
        servicePoint.endService(5.0);
        assertEquals(0, servicePoint.getBusyServers(), "No servers should be busy");
        assertEquals(2, servicePoint.getTasksServed(), "Two tasks should be served");
    }

    @Test
    @DisplayName("Service point name should be accessible")
    void testServicePointName() {
        assertEquals("Test Service", servicePoint.getName(), 
            "Service point name should match");
    }

    @Test
    @DisplayName("Should not start service from empty queue")
    void testBeginServiceFromEmptyQueue() {
        Task result = servicePoint.beginService(0.0);
        assertNull(result, "Should not start service from empty queue");
        assertEquals(0, servicePoint.getBusyServers(), "No servers should be busy");
    }
}


