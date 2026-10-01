package org.example.OptimizationProblems.Modelling;

import org.example.OptimizationProblems.OptimizationMethod;
import org.example.OptimizationProblems.VisualModelling.AbstractVisualization;
import org.example.GA.OPTIMIZATION_TYPE;

public abstract class AbstractProblem {
    private final AbstractVisualization visualization;
    private final String name;
    private final OptimizationMethod optimizationMethod;
    private final int modelSize;
    public AbstractProblem(AbstractVisualization visualization, String name, OptimizationMethod optimizationMethod, int modelSize) {
        if (modelSize < 1) {
            throw new IllegalArgumentException("Problem size must be positive");
        }
        this.visualization = visualization;
        this.name = name;
        this.optimizationMethod = optimizationMethod;
        this.modelSize = modelSize;

    }
    public abstract int[] sampleSolution();
    public abstract double solve(int[] solution);
    public abstract AbstractProblem generateRandom(int n);
    public OPTIMIZATION_TYPE getOptimizationType() {
        return OPTIMIZATION_TYPE.MAXIMIZE;
    }

    public int getGeneValueCount() {
        return modelSize;
    }

    public void validateSolution(int[] solution) {
        if (solution == null || solution.length != modelSize) {
            throw new IllegalArgumentException("Chromosome length must match problem size");
        }
        boolean[] seen = optimizationMethod == OptimizationMethod.PERMUTATION ? new boolean[modelSize] : null;
        for (int gene : solution) {
            if (gene < 0 || gene >= getGeneValueCount()) {
                throw new IllegalArgumentException("Gene outside the problem domain");
            }
            if (seen != null) {
                if (seen[gene]) {
                    throw new IllegalArgumentException("Permutation must contain each gene exactly once");
                }
                seen[gene] = true;
            }
        }
    }
    public AbstractVisualization getVisualization() {
        return visualization;
    }
    public String getName() {
        return name;
    }
    public OptimizationMethod getOptimizationMethod() {
        return optimizationMethod;
    }
    public int getModelSize() {
        return modelSize;
    }
}
