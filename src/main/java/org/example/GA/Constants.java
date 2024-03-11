package org.example.GA;

import java.util.Random;

public class Constants {

    public static final Random RANDOM = new Random();
    public static double CROSSOVER_RATE = 0.5;
    public static double MUTATION_RATE = 0.10;
    public static int TOURNAMENT_SELECTION_SIZE = 5;

    public Constants(double CROSSOVER_RATE, double MUTATION_RATE) {
        Constants.CROSSOVER_RATE = CROSSOVER_RATE;
        Constants.MUTATION_RATE = MUTATION_RATE;
    }



}
