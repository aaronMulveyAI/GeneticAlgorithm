package org.example.Experiment;

import org.example.GA.Agents.Abilities.Crossover.*;
import org.example.GA.Agents.Abilities.Selection.*;
import org.example.GA.Agents.Abilities.iReproduction;
import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.Agents.Population;
import org.example.GA.GeneticAlgorithm;
import org.example.OptimizationProblems.Modelling.*;
import java.util.Locale;

public class ParameterTuning {
    private static final int GENERATIONS = 1000;
    private static final int NUM_RUNS = 10;

    public record BenchmarkResult(double averageBestFitness, double averageBestGeneration,
                                  double generationStandardDeviation) {}

    public static void main(String[] args) {
        AbstractProblem problem = new GuessNumberProblem();
        iSelection[] selections = {
                new RouletteSelection(), new TournamentSelection(10),
                new TruncationSelection(0.5), new BrindleSelection()
        };
        iReproduction[] crossovers = {
                new UniformCrossover(), new SinglePointCrossover(), new DoublePointCrossover()
        };
        for (iSelection selection : selections) {
            for (iReproduction crossover : crossovers) {
                System.out.println(selection.getClass().getSimpleName() + " / " + crossover.getClass().getSimpleName());
                testConvergence(problem, new GeneticAlgorithm(problem, selection, crossover));
            }
        }
    }

    public static BenchmarkResult benchmark(AbstractProblem problem, GeneticAlgorithm algorithm,
                                            int runs, int generations, int populationSize) {
        if (runs < 1 || generations < 0 || populationSize < 1 || algorithm.problem != problem) {
            throw new IllegalArgumentException("Invalid benchmark parameters");
        }
        double totalFitness = 0;
        double totalGeneration = 0;
        int[] bestGenerations = new int[runs];
        for (int run = 0; run < runs; run++) {
            Population population = new Population(problem, populationSize);
            double bestFitness = population.getFittestIndividual().getFitness();
            int bestGeneration = 0;
            for (int generation = 1; generation <= generations; generation++) {
                population = algorithm.evolve(population);
                double fitness = population.getFittestIndividual().getFitness();
                if (population.isBetter(fitness, bestFitness)) {
                    bestFitness = fitness;
                    bestGeneration = generation;
                }
            }
            bestGenerations[run] = bestGeneration;
            totalFitness += bestFitness;
            totalGeneration += bestGeneration;
        }
        double meanGeneration = totalGeneration / runs;
        double squaredDeviations = 0;
        for (int generation : bestGenerations) {
            squaredDeviations += Math.pow(generation - meanGeneration, 2);
        }
        double standardDeviation = runs == 1 ? 0 : Math.sqrt(squaredDeviations / (runs - 1));
        return new BenchmarkResult(totalFitness / runs, meanGeneration, standardDeviation);
    }

    public static void testConvergence(AbstractProblem problem, GeneticAlgorithm algorithm) {
        BenchmarkResult result = benchmark(problem, algorithm, NUM_RUNS, GENERATIONS, 100);
        System.out.printf(Locale.US, "Average Best Fitness: %.2f%n", result.averageBestFitness());
        System.out.printf(Locale.US, "Average Generation of First Best Fitness: %.2f%n", result.averageBestGeneration());
        System.out.printf(Locale.US, "Standard Deviation (Generations): %.2f%n", result.generationStandardDeviation());
    }

    public static void testMutation(AbstractProblem problem) {
        rateSweep(problem, true);
    }

    public static void testCrossover(AbstractProblem problem) {
        rateSweep(problem, false);
    }

    private static void rateSweep(AbstractProblem problem, boolean mutation) {
        StringBuilder parameters = new StringBuilder(mutation ? "mutationTuning <- c(" : "crossoverTuning <- c(");
        StringBuilder averages = new StringBuilder("averageBestGeneration <- c(");
        for (int step = 0; step <= 50; step++) {
            double rate = step / 50.0;
            GeneticAlgorithm algorithm = new GeneticAlgorithm(problem, new TournamentSelection(5),
                    new SinglePointCrossover(), mutation ? 0.5 : rate, mutation ? rate : 0.1);
            BenchmarkResult result = benchmark(problem, algorithm, NUM_RUNS, GENERATIONS, 100);
            appendValue(parameters, rate, step > 0);
            appendValue(averages, result.averageBestGeneration(), step > 0);
        }
        System.out.println(parameters.append(')'));
        System.out.println(averages.append(')'));
    }

    public static void testTournament(AbstractProblem problem) {
        StringBuilder sizes = new StringBuilder("tournamentSizes <- c(");
        StringBuilder averages = new StringBuilder("averageBestGeneration <- c(");
        for (int step = 1; step <= 20; step++) {
            int size = step * 3;
            GeneticAlgorithm algorithm = new GeneticAlgorithm(problem, new TournamentSelection(size),
                    new SinglePointCrossover(), 0.5, 0.1);
            BenchmarkResult result = benchmark(problem, algorithm, NUM_RUNS, GENERATIONS, 100);
            appendValue(sizes, size, step > 1);
            appendValue(averages, result.averageBestGeneration(), step > 1);
        }
        System.out.println(sizes.append(')'));
        System.out.println(averages.append(')'));
    }

    private static void appendValue(StringBuilder output, double value, boolean separator) {
        if (separator) output.append(", ");
        output.append(String.format(Locale.US, "%.2f", value));
    }
}
