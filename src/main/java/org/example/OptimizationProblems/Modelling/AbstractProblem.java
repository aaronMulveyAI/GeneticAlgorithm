package org.example.OptimizationProblems.Modelling;

import org.example.OptimizationProblems.OptimizationMethod;
import org.example.OptimizationProblems.VisualModelling.AbstractVisualization;

public abstract class AbstractProblem{

    private final AbstractVisualization visualization;
    private final String name;
    private final OptimizationMethod optimizationMethod;
    private final int modelSize;

    public AbstractProblem(AbstractVisualization visualization, String name, OptimizationMethod optimizationMethod, int modelSize) {
        this.visualization = visualization;
        this.name = name;
        this.optimizationMethod = optimizationMethod;
        this.modelSize = modelSize;

    }
    public abstract int[] sampleSolution();
    public abstract double solve(int[] solution);
    public abstract AbstractProblem generateRandom(int n);
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
