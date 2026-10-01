package org.example.GA.Agents.Abilities.Crossover;

import org.example.GA.Agents.Abilities.iReproduction;
import org.example.GA.Agents.Individual;
import static org.example.GA.Constants.RANDOM;

public class DoublePointCrossover implements iReproduction {
    @Override
    public Individual crossover(Individual father, Individual mother) {
        return switch (father.getProblem().getOptimizationMethod()) {
            case COMBINATORIAL -> crossoverCombination(father, mother);
            case PERMUTATION -> crossoverPermutation(father, mother);
        };
    }

    private int[] points(int length) {
        int first = RANDOM.nextInt(length + 1);
        int second = RANDOM.nextInt(length);
        if (second >= first) second++;
        return new int[]{Math.min(first, second), Math.max(first, second)};
    }

    @Override
    public Individual crossoverCombination(Individual father, Individual mother) {
        validateParents(father, mother);
        int[] genes = mother.getGenes();
        int[] points = points(genes.length);
        for (int i = points[0]; i < points[1]; i++) genes[i] = father.getGene(i);
        return new Individual(father.getProblem(), genes);
    }

    @Override
    public Individual crossoverPermutation(Individual father, Individual mother) {
        int[] points = points(father.getProblem().getModelSize());
        return orderedChild(father, mother, points[0], points[1]);
    }
}
