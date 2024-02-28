package org.example.OptimizationProblems;

public abstract class AbstractProblem{
    public String name;
    public OptimizationMethod optimizationMethod;
    public int modelSize;

    public int genesBound;

    public AbstractProblem(String name, OptimizationMethod optimizationMethod, int modelSize, int genesBound) {
        this.name = name;
        this.optimizationMethod = optimizationMethod;
        this.modelSize = modelSize;
        this.genesBound = genesBound;
    }
    public abstract int[] sampleSolution();
    public abstract double solve(int[] solution);

}
