package org.example.Experiment;

import org.example.GA.Agents.Abilities.*;
import org.example.GA.Agents.Abilities.Crossover.DoublePointCrossover;
import org.example.GA.Agents.Abilities.Crossover.SinglePointCrossover;
import org.example.GA.Agents.Abilities.Crossover.UniformCrossover;
import org.example.GA.Agents.Population;
import org.example.GA.Constants;
import org.example.GA.GeneticAlgorithm;
import org.example.GA.OPTIMIZATION_TYPE;
import org.example.OptimizationProblems.Modelling.*;
import org.example.GA.Agents.Abilities.Selection.TournamentSelection;
import org.example.GA.Agents.Abilities.Selection.RouletteSelection;
import org.example.GA.Agents.Abilities.Selection.TruncationSelection;
import org.example.GA.Agents.Abilities.Selection.BrindleSelection;

import javax.swing.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static java.lang.Math.round;
import static java.util.Arrays.stream;

public class ParameterTuning {

    private static GeneticAlgorithm ga;
    private static Population population;

    public static void main(String[] args) {


        AbstractProblem[] problems = new AbstractProblem[] {
                new TravelingSalesmanProblem(),
                new KnapsackProblem(),
                new NQueensProblem(),
                new GuessNumberProblem(),
                new RealValueOptimizationProblem(),
                new CircularTSProblem()
        };

        //testTournament(problems[2]);


       AbstractProblem p = problems[3];
        ga = new GeneticAlgorithm(p, new RouletteSelection(), new UniformCrossover());
        testConvergence(p, ga);
        ga = new GeneticAlgorithm(p, new RouletteSelection(), new SinglePointCrossover());
        testConvergence(p, ga);
        ga = new GeneticAlgorithm(p, new RouletteSelection(), new DoublePointCrossover());
        testConvergence(p, ga);

        ga = new GeneticAlgorithm(p, new TournamentSelection(10), new UniformCrossover());
        testConvergence(p, ga);
        ga = new GeneticAlgorithm(p, new TournamentSelection(10), new SinglePointCrossover());
        testConvergence(p, ga);
        ga = new GeneticAlgorithm(p, new TournamentSelection(10), new DoublePointCrossover());
        testConvergence(p, ga);


        ga = new GeneticAlgorithm(p, new TruncationSelection(0.5), new UniformCrossover());
        testConvergence(p, ga);
        ga = new GeneticAlgorithm(p, new TruncationSelection(0.5), new SinglePointCrossover());
        testConvergence(p, ga);
        ga = new GeneticAlgorithm(p, new TruncationSelection(0.5), new DoublePointCrossover());
        testConvergence(p, ga);

    }

    public static void testConvergence(AbstractProblem problem, GeneticAlgorithm ga) {
        System.out.println(1);

        final int GENERATIONS = 1000;
        final int NUM_RUNS = 10; // Number of times to run the test with each crossover rate
        double totalFitness = 0.0;  // Accumulate fitness across all runs
        double totalSquaredDeviations = 0.0;  // Accumulate squared deviations for standard deviation (generations)
        int bestGenerations = 0;  // Store generation of best fitness for each run

        int[] convergenceGenerations = new int[NUM_RUNS];

        DecimalFormat df = new DecimalFormat("#.##");
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(new Locale("en", "US")));

        for (int run = 0; run < NUM_RUNS; run++) {
            ga = new GeneticAlgorithm(problem, ga.selectionMethod, ga.reproductionMethod);
            population = new Population(problem, 100);

            double maxFitness = (GeneticAlgorithm.optimizationType == OPTIMIZATION_TYPE.MAXIMIZE) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
            int maxFitFoundInGeneration = 0;

            for (int i = 0; i < GENERATIONS; i++) {
                population = ga.evolve(population);
                double fitness = population.getFittestIndividual().getFitness();
                switch (GeneticAlgorithm.optimizationType) {
                    case MAXIMIZE:
                        if (fitness > maxFitness) {
                            maxFitness = fitness;
                            maxFitFoundInGeneration = i;
                        }
                        break;
                    case MINIMIZE:
                        if (fitness < maxFitness) {
                            maxFitness = fitness;
                            maxFitFoundInGeneration = i;
                        }
                        break;

                }

            }

            convergenceGenerations[run] = maxFitFoundInGeneration;
            totalFitness += maxFitness;  // Accumulate fitness for this run
            bestGenerations += maxFitFoundInGeneration;  // Store generation of best fitness
            System.out.println(maxFitFoundInGeneration);
        }

        double meanGeneration = stream(convergenceGenerations).average().getAsDouble();

        double variance = 0;

        for (int i = 0; i < convergenceGenerations.length; i++) {
            variance += (convergenceGenerations[i] - meanGeneration) * (convergenceGenerations[i] - meanGeneration);
        }
        variance = variance / (NUM_RUNS - 1);

        double SD = Math.sqrt(variance);  // Calculate deviation from average generation

        double deviation = bestGenerations - ((double) bestGenerations / NUM_RUNS);  // Calculate deviation from average generation
        totalSquaredDeviations += deviation * deviation;  // Accumulate squared deviation

        // Calculate average fitness and standard deviation of generations
        double averageGeneration = totalFitness / NUM_RUNS;
        double standardDeviationGeneration = Math.sqrt(totalSquaredDeviations / (NUM_RUNS - 1));

        // Print results
        System.out.println("Average Fitness: " + df.format(meanGeneration));
        System.out.println("Standard Deviation (Generations): " + df.format(SD));

    }



    public static void testMutation(AbstractProblem problem) {

        final int GENERATIONS = 1000;
        final int NUM_RUNS = 10; // Number of times to run the test with each crossover rate

        StringBuilder mutation = new StringBuilder("mutationTuning <- c(");  // Initialize output string
        StringBuilder avgMutation = new StringBuilder("avgMutation <- c(");
        Constants.CROSSOVER_RATE = 0;
        DecimalFormat df = new DecimalFormat("#.##");
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(new Locale("en", "US")));

        for (int x = 0; x < 50; x++) {
            Constants.CROSSOVER_RATE += 0.01;

            double totalFitness = 0.0;  // Reset total fitness for each crossover rate

            for (int run = 0; run < NUM_RUNS; run++) {
                ga = new GeneticAlgorithm(problem, new TournamentSelection(5), new SinglePointCrossover());
                population = new Population(problem, 100);

                double maxFitness = (GeneticAlgorithm.optimizationType == OPTIMIZATION_TYPE.MAXIMIZE) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                int maxFitFoundInGeneration = 0;

                for (int i = 0; i < GENERATIONS; i++) {
                    population = ga.evolve(population);
                    double fitness = population.getFittestIndividual().getFitness();
                    switch (GeneticAlgorithm.optimizationType) {
                        case MAXIMIZE:
                            if (fitness > maxFitness) {
                                maxFitness = fitness;
                                maxFitFoundInGeneration = i;
                            }
                            break;
                        case MINIMIZE:
                            if (fitness < maxFitness) {
                                maxFitness = fitness;
                                maxFitFoundInGeneration = i;
                            }
                            break;

                    }
                }

                totalFitness += maxFitFoundInGeneration;  // Accumulate fitness for this run
            }

            // Calculate average fitness and append to output string
            double averageFitness = totalFitness / NUM_RUNS;
            mutation.append(df.format(Constants.CROSSOVER_RATE)).append(", ");
            avgMutation.append(df.format(averageFitness)).append(", ");

            System.out.println(mutation);
            System.out.println(avgMutation);
        }

        mutation.append(")");  // Append closing bracket to output string
        avgMutation.append(")");
        System.out.println(mutation);  // Print output string
        System.out.println(avgMutation);  // Print output string
    }

    public static void testCrossover(AbstractProblem problem) {

        final int GENERATIONS = 1000;
        final int NUM_RUNS = 10; // Number of times to run the test with each crossover rate

        StringBuilder mutation = new StringBuilder("mutationTuning <- c(");  // Initialize output string
        StringBuilder avgMutation = new StringBuilder("avgMutation <- c(");
        Constants.CROSSOVER_RATE = 0;
        DecimalFormat df = new DecimalFormat("#.##");
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(new Locale("en", "US")));

        for (int x = 0; x < 50; x++) {
            Constants.CROSSOVER_RATE += 0.2;

            double totalFitness = 0.0;  // Reset total fitness for each crossover rate

            for (int run = 0; run < NUM_RUNS; run++) {
                ga = new GeneticAlgorithm(problem, new TournamentSelection(5), new SinglePointCrossover());
                population = new Population(problem, 100);

                double maxFitness = (GeneticAlgorithm.optimizationType == OPTIMIZATION_TYPE.MAXIMIZE) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                int maxFitFoundInGeneration = 0;

                for (int i = 0; i < GENERATIONS; i++) {
                    population = ga.evolve(population);
                    double fitness = population.getFittestIndividual().getFitness();
                    switch (GeneticAlgorithm.optimizationType) {
                        case MAXIMIZE:
                            if (fitness > maxFitness) {
                                maxFitness = fitness;
                                maxFitFoundInGeneration = i;
                            }
                            break;
                        case MINIMIZE:
                            if (fitness < maxFitness) {
                                maxFitness = fitness;
                                maxFitFoundInGeneration = i;
                            }
                            break;

                    }
                }

                totalFitness += maxFitFoundInGeneration;  // Accumulate fitness for this run
            }

            // Calculate average fitness and append to output string
            double averageFitness = totalFitness / NUM_RUNS;
            mutation.append(df.format(Constants.CROSSOVER_RATE)).append(", ");
            avgMutation.append(df.format(averageFitness)).append(", ");

            System.out.println(mutation);
            System.out.println(avgMutation);
        }

        mutation.append(")");  // Append closing bracket to output string
        avgMutation.append(")");
        System.out.println(mutation);  // Print output string
        System.out.println(avgMutation);  // Print output string
    }

    public static void testTournament(AbstractProblem problem) {

        final int GENERATIONS = 1000;
        final int NUM_RUNS = 10; // Number of times to run the test with each crossover rate

        StringBuilder mutation = new StringBuilder("mutationTuning <- c(");  // Initialize output string
        StringBuilder avgMutation = new StringBuilder("avgMutation <- c(");
        Constants.CROSSOVER_RATE = 0.5;
        DecimalFormat df = new DecimalFormat("#.##");
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(new Locale("en", "US")));
        int n = 0;
        for (int x = 0; x < 20; x++) {
            n+= 3;

            double totalFitness = 0.0;  // Reset total fitness for each crossover rate

            for (int run = 0; run < NUM_RUNS; run++) {
                ga = new GeneticAlgorithm(problem, new TournamentSelection(n), new SinglePointCrossover());
                population = new Population(problem, 100);

                double maxFitness = (GeneticAlgorithm.optimizationType == OPTIMIZATION_TYPE.MAXIMIZE) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                int maxFitFoundInGeneration = 0;

                for (int i = 0; i < GENERATIONS; i++) {
                    population = ga.evolve(population);
                    double fitness = population.getFittestIndividual().getFitness();
                    switch (GeneticAlgorithm.optimizationType) {
                        case MAXIMIZE:
                            if (fitness > maxFitness) {
                                maxFitness = fitness;
                                maxFitFoundInGeneration = i;
                            }
                            break;
                        case MINIMIZE:
                            if (fitness < maxFitness) {
                                maxFitness = fitness;
                                maxFitFoundInGeneration = i;
                            }
                            break;

                    }
                }

                totalFitness += maxFitFoundInGeneration;  // Accumulate fitness for this run
            }

            // Calculate average fitness and append to output string
            double averageFitness = totalFitness / NUM_RUNS;
            mutation.append(df.format(n)).append(", ");
            avgMutation.append(df.format(averageFitness)).append(", ");

            System.out.println(mutation);
            System.out.println(avgMutation);
        }

        mutation.append(")");  // Append closing bracket to output string
        avgMutation.append(")");
        System.out.println(mutation);  // Print output string
        System.out.println(avgMutation);  // Print output string
    }
}
