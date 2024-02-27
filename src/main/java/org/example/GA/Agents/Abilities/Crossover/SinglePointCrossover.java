package org.example.GA.Agents.Abilities.Crossover;

import org.example.GA.Agents.Abilities.iReproduction;
import org.example.GA.Agents.Individual;

import java.util.Arrays;
import static org.example.GA.Constants.RANDOM;

public class SinglePointCrossover implements iReproduction {

    @Override
    public Individual crossover(Individual father, Individual mother) {
       return switch (father.getProblem().optimizationMethod) {
           case COMBINATORIAL -> crossoverCombination(father, mother);
           case PERMUTATION -> crossoverPermutation(father, mother);
       };
    }

    @Override
    public Individual crossoverCombination(Individual father, Individual mother) {
        Individual child = new Individual(father.getProblem());

        int crossoverPoint = RANDOM.nextInt(father.getProblem().modelSize);

        for (int i = 0; i < child.getGenes().length; i++) {
            if (i <= crossoverPoint) {
                child.setGene(i, father.getGenes()[i]);
            } else {
                child.setGene(i, mother.getGenes()[i]);
            }
        }

        return child;
    }

    @Override
    public Individual crossoverPermutation(Individual father, Individual mother) {
        Individual child = new Individual(father.getProblem());
        int[] childGenes = new int[father.getGenes().length];
        Arrays.fill(childGenes, -1);

        int crossoverPoint1 = RANDOM.nextInt(child.getGenes().length);
        int crossoverPoint2 = RANDOM.nextInt(child.getGenes().length);

        if (crossoverPoint1 > crossoverPoint2) {
            int temp = crossoverPoint1;
            crossoverPoint1 = crossoverPoint2;
            crossoverPoint2 = temp;
        }


        for (int i = crossoverPoint1; i <= crossoverPoint2; i++) {
            childGenes[i] = father.getGenes()[i];
        }


        int currentPos = 0;
        for (int i = 0; i < childGenes.length; i++) {
            int gene = mother.getGenes()[i];
            if (!containsGene(childGenes, gene)) {
                // Encuentra la próxima posición no asignada
                while (currentPos < childGenes.length && childGenes[currentPos] != -1) {
                    currentPos++;
                }
                if (currentPos < childGenes.length) {
                    childGenes[currentPos] = gene;
                }
            }
        }

        child.setGenes(childGenes);
        return child;
    }
}
