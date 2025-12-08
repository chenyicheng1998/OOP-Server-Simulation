package com.simulation.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Clock class
 */
@DisplayName("Clock Tests")
class ClockTest {

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.getInstance();
        clock.reset();
    }

    @Test
    @DisplayName("Clock should be a singleton")
    void testSingleton() {
        Clock clock1 = Clock.getInstance();
        Clock clock2 = Clock.getInstance();
        assertSame(clock1, clock2, "Clock instances should be the same");
    }

    @Test
    @DisplayName("Clock should initialize at time 0")
    void testInitialTime() {
        assertEquals(0.0, clock.getTime(), 0.001, "Initial time should be 0.0");
    }

    @Test
    @DisplayName("Clock should update time correctly")
    void testSetTime() {
        clock.setTime(10.5);
        assertEquals(10.5, clock.getTime(), 0.001, "Time should be 10.5");

        clock.setTime(25.8);
        assertEquals(25.8, clock.getTime(), 0.001, "Time should be 25.8");
    }

    @Test
    @DisplayName("Clock should reset to 0")
    void testReset() {
        clock.setTime(100.0);
        assertEquals(100.0, clock.getTime(), 0.001);

        clock.reset();
        assertEquals(0.0, clock.getTime(), 0.001, "Time should be 0.0 after reset");
    }

    @Test
    @DisplayName("Clock should handle negative time values")
    void testNegativeTime() {
        clock.setTime(-5.0);
        assertEquals(-5.0, clock.getTime(), 0.001, "Clock should accept negative values");
    }

    @Test
    @DisplayName("Clock should handle very large time values")
    void testLargeTime() {
        double largeTime = 1_000_000.0;
        clock.setTime(largeTime);
        assertEquals(largeTime, clock.getTime(), 0.001, "Clock should handle large values");
    }
}

