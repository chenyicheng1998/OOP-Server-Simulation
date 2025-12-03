package com.simulation.util;

import java.util.Random;

/**
 * Utility class for generating random numbers from various distributions
 */
public class RandomGenerator {
    private static final Random random = new Random();

    /**
     * Generate a random number from exponential distribution
     * @param mean mean value
     * @return random value
     */
    public static double exponential(double mean) {
        return -mean * Math.log(1 - random.nextDouble());
    }

    /**
     * Generate a random number from uniform distribution
     * @param min minimum value
     * @param max maximum value
     * @return random value
     */
    public static double uniform(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    /**
     * Generate a random boolean with given probability
     * @param probability probability of true (0.0 to 1.0)
     * @return true or false
     */
    public static boolean bernoulli(double probability) {
        return random.nextDouble() < probability;
    }

    /**
     * Generate a random integer in range [min, max]
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return random integer
     */
    public static int uniformInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    /**
     * Set random seed for reproducibility
     * @param seed the seed value
     */
    public static void setSeed(long seed) {
        random.setSeed(seed);
    }
}

