package org.example.GA.Agents.Abilities.Crossover;

import org.example.GA.Agents.Abilities.iReproduction;
import org.example.GA.Agents.Individual;

import java.util.Arrays;

import static org.example.GA.Constants.CROSSOVER_RATE;
import static org.example.GA.Constants.RANDOM;

public class UniformCrossover implements iReproduction {

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

        for (int i = 0; i < child.getGenes().length; i++) {
            int gene = (RANDOM.nextDouble() <= CROSSOVER_RATE)? father.getGenes()[i] : mother.getGenes()[i];
            child.setGene(i, gene);
        }
        return child;
    }

    @Override
    public Individual crossoverPermutation(Individual father, Individual mother) {
        Individual child = new Individual(father.getProblem());
        int[] childGenes = new int[father.getGenes().length];
        boolean[] taken = new boolean[childGenes.length];
        Arrays.fill(childGenes, -1); // Inicializar con valores no válidos
        Arrays.fill(taken, false);

        // Seleccionar genes de manera uniforme de ambos padres
        for (int i = 0; i < childGenes.length; i++) {
            if (RANDOM.nextDouble() < 0.5 && !taken[father.getGenes()[i]]) {
                childGenes[i] = father.getGenes()[i];
                taken[father.getGenes()[i]] = true;
            }
        }

        // Rellenar los espacios restantes con genes de la madre
        for (int i = 0; i < mother.getGenes().length; i++) {
            if (!taken[mother.getGenes()[i]]) {
                for (int j = 0; j < childGenes.length; j++) {
                    if (childGenes[j] == -1) {
                        childGenes[j] = mother.getGenes()[i];
                        taken[mother.getGenes()[i]] = true;
                        break;
                    }
                }
            }
        }

        child.setGenes(childGenes);
        return child;
    }
}
