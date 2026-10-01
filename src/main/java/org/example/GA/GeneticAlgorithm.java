package org.example.GA;

import org.example.GA.Agents.Abilities.*;
import org.example.GA.Agents.*;
import org.example.OptimizationProblems.Modelling.AbstractProblem;
import java.util.Objects;
import static org.example.GA.Constants.*;

public class GeneticAlgorithm {
    public final OPTIMIZATION_TYPE optimizationType;
    public final AbstractProblem problem;
    public final iSelection selectionMethod;
    public final iReproduction reproductionMethod;
    private final double crossoverRate;
    private final double mutationRate;

    public GeneticAlgorithm(AbstractProblem problem, iSelection selectionMethod, iReproduction reproductionMethod) {
        this(problem, selectionMethod, reproductionMethod, CROSSOVER_RATE, MUTATION_RATE);
    }

    public GeneticAlgorithm(AbstractProblem problem, iSelection selectionMethod, iReproduction reproductionMethod,
                            double crossoverRate, double mutationRate) {
        validateRate(crossoverRate);
        validateRate(mutationRate);
        this.problem = Objects.requireNonNull(problem);
        this.selectionMethod = Objects.requireNonNull(selectionMethod);
        this.reproductionMethod = Objects.requireNonNull(reproductionMethod);
        this.optimizationType = problem.getOptimizationType();
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
    }

    private static void validateRate(double rate) {
        if (!Double.isFinite(rate) || rate < 0 || rate > 1) {
            throw new IllegalArgumentException("Rates must be between 0 and 1");
        }
    }

    public OPTIMIZATION_TYPE optimizationType(AbstractProblem problem) { return problem.getOptimizationType(); }

    public Population evolve(Population population) {
        if (population.getProblem() != problem) {
            throw new IllegalArgumentException("Population belongs to another problem");
        }
        Population next = new Population(problem, population.size(), false);
        for (int i = 0; i < population.size(); i++) {
            Individual father = selectionMethod.selectIndividual(population);
            Individual mother = selectionMethod.selectIndividual(population);
            Individual child = crossover(father, mother);
            mutate(child);
            next.saveIndividual(i, child);
        }
        return next;
    }

    public Individual crossover(Individual father, Individual mother) {
        if (father.getProblem() != problem || mother.getProblem() != problem) {
            throw new IllegalArgumentException("Parents belong to another problem");
        }
        return RANDOM.nextDouble() < crossoverRate ? reproductionMethod.crossover(father, mother) : father.copy();
    }

    private void mutate(Individual individual) {
        int[] genes = individual.getGenes();
        switch (problem.getOptimizationMethod()) {
            case COMBINATORIAL -> {
                int domain = problem.getGeneValueCount();
                for (int i = 0; i < genes.length; i++) {
                    if (domain > 1 && RANDOM.nextDouble() < mutationRate) {
                        int replacement = RANDOM.nextInt(domain - 1);
                        genes[i] = replacement >= genes[i] ? replacement + 1 : replacement;
                    }
                }
            }
            case PERMUTATION -> {
                if (genes.length > 1 && RANDOM.nextDouble() < mutationRate) {
                    int first = RANDOM.nextInt(genes.length);
                    int second = RANDOM.nextInt(genes.length - 1);
                    if (second >= first) second++;
                    int temp = genes[first];
                    genes[first] = genes[second];
                    genes[second] = temp;
                }
            }
        }
        individual.setGenes(genes);
    }
}
