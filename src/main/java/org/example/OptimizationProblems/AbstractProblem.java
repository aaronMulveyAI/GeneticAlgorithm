package org.example.OptimizationProblems;

import org.example.OptimizationProblems.Modelling.OptimizationMethod;

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
    public int[] sampleSolution(){
        return new int[modelSize];
    }
    public double solve(int[] solution){
        return 0;
    }

}
