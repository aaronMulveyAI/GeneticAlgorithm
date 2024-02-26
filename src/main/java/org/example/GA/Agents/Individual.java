package org.example.GA.Agents;

import org.example.GA.Constants;

import java.util.Arrays;

import static org.example.GA.Constants.GENES_BASE;
import static org.example.GA.Constants.RANDOM;

public class Individual {
    private int[] genes;
    private int[] weight;
    private int[] value;
    private int fitness = 0;

    public Individual(int[] weight, int[] value){
        this.weight = weight;
        this.value = value;
        this.genes = new int[value.length];
        initializeGenes();
    }

    /**
     * Initialize genes with random values
     */
    private void initializeGenes(){
        for (int i = 0; i < value.length; i++){
            genes[i] = RANDOM.nextInt(2);
        }
    }

    /**
     * Calculate fitness of individual
     * @return fitness of individual
     */
    public int calculateFitness(){
        int weight = 0;
        int value = 0;
        for (int i = 0; i < genes.length; i++) {
            if(genes[i] == 1){ // lo cojo?
                weight += this.weight[i];
                value += this.value[i];
            }
        }
        if(weight <= Constants.KNAPSACK_CAPACITY){
            return value;
        }
        return Integer.MIN_VALUE;
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
    public int getFitness() {return fitness;}
    public int[] getGenes() {
        return genes;
    }
    @Override
    public String toString() {
        return "Individual {" +
                "genes=" + Arrays.toString(genes) +
                ", fitness=" + fitness +
                '}';
    }
}
