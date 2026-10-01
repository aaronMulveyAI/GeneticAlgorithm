package org.example.GA.Agents.Abilities;

import org.example.GA.Agents.Individual;
import java.util.Arrays;

public interface iReproduction {
    Individual crossover(Individual father, Individual mother);
    Individual crossoverCombination(Individual father, Individual mother);
    Individual crossoverPermutation(Individual father, Individual mother);

    default void validateParents(Individual father, Individual mother) {
        if (father.getProblem() != mother.getProblem()) {
            throw new IllegalArgumentException("Parents must belong to the same problem");
        }
        father.getProblem().validateSolution(father.getGenes());
        mother.getProblem().validateSolution(mother.getGenes());
    }

    default boolean containsGene(int[] genes, int gene) {
        for (int value : genes) {
            if (value == gene) return true;
        }
        return false;
    }

    default Individual orderedChild(Individual father, Individual mother, int start, int end) {
        validateParents(father, mother);
        int[] genes = new int[father.getProblem().getModelSize()];
        boolean[] taken = new boolean[genes.length];
        Arrays.fill(genes, -1);
        for (int i = start; i < end; i++) {
            genes[i] = father.getGene(i);
            taken[genes[i]] = true;
        }
        int position = end % genes.length;
        for (int offset = 0; offset < genes.length; offset++) {
            int gene = mother.getGene((end + offset) % genes.length);
            if (!taken[gene]) {
                while (genes[position] != -1) position = (position + 1) % genes.length;
                genes[position] = gene;
                taken[gene] = true;
            }
        }
        return new Individual(father.getProblem(), genes);
    }
}
