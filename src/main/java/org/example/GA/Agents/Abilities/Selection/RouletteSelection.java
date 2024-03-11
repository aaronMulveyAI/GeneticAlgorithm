package org.example.GA.Agents.Abilities.Selection;

import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.Agents.Individual;
import org.example.GA.Agents.Population;
import org.example.GA.GeneticAlgorithm;
import org.example.GA.OPTIMIZATION_TYPE;

import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.E;

public class RouletteSelection implements iSelection {

    private static final double T = 2;
    @Override
    public Individual selectIndividual(Population population) {
        Individual[] individuals = population.getIndividuals();
        double sumOfFitness;



        sumOfFitness = Arrays.stream(individuals).mapToDouble(Individual::getFitness).sum();

        double sum = 0;
        for (Individual individual : individuals) {
            sum += Math.pow(Math.E, individual.getFitness() / T);
        }


        double[] probabilities = new double[individuals.length];

        for (int i = 0; i < individuals.length; i++) {

            double fitness = Math.pow(Math.E, individuals[i].getFitness() / T);

            if(GeneticAlgorithm.optimizationType == OPTIMIZATION_TYPE.MAXIMIZE) {
                probabilities[i] = fitness / sumOfFitness;
            } else {
                probabilities[i] = (1.0 / Math.max(fitness, 1e-6)) / sumOfFitness;
            }
        }


        double[] cumulativeProbabilities = new double[individuals.length];
        double cumulative = 0;
        for (int i = 0; i < individuals.length; i++) {
            cumulative += probabilities[i];
            cumulativeProbabilities[i] = cumulative;
        }

        int a = (int) cumulativeProbabilities.length -1;
        double rand = new Random().nextDouble(a);

        for (int i = 0; i < cumulativeProbabilities.length; i++) {
            if (rand < cumulativeProbabilities[i]) {
                return individuals[i];
            }
        }


        return population.getFittestIndividual();
    }
}
