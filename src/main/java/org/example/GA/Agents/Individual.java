package org.example.GA.Agents;

import org.example.OptimizationProblems.Modelling.AbstractProblem;
import java.util.Arrays;

public class Individual {
    public int[] genes;
    private double fitness = 0;
    private AbstractProblem problem;

    public Individual(AbstractProblem problem){
        this.problem = problem;
        this.genes = new int[problem.getModelSize()];
        initializeGenes();
    }

    /**
     * Initialize genes with random values
     */
    private void initializeGenes(){

        this.genes = problem.sampleSolution();
        this.fitness = problem.solve(genes);
    }

    /**
     * Calculate fitness of individual
     * @return fitness of individual
     */
    public double calculateFitness(){
        this.fitness = problem.solve(genes);
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
    public double getFitness() {return problem.solve(genes);}
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
