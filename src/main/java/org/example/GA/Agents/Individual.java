package org.example.GA.Agents;

import org.example.OptimizationProblems.AbstractProblem;
import java.util.Arrays;

public class Individual {
    private int[] genes;
    private double fitness = 0;
    private AbstractProblem problem;

    public Individual(AbstractProblem problem){
        this.problem = problem;
        this.genes = new int[problem.modelSize];
        initializeGenes();
    }

    /**
     * Initialize genes with random values
     */
    private void initializeGenes(){
        this.genes = problem.sampleSolution();
    }

    /**
     * Calculate fitness of individual
     * @return fitness of individual
     */
    public double calculateFitness(){
        return problem.solve(genes);
    }

    /**
     * Set gene
     * @param position position to set gene
     * @param gene gene to set
     */
    public void setGene(int position, int gene) {
        genes[position] = gene;
    }
    public void setGenes(int[] genes) {
        this.genes = genes;
    }
    public double getFitness() {return fitness;}
    public int[] getGenes() {
        return genes;
    }
    public AbstractProblem getProblem() {
        return problem;
    }

    public int getGene(int index){return genes[index];}

    @Override
    public String toString() {
        return "Individual {" +
                "genes=" + Arrays.toString(genes) +
                ", fitness=" + fitness +
                '}';
    }
}
