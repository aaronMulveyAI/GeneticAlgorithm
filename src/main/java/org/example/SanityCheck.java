package org.example;

import org.example.GA.Agents.Abilities.Crossover.*;
import org.example.GA.Agents.Abilities.Selection.*;
import org.example.GA.Agents.Abilities.iReproduction;
import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.GeneticAlgorithm;
import org.example.GA.Agents.Population;
import org.example.OptimizationProblems.AbstractProblem;
import org.example.OptimizationProblems.KnapsackProblem;
import org.example.OptimizationProblems.TravelingSalesmanProblem;

public class SanityCheck {
    public static void main(String[] args) {

        // Configuración del problema de la mochila
        int[] weights = {2, 3, 4, 5};
        int[] values = {3, 4, 5, 6};
        int maxWeight = 5;

        KnapsackProblem knapsack = KnapsackProblem.generateRandom(20);

        // Configuración del problema del vendedor viajero (TSP)

        double[][] distances = {
                {0, 10, 15, 20},
                {10, 0, 35, 25},
                {15, 35, 0, 30},
                {20, 25, 30, 0}
        };

        AbstractProblem tsp = new TravelingSalesmanProblem(distances);

        // Selección y reproducción
        iSelection selectionTournament = new TournamentSelection();
        iReproduction singlePoint = new SinglePointCrossover();
        iReproduction uniform = new UniformCrossover();
        iReproduction doublePoint = new DoublePointCrossover();

        // Ejecutar el GA para el problema de la mochila
        GeneticAlgorithm gaKnapsack = new GeneticAlgorithm(knapsack, selectionTournament, uniform);
        Population populationKnapsack = new Population(knapsack, 10);
        runGeneticAlgorithm(gaKnapsack, populationKnapsack, "Knapsack Problem");

        // Ejecutar el GA para el TSP
        GeneticAlgorithm gaTSP = new GeneticAlgorithm(tsp, selectionTournament, singlePoint); // Considera ajustar el método de cruzamiento para el TSP
        Population populationTSP = new Population(tsp, 10);
        runGeneticAlgorithm(gaTSP, populationTSP, "TSP");
    }

    private static void runGeneticAlgorithm(GeneticAlgorithm geneticAlgorithm, Population population, String problemName) {
        int generationNumber = 0;
        System.out.println("Starting " + problemName);
        do {
            generationNumber++;
            System.out.println("Generation: " + generationNumber + " Fittest: " + population.getFittestIndividual().calculateFitness());
            population = geneticAlgorithm.evolve(population);
        } while (generationNumber < 50);
        System.out.println("\nSolution found in generation " + generationNumber + " for " + problemName + " solution: " + population.getFittestIndividual());
        System.out.println("--------------------------------------------------");
    }
}
