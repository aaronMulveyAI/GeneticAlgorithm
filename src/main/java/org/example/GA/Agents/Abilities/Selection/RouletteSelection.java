package org.example.GA.Agents.Abilities.Selection;

import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.Agents.Individual;
import org.example.GA.Agents.Population;
import org.example.GA.GeneticAlgorithm;
import org.example.GA.OPTIMIZATION_TYPE;

import java.util.Arrays;

public class RouletteSelection implements iSelection {
    @Override
    public Individual selectIndividual(Population population) {
        Individual[] individuals = population.getIndividuals();
        double sumOfFitness;


        if(GeneticAlgorithm.optimizationType == OPTIMIZATION_TYPE.MAXIMIZE) {

            sumOfFitness = Arrays.stream(individuals).mapToDouble(Individual::getFitness).sum();
        } else {

            sumOfFitness = Arrays.stream(individuals).mapToDouble(ind -> 1.0 / Math.max(ind.getFitness(), 1e-6)).sum();
        }

        double[] probabilities = new double[individuals.length];
        for (int i = 0; i < individuals.length; i++) {
            double fitness = individuals[i].getFitness();
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

        double rand = Math.random();
        for (int i = 0; i < cumulativeProbabilities.length; i++) {
            if (rand < cumulativeProbabilities[i]) {
                return individuals[i];
            }
        }

        return individuals[individuals.length - 1];
    }
}
