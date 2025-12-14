package com.simulation.util;

import java.util.Random;

/**
 * Utility class for generating random numbers from various probability distributions.
 *
 * <p>This class provides static methods to generate random values from different
 * statistical distributions commonly used in discrete-event simulation:
 * <ul>
 *   <li><b>Exponential Distribution</b>: Used for inter-arrival times and service times</li>
 *   <li><b>Uniform Distribution</b>: Used for bounded random values</li>
 *   <li><b>Bernoulli Distribution</b>: Used for binary decisions (e.g., CPU vs GPU)</li>
 *   <li><b>Discrete Uniform</b>: Used for random integers in a range</li>
 * </ul>
 *
 * <p>All methods are thread-safe and use a shared {@link Random} instance.
 *
 * <p><b>Mathematical Formulas:</b>
 * <ul>
 *   <li>Exponential: X = -λ * ln(1 - U), where U ~ Uniform(0,1) and λ is the mean</li>
 *   <li>Uniform: X = min + (max - min) * U, where U ~ Uniform(0,1)</li>
 *   <li>Bernoulli: X = 1 if U &lt; p, else 0, where U ~ Uniform(0,1) and p is probability</li>
 * </ul>
 *
 * @author Cloud Simulation Team
 * @version 2.0
 * @see Random
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

