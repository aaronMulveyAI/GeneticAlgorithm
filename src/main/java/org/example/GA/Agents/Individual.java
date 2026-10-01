package org.example.GA.Agents;

import org.example.OptimizationProblems.Modelling.AbstractProblem;
import java.util.Arrays;
import java.util.Objects;

public class Individual {
    private int[] genes;
    private double fitness;
    private boolean fitnessValid;
    private final AbstractProblem problem;

    public Individual(AbstractProblem problem) {
        this(problem, problem.sampleSolution());
    }

    public Individual(AbstractProblem problem, int[] genes) {
        this.problem = Objects.requireNonNull(problem);
        setGenes(genes);
    }

    public double calculateFitness() { return getFitness(); }

    public void setGene(int position, int gene) {
        if (gene < 0 || gene >= problem.getGeneValueCount()) {
            throw new IllegalArgumentException("Gene outside the problem domain");
        }
        genes[position] = gene;
        fitnessValid = false;
    }

    public void setGenes(int[] genes) {
        problem.validateSolution(genes);
        this.genes = genes.clone();
        fitnessValid = false;
    }

    public double getFitness() {
        if (!fitnessValid) {
            problem.validateSolution(genes);
            fitness = problem.solve(genes);
            if (!Double.isFinite(fitness)) {
                throw new IllegalStateException("Fitness must be finite");
            }
            fitnessValid = true;
        }
        return fitness;
    }

    public int[] getGenes() { return genes.clone(); }
    public AbstractProblem getProblem() { return problem; }
    public int getGene(int index) { return genes[index]; }
    public Individual copy() { return new Individual(problem, genes); }

    @Override
    public String toString() {
        return "Individual {genes=" + Arrays.toString(genes) + ", fitness=" + getFitness() + '}';
    }
}
