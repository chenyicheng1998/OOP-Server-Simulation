package com.simulation.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimulationResults class
 */
@DisplayName("SimulationResults Tests")
class SimulationResultsTest {

    private SimulationResults results;

    @BeforeEach
    void setUp() {
        results = new SimulationResults();
    }

    @Test
    @DisplayName("New results should have zero statistics")
    void testInitialState() {
        assertEquals(0, results.getTotalArrivedTasks(), "Arrived tasks should be 0");
        assertEquals(0, results.getTotalCompletedTasks(), "Completed tasks should be 0");
        assertEquals(0, results.getCpuTasksCompleted(), "CPU tasks should be 0");
        assertEquals(0, results.getGpuTasksCompleted(), "GPU tasks should be 0");
        assertEquals(0.0, results.getAverageSystemTime(), 0.001, "Avg system time should be 0");
        assertEquals(0.0, results.getThroughput(), 0.001, "Throughput should be 0");
    }

    @Test
    @DisplayName("Should record task arrivals")
    void testRecordArrival() {
        results.recordArrival();
        assertEquals(1, results.getTotalArrivedTasks(), "Should have 1 arrival");

        results.recordArrival();
        results.recordArrival();
        assertEquals(3, results.getTotalArrivedTasks(), "Should have 3 arrivals");
    }

    @Test
    @DisplayName("Should record task completions and track by type")
    void testRecordCompletion() {
        Task cpuTask = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        cpuTask.setCompletionTime(10.0);

        Task gpuTask = new Task(TaskType.GPU, UserType.PERSONAL_VIP, 0.0);
        gpuTask.setCompletionTime(15.0);

        results.recordCompletion(cpuTask);
        assertEquals(1, results.getTotalCompletedTasks(), "Should have 1 completed task");
        assertEquals(1, results.getCpuTasksCompleted(), "Should have 1 CPU task");
        assertEquals(0, results.getGpuTasksCompleted(), "Should have 0 GPU tasks");

        results.recordCompletion(gpuTask);
        assertEquals(2, results.getTotalCompletedTasks(), "Should have 2 completed tasks");
        assertEquals(1, results.getCpuTasksCompleted(), "Should have 1 CPU task");
        assertEquals(1, results.getGpuTasksCompleted(), "Should have 1 GPU task");
    }

    @Test
    @DisplayName("Should track user type statistics")
    void testUserTypeStatistics() {
        Task normalTask = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        normalTask.setCompletionTime(10.0);

        Task personalTask = new Task(TaskType.CPU, UserType.PERSONAL_VIP, 0.0);
        personalTask.setCompletionTime(15.0);

        Task enterpriseTask = new Task(TaskType.GPU, UserType.ENTERPRISE_VIP, 0.0);
        enterpriseTask.setCompletionTime(20.0);

        results.recordCompletion(normalTask);
        results.recordCompletion(personalTask);
        results.recordCompletion(enterpriseTask);

        assertEquals(1, results.getNormalUserTasksCompleted(), "Should have 1 normal user task");
        assertEquals(1, results.getPersonalVipTasksCompleted(), "Should have 1 personal VIP task");
        assertEquals(1, results.getEnterpriseVipTasksCompleted(), "Should have 1 enterprise VIP task");
    }

    @Test
    @DisplayName("Should calculate average system time correctly")
    void testAverageSystemTime() {
        Task task1 = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        task1.setCompletionTime(10.0); // System time = 10.0

        Task task2 = new Task(TaskType.CPU, UserType.NORMAL, 5.0);
        task2.setCompletionTime(20.0); // System time = 15.0

        Task task3 = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        task3.setCompletionTime(5.0); // System time = 5.0

        results.recordCompletion(task1);
        results.recordCompletion(task2);
        results.recordCompletion(task3);

        // Average = (10 + 15 + 5) / 3 = 10.0
        assertEquals(10.0, results.getAverageSystemTime(), 0.001, 
            "Average system time should be 10.0");
    }

    @Test
    @DisplayName("Should track min and max system time")
    void testMinMaxSystemTime() {
        Task task1 = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        task1.setCompletionTime(20.0); // System time = 20.0

        Task task2 = new Task(TaskType.CPU, UserType.NORMAL, 5.0);
        task2.setCompletionTime(10.0); // System time = 5.0

        Task task3 = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        task3.setCompletionTime(50.0); // System time = 50.0

        results.recordCompletion(task1);
        results.recordCompletion(task2);
        results.recordCompletion(task3);

        assertEquals(5.0, results.getMinSystemTime(), 0.001, "Min system time should be 5.0");
        assertEquals(50.0, results.getMaxSystemTime(), 0.001, "Max system time should be 50.0");
    }

    @Test
    @DisplayName("Should calculate throughput correctly")
    void testThroughput() {
        Task task1 = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        task1.setCompletionTime(10.0);

        Task task2 = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        task2.setCompletionTime(15.0);

        results.recordCompletion(task1);
        results.recordCompletion(task2);

        results.setTotalSimulationTime(100.0);

        // Throughput = 2 tasks / 100 seconds = 0.02 tasks/second
        assertEquals(0.02, results.getThroughput(), 0.001, 
            "Throughput should be 0.02 tasks/second");
    }

    @Test
    @DisplayName("Should return completed tasks list")
    void testGetCompletedTasks() {
        Task task1 = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        task1.setCompletionTime(10.0);

        Task task2 = new Task(TaskType.GPU, UserType.PERSONAL_VIP, 0.0);
        task2.setCompletionTime(15.0);

        results.recordCompletion(task1);
        results.recordCompletion(task2);

        List<Task> completedTasks = results.getCompletedTasks();

        assertEquals(2, completedTasks.size(), "Should have 2 completed tasks");
        
        // Verify it's a copy (defensive programming)
        completedTasks.clear();
        assertEquals(2, results.getCompletedTasks().size(), 
            "Original list should not be affected");
    }

    @Test
    @DisplayName("Reset should clear all statistics")
    void testReset() {
        // Add some data
        results.recordArrival();
        results.recordArrival();

        Task task = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        task.setCompletionTime(10.0);
        results.recordCompletion(task);

        results.setTotalSimulationTime(100.0);

        // Verify data exists
        assertTrue(results.getTotalArrivedTasks() > 0);
        assertTrue(results.getTotalCompletedTasks() > 0);

        // Reset
        results.reset();

        // Verify everything is cleared
        assertEquals(0, results.getTotalArrivedTasks(), "Arrived tasks should be 0");
        assertEquals(0, results.getTotalCompletedTasks(), "Completed tasks should be 0");
        assertEquals(0, results.getCpuTasksCompleted(), "CPU tasks should be 0");
        assertEquals(0, results.getGpuTasksCompleted(), "GPU tasks should be 0");
        assertEquals(0, results.getNormalUserTasksCompleted(), "Normal user tasks should be 0");
        assertEquals(0, results.getPersonalVipTasksCompleted(), "Personal VIP tasks should be 0");
        assertEquals(0, results.getEnterpriseVipTasksCompleted(), "Enterprise VIP tasks should be 0");
        assertEquals(0.0, results.getAverageSystemTime(), 0.001, "Avg system time should be 0");
        assertEquals(0.0, results.getMaxSystemTime(), 0.001, "Max system time should be 0");
        assertEquals(0.0, results.getMinSystemTime(), 0.001, "Min system time should be 0");
        assertEquals(0.0, results.getTotalSimulationTime(), 0.001, "Total sim time should be 0");
        assertTrue(results.getCompletedTasks().isEmpty(), "Completed tasks list should be empty");
    }

    @Test
    @DisplayName("Should handle zero simulation time in throughput calculation")
    void testThroughputWithZeroTime() {
        Task task = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        task.setCompletionTime(10.0);
        results.recordCompletion(task);

        results.setTotalSimulationTime(0.0);

        assertEquals(0.0, results.getThroughput(), 0.001, 
            "Throughput should be 0 when simulation time is 0");
    }

    @Test
    @DisplayName("Should handle multiple task types and user types simultaneously")
    void testMixedTasksAndUsers() {
        Task[] tasks = {
            new Task(TaskType.CPU, UserType.NORMAL, 0.0),
            new Task(TaskType.CPU, UserType.PERSONAL_VIP, 0.0),
            new Task(TaskType.GPU, UserType.NORMAL, 0.0),
            new Task(TaskType.GPU, UserType.ENTERPRISE_VIP, 0.0),
            new Task(TaskType.CPU, UserType.ENTERPRISE_VIP, 0.0)
        };

        for (int i = 0; i < tasks.length; i++) {
            tasks[i].setCompletionTime(10.0 * (i + 1));
            results.recordCompletion(tasks[i]);
        }

        assertEquals(5, results.getTotalCompletedTasks(), "Should have 5 completed tasks");
        assertEquals(3, results.getCpuTasksCompleted(), "Should have 3 CPU tasks");
        assertEquals(2, results.getGpuTasksCompleted(), "Should have 2 GPU tasks");
        assertEquals(2, results.getNormalUserTasksCompleted(), "Should have 2 normal users");
        assertEquals(1, results.getPersonalVipTasksCompleted(), "Should have 1 personal VIP");
        assertEquals(2, results.getEnterpriseVipTasksCompleted(), "Should have 2 enterprise VIPs");
    }

    @Test
    @DisplayName("Average should handle single task correctly")
    void testAverageWithSingleTask() {
        Task task = new Task(TaskType.CPU, UserType.NORMAL, 5.0);
        task.setCompletionTime(20.0);

        results.recordCompletion(task);

        assertEquals(15.0, results.getAverageSystemTime(), 0.001, 
            "Average system time should equal the single task's system time");
    }

    @Test
    @DisplayName("Should handle large number of tasks")
    void testLargeNumberOfTasks() {
        for (int i = 0; i < 10000; i++) {
            results.recordArrival();
            
            if (i % 2 == 0) { // Complete every other task
                Task task = new Task(
                    i % 3 == 0 ? TaskType.CPU : TaskType.GPU,
                    UserType.values()[i % 3],
                    i * 1.0
                );
                task.setCompletionTime(i * 2.0);
                results.recordCompletion(task);
            }
        }

        assertEquals(10000, results.getTotalArrivedTasks(), "Should have 10000 arrivals");
        assertEquals(5000, results.getTotalCompletedTasks(), "Should have 5000 completions");
        assertTrue(results.getAverageSystemTime() > 0, "Average should be positive");
    }
}

