package org.example;

import org.example.GA.GeneticAlgorithm;
import org.example.GA.Agents.Population;

import static org.example.GA.Constants.SIMULATION_LENGTH;

public class Main {
    public static void main(String[] args) {


        int[] weights = {5,7,9,2};
        int[] values = {10,13,19,4};

        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(weights, values);
        Population population = new Population(100, weights, values);

        int generationNumber = 0;

        do{
            generationNumber++;
            System.out.println("Generation: " + generationNumber + " Fittest: " + population.getFittestIndividual().calculateFitness());
            System.out.println(population.getFittestIndividual());
            population = geneticAlgorithm.evolve(population);
        } while (generationNumber < 10);

        System.out.println("\nSolution found in generation " + generationNumber + " solution: " + population.getFittestIndividual());

    }
}