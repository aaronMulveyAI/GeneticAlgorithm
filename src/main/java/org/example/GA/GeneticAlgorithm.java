package org.example.GA;

import org.example.GA.Agents.Individual;
import org.example.GA.Agents.Population;

import static org.example.GA.Constants.*;

public class GeneticAlgorithm {

    int[] weight,value;
    public GeneticAlgorithm(int[] weight, int[] value){
        this.weight = weight;
        this.value = value;
    }

    /**
     * Evolve population to next generation using crossover and mutation operations
     * @param population population to evolve
     * @return new population after evolution
     */
    public Population evolve(Population population){

        Population newPopulation = new Population(population.size(), weight, value);

        // crossover
        for (int i = 0; i < population.size(); i++) {
            Individual father = randomSelection(population);
            Individual mother = randomSelection(population);
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
    private Individual randomSelection(Population population){
        Population newPopulation = new Population(Constants.TOURNAMENT_SELECTION_SIZE, weight, value);
        for (int i = 0; i < Constants.TOURNAMENT_SELECTION_SIZE; i++) {
            int randomIndex = (int) (RANDOM.nextDouble() * population.size());
            newPopulation.saveIndividual(i, population.getIndividual(randomIndex));
        }
        return newPopulation.getFittestIndividual();
    }

    /**
     * Crossover individuals
     * @param father father individual
     * @param mother mother individual
     * @return child individual
     */
    public Individual crossover(Individual father, Individual mother) {
        Individual child = new Individual(weight, value);
        for (int i = 0; i < value.length; i++) {
            int gene = (RANDOM.nextDouble() <= CROSSOVER_RATE)? father.getGenes()[i] : mother.getGenes()[i];
            child.setGene(i, gene);
        }
        return child;
    }

    /**
     * Mutate individual
     * @param individual individual to mutate
     */
    private void mutate(Individual individual){
        for (int i = 0; i < value.length; i++) {
            if (RANDOM.nextDouble() <= MUTATION_RATE) {
                int gene = RANDOM.nextInt(2);
                individual.setGene(i, gene);
            }
        }
    }
}
