package org.example.GA;

import org.example.GA.Agents.Abilities.*;
import org.example.GA.Agents.*;
import org.example.OptimizationProblems.AbstractProblem;

import static org.example.GA.Constants.*;

public class GeneticAlgorithm {

    private AbstractProblem problem;
    private iSelection selectionMethod;
    private iReproduction reproductionMethod;

    public GeneticAlgorithm(AbstractProblem problem, iSelection selectionMethod, iReproduction reproductionMethod){
        this.problem = problem;
        this.selectionMethod = selectionMethod;
        this.reproductionMethod = reproductionMethod;
    }

    /**
     * Evolve population to next generation using crossover and mutation operations
     * @param population population to evolve
     * @return new population after evolution
     */
    public Population evolve(Population population){

        Population newPopulation = new Population(problem, population.size());

        // crossover
        for (int i = 0; i < population.size(); i++) {
            Individual father = selection(population);
            Individual mother = selection(population);
            Individual child = crossover(father, mother);
            newPopulation.saveIndividual(i, child);
        }

        // mutate
        for (int i = 0; i < newPopulation.size(); i++) {
            mutate(newPopulation.getIndividual(i));
        }

        return newPopulation;
    }

    /**
     * Random Selection of individuals with tournament selection
     * @param population population to select from
     * @return individual selected
     */
    private Individual selection(Population population){
       return selectionMethod.selectIndividual(population);
    }

    /**
     * Crossover individuals
     * @param father father individual
     * @param mother mother individual
     * @return child individual
     */
    public Individual crossover(Individual father, Individual mother) {
        return reproductionMethod.crossover(father, mother);
    }

    /**
     * Mutate individual
     * @param individual individual to mutate
     */
    private void mutate(Individual individual){
        switch (individual.getProblem().optimizationMethod){
            case COMBINATORIAL:
                mutateCombinatorial(individual);
                break;
            case PERMUTATION:
                mutatePermutation(individual);
                break;
        }
    }

    private void mutateCombinatorial(Individual individual){
        for (int i = 0; i < problem.modelSize; i++) {
            if (RANDOM.nextDouble() <= MUTATION_RATE) {
                int gene = RANDOM.nextInt(individual.getProblem().modelSize);
                individual.setGene(i, gene);
            }
        }
    }

    private void mutatePermutation(Individual individual){
        if (RANDOM.nextDouble() <= MUTATION_RATE) {
            int index1 = RANDOM.nextInt(individual.getGenes().length);
            int index2 = RANDOM.nextInt(individual.getGenes().length);

            // Asegúrate de que index1 y index2 sean diferentes
            while (index1 == index2) {
                index2 = RANDOM.nextInt(individual.getGenes().length);
            }

            // Realiza el intercambio (swap)
            int temp = individual.getGenes()[index1];
            individual.setGene(index1, individual.getGenes()[index2]);
            individual.setGene(index2, temp);
        }
    }

    public AbstractProblem getProblem() {
        return problem;
    }

    public void setProblem(AbstractProblem problem) {
        this.problem = problem;
    }

    public iSelection getSelectionMethod() {
        return selectionMethod;
    }

    public void setSelectionMethod(iSelection selectionMethod) {
        this.selectionMethod = selectionMethod;
    }

    public iReproduction getReproductionMethod() {
        return reproductionMethod;
    }

    public void setReproductionMethod(iReproduction reproductionMethod) {
        this.reproductionMethod = reproductionMethod;
    }
}
