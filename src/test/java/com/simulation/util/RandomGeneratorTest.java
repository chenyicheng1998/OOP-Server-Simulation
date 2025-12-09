package com.simulation.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RandomGenerator class
 */
@DisplayName("RandomGenerator Tests")
class RandomGeneratorTest {

    @Test
    @DisplayName("Exponential distribution should return positive values")
    void testExponentialPositive() {
        for (int i = 0; i < 1000; i++) {
            double value = RandomGenerator.exponential(5.0);
            assertTrue(value >= 0, "Exponential values should be non-negative");
        }
    }

    @Test
    @DisplayName("Exponential distribution mean should be approximately correct")
    void testExponentialMean() {
        double mean = 10.0;
        int samples = 10000;
        double sum = 0;

        for (int i = 0; i < samples; i++) {
            sum += RandomGenerator.exponential(mean);
        }

        double actualMean = sum / samples;
        
        // Mean should be within 10% of expected (with large sample size)
        assertEquals(mean, actualMean, mean * 0.1, 
            "Exponential mean should be approximately " + mean);
    }

    @Test
    @DisplayName("Uniform distribution should return values in range")
    void testUniformRange() {
        double min = 5.0;
        double max = 15.0;

        for (int i = 0; i < 1000; i++) {
            double value = RandomGenerator.uniform(min, max);
            assertTrue(value >= min && value <= max, 
                "Uniform values should be in range [" + min + ", " + max + "]");
        }
    }

    @Test
    @DisplayName("Uniform distribution mean should be approximately correct")
    void testUniformMean() {
        double min = 0.0;
        double max = 10.0;
        double expectedMean = (min + max) / 2.0;
        int samples = 10000;
        double sum = 0;

        for (int i = 0; i < samples; i++) {
            sum += RandomGenerator.uniform(min, max);
        }

        double actualMean = sum / samples;
        
        assertEquals(expectedMean, actualMean, 0.2, 
            "Uniform mean should be approximately " + expectedMean);
    }

    @Test
    @DisplayName("Bernoulli should respect probability")
    void testBernoulliProbability() {
        double probability = 0.7;
        int samples = 10000;
        int trueCount = 0;

        for (int i = 0; i < samples; i++) {
            if (RandomGenerator.bernoulli(probability)) {
                trueCount++;
            }
        }

        double actualProbability = (double) trueCount / samples;
        
        // Actual probability should be within 5% of expected
        assertEquals(probability, actualProbability, 0.05, 
            "Bernoulli probability should be approximately " + probability);
    }

    @Test
    @DisplayName("Bernoulli with probability 0 should always return false")
    void testBernoulliZero() {
        for (int i = 0; i < 1000; i++) {
            assertFalse(RandomGenerator.bernoulli(0.0), 
                "Bernoulli(0) should always return false");
        }
    }

    @Test
    @DisplayName("Bernoulli with probability 1 should always return true")
    void testBernoulliOne() {
        for (int i = 0; i < 1000; i++) {
            assertTrue(RandomGenerator.bernoulli(1.0), 
                "Bernoulli(1) should always return true");
        }
    }

    @Test
    @DisplayName("UniformInt should return values in range")
    void testUniformIntRange() {
        int min = 1;
        int max = 10;

        for (int i = 0; i < 1000; i++) {
            int value = RandomGenerator.uniformInt(min, max);
            assertTrue(value >= min && value <= max, 
                "UniformInt values should be in range [" + min + ", " + max + "]");
        }
    }

    @Test
    @DisplayName("UniformInt should generate all values in range")
    void testUniformIntCoverage() {
        int min = 1;
        int max = 5;
        boolean[] seen = new boolean[max - min + 1];

        // Run enough times to likely see all values
        for (int i = 0; i < 10000; i++) {
            int value = RandomGenerator.uniformInt(min, max);
            seen[value - min] = true;
        }

        // Check that all values were seen at least once
        for (int i = 0; i < seen.length; i++) {
            assertTrue(seen[i], "Value " + (min + i) + " should appear at least once");
        }
    }

    @Test
    @DisplayName("SetSeed should make random sequence reproducible")
    void testSetSeed() {
        long seed = 12345L;
        
        // Generate first sequence
        RandomGenerator.setSeed(seed);
        double[] sequence1 = new double[10];
        for (int i = 0; i < 10; i++) {
            sequence1[i] = RandomGenerator.exponential(5.0);
        }

        // Generate second sequence with same seed
        RandomGenerator.setSeed(seed);
        double[] sequence2 = new double[10];
        for (int i = 0; i < 10; i++) {
            sequence2[i] = RandomGenerator.exponential(5.0);
        }

        // Sequences should be identical
        assertArrayEquals(sequence1, sequence2, 
            "Sequences with same seed should be identical");
    }

    @Test
    @DisplayName("Different seeds should produce different sequences")
    void testDifferentSeeds() {
        RandomGenerator.setSeed(111L);
        double value1 = RandomGenerator.exponential(5.0);

        RandomGenerator.setSeed(222L);
        double value2 = RandomGenerator.exponential(5.0);

        assertNotEquals(value1, value2, 
            "Different seeds should produce different values");
    }

    @RepeatedTest(5)
    @DisplayName("Exponential with different means should have different distributions")
    void testExponentialDifferentMeans() {
        int samples = 5000;
        
        double sum1 = 0;
        for (int i = 0; i < samples; i++) {
            sum1 += RandomGenerator.exponential(5.0);
        }
        double mean1 = sum1 / samples;

        double sum2 = 0;
        for (int i = 0; i < samples; i++) {
            sum2 += RandomGenerator.exponential(10.0);
        }
        double mean2 = sum2 / samples;

        assertTrue(mean2 > mean1, 
            "Exponential(10) should have larger mean than Exponential(5)");
    }

    @Test
    @DisplayName("Uniform should handle min equals max")
    void testUniformSingleValue() {
        double value = 7.0;
        
        for (int i = 0; i < 100; i++) {
            double result = RandomGenerator.uniform(value, value);
            assertEquals(value, result, 0.001, 
                "Uniform(x, x) should always return x");
        }
    }

    @Test
    @DisplayName("UniformInt with same min and max should return that value")
    void testUniformIntSingleValue() {
        int value = 5;
        
        for (int i = 0; i < 100; i++) {
            int result = RandomGenerator.uniformInt(value, value);
            assertEquals(value, result, 
                "UniformInt(x, x) should always return x");
        }
    }

    @Test
    @DisplayName("Exponential variance should be approximately mean squared")
    void testExponentialVariance() {
        double mean = 10.0;
        int samples = 10000;
        double sum = 0;
        double sumSquares = 0;

        for (int i = 0; i < samples; i++) {
            double value = RandomGenerator.exponential(mean);
            sum += value;
            sumSquares += value * value;
        }

        double actualMean = sum / samples;
        double variance = (sumSquares / samples) - (actualMean * actualMean);
        double expectedVariance = mean * mean;

        // Variance should be approximately mean^2 for exponential distribution
        assertEquals(expectedVariance, variance, expectedVariance * 0.15, 
            "Exponential variance should be approximately mean²");
    }

    @Test
    @DisplayName("Uniform distribution should be reasonably uniform")
    void testUniformDistribution() {
        int bins = 10;
        int[] counts = new int[bins];
        int samples = 10000;
        double min = 0.0;
        double max = 10.0;
        double binWidth = (max - min) / bins;

        for (int i = 0; i < samples; i++) {
            double value = RandomGenerator.uniform(min, max);
            int binIndex = (int) (value / binWidth);
            if (binIndex >= bins) binIndex = bins - 1; // Handle edge case
            counts[binIndex]++;
        }

        int expectedPerBin = samples / bins;
        
        // Each bin should have roughly equal counts (within 20%)
        for (int i = 0; i < bins; i++) {
            assertTrue(Math.abs(counts[i] - expectedPerBin) < expectedPerBin * 0.2, 
                "Bin " + i + " should have approximately " + expectedPerBin + " samples");
        }
    }
}


