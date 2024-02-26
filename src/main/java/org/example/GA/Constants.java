package org.example.GA;

import java.util.Random;

public class Constants {

    private Constants() {}

    public static final int GENES_BASE = 2;
    public static final Random RANDOM = new Random();

    public static final int KNAPSACK_CAPACITY = 10;
    public static final int[] SOLUTION_SEQUENCE = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static final int SIMULATION_LENGTH = 500;
    public static final double CROSSOVER_RATE = 0.5;
    public static final double MUTATION_RATE = 0.15;
    public static final int TOURNAMENT_SELECTION_SIZE = 5;
    public static final int MAX_FITNESS = 10;
    public static final int CHROMOSOME_LENGTH = 10;

}
