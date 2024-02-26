package org.example.GA.Agents.Abilities;

import org.example.GA.Agents.Individual;

public interface iReproduction {
    Individual crossover(Individual father, Individual mother);

    Individual crossoverCombination(Individual father, Individual mother);
    Individual crossoverPermutation(Individual father, Individual mother);
    default boolean containsGene(int[] genes, int gene) {
        for (int geneValue : genes) {
            if (geneValue == gene) {
                return true;
            }
        }
        return false;
    }

}
