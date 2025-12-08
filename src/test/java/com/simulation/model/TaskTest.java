package com.simulation.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Task class
 */
@DisplayName("Task Tests")
class TaskTest {

    @BeforeEach
    void setUp() {
        // Reset task ID counter by creating and discarding tasks
        // Note: In production, you might want a resetIdCounter() method
    }

    @Test
    @DisplayName("Task should be created with correct attributes")
    void testTaskCreation() {
        Task task = new Task(TaskType.CPU, UserType.NORMAL, 5.0);

        assertEquals(TaskType.CPU, task.getTaskType(), "Task type should be CPU");
        assertEquals(UserType.NORMAL, task.getUserType(), "User type should be NORMAL");
        assertEquals(5.0, task.getArrivalTime(), 0.001, "Arrival time should be 5.0");
        assertTrue(task.getId() > 0, "Task ID should be positive");
    }

    @Test
    @DisplayName("Each task should have unique ID")
    void testUniqueIds() {
        Task task1 = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        Task task2 = new Task(TaskType.GPU, UserType.PERSONAL_VIP, 0.0);
        Task task3 = new Task(TaskType.CPU, UserType.ENTERPRISE_VIP, 0.0);

        assertNotEquals(task1.getId(), task2.getId(), "Tasks should have different IDs");
        assertNotEquals(task2.getId(), task3.getId(), "Tasks should have different IDs");
        assertNotEquals(task1.getId(), task3.getId(), "Tasks should have different IDs");
    }

    @Test
    @DisplayName("Task should calculate system time correctly")
    void testSystemTime() {
        Task task = new Task(TaskType.CPU, UserType.NORMAL, 10.0);
        task.setCompletionTime(25.0);

        double systemTime = task.getTotalSystemTime();
        assertEquals(15.0, systemTime, 0.001, "System time should be 25.0 - 10.0 = 15.0");
    }

    @Test
    @DisplayName("Enterprise VIP should have higher priority than Personal VIP")
    void testPriorityEnterpriseVsPersonal() {
        Task enterpriseTask = new Task(TaskType.CPU, UserType.ENTERPRISE_VIP, 0.0);
        Task personalTask = new Task(TaskType.CPU, UserType.PERSONAL_VIP, 0.0);

        int comparison = enterpriseTask.compareTo(personalTask);
        assertTrue(comparison < 0, "Enterprise VIP should have higher priority (negative comparison)");
    }

    @Test
    @DisplayName("Personal VIP should have higher priority than Normal")
    void testPriorityPersonalVsNormal() {
        Task personalTask = new Task(TaskType.CPU, UserType.PERSONAL_VIP, 0.0);
        Task normalTask = new Task(TaskType.CPU, UserType.NORMAL, 0.0);

        int comparison = personalTask.compareTo(normalTask);
        assertTrue(comparison < 0, "Personal VIP should have higher priority (negative comparison)");
    }

    @Test
    @DisplayName("Enterprise VIP should have higher priority than Normal")
    void testPriorityEnterpriseVsNormal() {
        Task enterpriseTask = new Task(TaskType.CPU, UserType.ENTERPRISE_VIP, 0.0);
        Task normalTask = new Task(TaskType.CPU, UserType.NORMAL, 0.0);

        int comparison = enterpriseTask.compareTo(normalTask);
        assertTrue(comparison < 0, "Enterprise VIP should have higher priority (negative comparison)");
    }

    @Test
    @DisplayName("Same priority tasks should be ordered by arrival time (FCFS)")
    void testFCFSForSamePriority() {
        Task earlierTask = new Task(TaskType.CPU, UserType.NORMAL, 5.0);
        Task laterTask = new Task(TaskType.CPU, UserType.NORMAL, 10.0);

        int comparison = earlierTask.compareTo(laterTask);
        assertTrue(comparison < 0, "Earlier arriving task should have higher priority");
    }

    @Test
    @DisplayName("Priority should override arrival time")
    void testPriorityOverridesArrivalTime() {
        // VIP arrives later but should still have higher priority
        Task normalEarly = new Task(TaskType.CPU, UserType.NORMAL, 1.0);
        Task vipLate = new Task(TaskType.CPU, UserType.ENTERPRISE_VIP, 100.0);

        int comparison = vipLate.compareTo(normalEarly);
        assertTrue(comparison < 0, "VIP should have higher priority despite later arrival");
    }

    @Test
    @DisplayName("Task comparison should be transitive")
    void testTransitivity() {
        Task enterprise = new Task(TaskType.CPU, UserType.ENTERPRISE_VIP, 0.0);
        Task personal = new Task(TaskType.CPU, UserType.PERSONAL_VIP, 0.0);
        Task normal = new Task(TaskType.CPU, UserType.NORMAL, 0.0);

        // If A < B and B < C, then A < C
        assertTrue(enterprise.compareTo(personal) < 0, "Enterprise < Personal");
        assertTrue(personal.compareTo(normal) < 0, "Personal < Normal");
        assertTrue(enterprise.compareTo(normal) < 0, "Enterprise < Normal (transitivity)");
    }

    @Test
    @DisplayName("Task should be equal to itself")
    void testReflexive() {
        Task task = new Task(TaskType.CPU, UserType.NORMAL, 0.0);
        assertEquals(0, task.compareTo(task), "Task should be equal to itself");
    }

    @Test
    @DisplayName("Comparison should be symmetric for same priority and time")
    void testSymmetric() {
        Task task1 = new Task(TaskType.CPU, UserType.NORMAL, 10.0);
        Task task2 = new Task(TaskType.CPU, UserType.NORMAL, 10.0);

        int comp1to2 = task1.compareTo(task2);
        int comp2to1 = task2.compareTo(task1);

        // They should be comparable in opposite directions
        assertEquals(-Integer.signum(comp1to2), Integer.signum(comp2to1),
            "Comparison should be symmetric");
    }

    @Test
    @DisplayName("Different task types with same priority should order by arrival time")
    void testDifferentTaskTypes() {
        Task cpuTask = new Task(TaskType.CPU, UserType.NORMAL, 5.0);
        Task gpuTask = new Task(TaskType.GPU, UserType.NORMAL, 10.0);

        int comparison = cpuTask.compareTo(gpuTask);
        assertTrue(comparison < 0, "Earlier task should have priority regardless of type");
    }

    @Test
    @DisplayName("Completion time should be settable")
    void testSetCompletionTime() {
        Task task = new Task(TaskType.CPU, UserType.NORMAL, 5.0);
        
        task.setCompletionTime(20.0);
        assertEquals(20.0, task.getCompletionTime(), 0.001, "Completion time should be 20.0");
        
        task.setCompletionTime(30.0);
        assertEquals(30.0, task.getCompletionTime(), 0.001, "Completion time should be updatable");
    }

    @Test
    @DisplayName("User type priorities should match expected values")
    void testUserTypePriorities() {
        assertEquals(1, UserType.NORMAL.getPriority(), "Normal priority should be 1");
        assertEquals(2, UserType.PERSONAL_VIP.getPriority(), "Personal VIP priority should be 2");
        assertEquals(3, UserType.ENTERPRISE_VIP.getPriority(), "Enterprise VIP priority should be 3");
    }
}

