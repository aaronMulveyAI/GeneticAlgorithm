package org.example.GA;

import java.util.Random;

public class Constants {

    private Constants() {}
    public static final Random RANDOM = new Random();
    public static double CROSSOVER_RATE = 0.5;
    public static double MUTATION_RATE = 0.15;
    public static int TOURNAMENT_SELECTION_SIZE = 5;

}
