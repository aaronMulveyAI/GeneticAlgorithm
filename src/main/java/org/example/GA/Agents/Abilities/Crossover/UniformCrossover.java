package org.example.GA.Agents.Abilities.Crossover;

import org.example.GA.Agents.Abilities.iReproduction;
import org.example.GA.Agents.Individual;
import java.util.Arrays;
import static org.example.GA.Constants.RANDOM;

public class UniformCrossover implements iReproduction {
    @Override
    public Individual crossover(Individual father, Individual mother) {
        return switch (father.getProblem().getOptimizationMethod()) {
            case COMBINATORIAL -> crossoverCombination(father, mother);
            case PERMUTATION -> crossoverPermutation(father, mother);
        };
    }

    @Override
    public Individual crossoverCombination(Individual father, Individual mother) {
        validateParents(father, mother);
        int[] genes = father.getGenes();
        for (int i = 0; i < genes.length; i++) {
            if (RANDOM.nextBoolean()) genes[i] = mother.getGene(i);
        }
        return new Individual(father.getProblem(), genes);
    }

    @Override
    public Individual crossoverPermutation(Individual father, Individual mother) {
        validateParents(father, mother);
        int[] genes = new int[father.getProblem().getModelSize()];
        boolean[] taken = new boolean[genes.length];
        Arrays.fill(genes, -1);
        for (int i = 0; i < genes.length; i++) {
            if (RANDOM.nextBoolean()) {
                genes[i] = father.getGene(i);
                taken[genes[i]] = true;
            }
        }
        int position = 0;
        for (int i = 0; i < genes.length; i++) {
            int gene = mother.getGene(i);
            if (!taken[gene]) {
                while (genes[position] != -1) position++;
                genes[position] = gene;
                taken[gene] = true;
            }
        }
        return new Individual(father.getProblem(), genes);
    }
}
