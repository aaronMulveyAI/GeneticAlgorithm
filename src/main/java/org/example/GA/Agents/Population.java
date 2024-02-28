package org.example.GA.Agents;


import org.example.OptimizationProblems.AbstractProblem;

public class Population {
    private Individual[] individuals;
    private int[] weights;
    private int[] values;

    public Population(AbstractProblem problem, int populationSize){
        individuals = new Individual[populationSize];
        this.weights = weights;
        this.values = values;
        initializePopulation(problem);
    }

    /**
     * Initialize population
     */
    private void initializePopulation(AbstractProblem problem){
        for (int i = 0; i < individuals.length; i++) {
            individuals[i] = new Individual(problem);
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

            switch (individuals[0].getProblem().optimizationMethod){

                case COMBINATORIAL:
                    if (fittest.calculateFitness() <= getIndividual(i).calculateFitness()){
                        fittest = getIndividual(i);
                    }
                    break;

                case PERMUTATION:
                    if (fittest.calculateFitness() > getIndividual(i).calculateFitness()){
                        fittest = getIndividual(i);
                    }
                    break;
            }
        }
        return fittest;
    }

    public Individual getLeastFitIndividual(){
        Individual fittest = individuals[0];

        for (int i = 0; i < individuals.length; i++){

            switch (individuals[0].getProblem().optimizationMethod){

                case COMBINATORIAL:
                    if (fittest.calculateFitness() > getIndividual(i).calculateFitness()){
                        fittest = getIndividual(i);
                    }
                    break;
                case PERMUTATION:
                    if (fittest.calculateFitness() < getIndividual(i).calculateFitness()){
                        fittest = getIndividual(i);
                    }
                    break;
            }
        }
        return fittest;
    }
    public Individual getIndividual(int index){
        return individuals[index];
    }

    public Individual[] getIndividuals() {return individuals;}

}
