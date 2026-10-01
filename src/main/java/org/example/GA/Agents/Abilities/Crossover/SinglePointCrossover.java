package org.example.GA.Agents.Abilities.Crossover;

import org.example.GA.Agents.Abilities.iReproduction;
import org.example.GA.Agents.Individual;
import static org.example.GA.Constants.RANDOM;

public class SinglePointCrossover implements iReproduction {
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
        int point = genes.length == 1 ? 1 : RANDOM.nextInt(genes.length - 1) + 1;
        for (int i = point; i < genes.length; i++) genes[i] = mother.getGene(i);
        return new Individual(father.getProblem(), genes);
    }

    @Override
    public Individual crossoverPermutation(Individual father, Individual mother) {
        int length = father.getProblem().getModelSize();
        int point = length == 1 ? 1 : RANDOM.nextInt(length - 1) + 1;
        return orderedChild(father, mother, 0, point);
    }
}
