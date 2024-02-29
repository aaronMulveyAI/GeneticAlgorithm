package org.example.GA.Agents.Abilities.Selection;

import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.Agents.Individual;
import org.example.GA.Agents.Population;
import org.example.GA.GeneticAlgorithm;
import org.example.GA.OPTIMIZATION_TYPE;

import java.util.ArrayList;
import java.util.List;

public class BrindleSelection implements iSelection {

    @Override
    public Individual selectIndividual(Population population) {
        Individual[] individuals = population.getIndividuals();
        double totalFitness = 0.0;


        for (Individual individual : individuals) {
            totalFitness += (GeneticAlgorithm.optimizationType == OPTIMIZATION_TYPE.MAXIMIZE) ? individual.getFitness() : 1.0 / Math.max(individual.getFitness(), 1e-6);
        }

        double[] expectedNumbers = new double[individuals.length];
        List<Individual> newPopulationList = new ArrayList<>();
        int totalIntParts = 0;


        for (int i = 0; i < individuals.length; i++) {
            double fitness = (GeneticAlgorithm.optimizationType == OPTIMIZATION_TYPE.MAXIMIZE) ? individuals[i].getFitness() : 1.0 / Math.max(individuals[i].getFitness(), 1e-6);
            double expectedNumber = (fitness / totalFitness) * population.size();
            int intPart = (int) expectedNumber;
            expectedNumbers[i] = expectedNumber - intPart;
            totalIntParts += intPart;


            for (int j = 0; j < intPart; j++) {
                newPopulationList.add(individuals[i]);
            }
        }


        int remainingSpots = population.size() - totalIntParts;
        while (remainingSpots > 0) {
            double rand = Math.random();
            for (int i = 0; i < individuals.length; i++) {
                if (rand < expectedNumbers[i]) {
                    newPopulationList.add(individuals[i]);
                    break;
                }
                rand -= expectedNumbers[i];
            }
            remainingSpots--;
        }


        Individual[] newPopulationArray = newPopulationList.toArray(new Individual[0]);
        return newPopulationArray[(int) (Math.random() * newPopulationArray.length)];
    }
}
