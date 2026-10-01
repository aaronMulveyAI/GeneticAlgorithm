package org.example.GA.Agents;

import org.example.OptimizationProblems.Modelling.AbstractProblem;
import org.example.GA.OPTIMIZATION_TYPE;
import java.util.Objects;

public class Population {
    private final Individual[] individuals;
    private final AbstractProblem problem;

    public Population(AbstractProblem problem, int populationSize) {
        this(problem, populationSize, true);
    }

    public Population(AbstractProblem problem, int populationSize, boolean initialize) {
        if (populationSize < 1) {
            throw new IllegalArgumentException("Population size must be positive");
        }
        this.problem = Objects.requireNonNull(problem);
        individuals = new Individual[populationSize];
        if (initialize) {
            for (int i = 0; i < populationSize; i++) {
                individuals[i] = new Individual(problem);
            }
        }
    }

    public void saveIndividual(int index, Individual individual) {
        if (Objects.requireNonNull(individual).getProblem() != problem) {
            throw new IllegalArgumentException("Individual belongs to another problem");
        }
        individuals[index] = individual;
    }

    public int size() { return individuals.length; }
    public AbstractProblem getProblem() { return problem; }
    public OPTIMIZATION_TYPE getOptimizationType() { return problem.getOptimizationType(); }

    public boolean isBetter(double candidate, double reference) {
        return getOptimizationType() == OPTIMIZATION_TYPE.MAXIMIZE ? candidate > reference : candidate < reference;
    }

    public Individual getFittestIndividual() { return getExtreme(true); }
    public Individual getLeastFitIndividual() { return getExtreme(false); }

    private Individual getExtreme(boolean best) {
        Individual result = getIndividual(0);
        double fitness = result.getFitness();
        for (int i = 1; i < size(); i++) {
            Individual candidate = getIndividual(i);
            double candidateFitness = candidate.getFitness();
            if (best ? isBetter(candidateFitness, fitness) : isBetter(fitness, candidateFitness)) {
                result = candidate;
                fitness = candidateFitness;
            }
        }
        return result;
    }

    public Individual getIndividual(int index) {
        return Objects.requireNonNull(individuals[index], "Population is not fully initialized");
    }

    public Individual[] getIndividuals() { return individuals.clone(); }
}
