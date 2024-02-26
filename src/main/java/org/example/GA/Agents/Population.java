package org.example.GA.Agents;

import org.example.GA.Agents.Individual;

public class Population {
    private Individual[] individuals;
    private int[] weights;
    private int[] values;

    public Population(int populationSize, int[] weights, int[] values){
        individuals = new Individual[populationSize];
        this.weights = weights;
        this.values = values;
        initializePopulation();
    }

    /**
     * Initialize population
     */
    private void initializePopulation(){
        for (int i = 0; i < individuals.length; i++) {
            individuals[i] = new Individual(weights, values);
        }
    }

    /**
     * Save individual
     * @param index index of individual
     * @param individual individual to save
     */
    public void saveIndividual(int index, Individual individual){
        individuals[index] = individual;
    }

    /**
     * Get size of population
     * @return size of population
     */
    public int size(){return individuals.length;}

    /**
     * Get the fittest individual
     * @return fittest individual
     */
    public Individual getFittestIndividual(){
        Individual fittest = individuals[0];
        for (int i = 0; i < individuals.length; i++){
            if (fittest.calculateFitness() <= getIndividual(i).calculateFitness()){
                fittest = getIndividual(i);
            }
        }
        return fittest;
    }
    public Individual getIndividual(int index){
        return individuals[index];
    }

    public int[] getWeights() {
        return weights;
    }

    public int[] getValues() {
        return values;
    }
}
