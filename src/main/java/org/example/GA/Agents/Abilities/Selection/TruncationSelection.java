package org.example.GA.Agents.Abilities.Selection;

import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.Agents.Individual;
import org.example.GA.Agents.Population;
import org.example.GA.GeneticAlgorithm;
import org.example.GA.OPTIMIZATION_TYPE;

import java.util.Arrays;
import java.util.Comparator;

public class TruncationSelection implements iSelection {
    private double truncationThreshold = 0.5;
    static int i = 0;

    public TruncationSelection(double threshold) {
        this.truncationThreshold = threshold;
    }

    @Override
    public Individual selectIndividual(Population population) {

        Individual[] sortedIndividuals = Arrays.copyOf(population.getIndividuals(), population.size());

        if(GeneticAlgorithm.optimizationType == OPTIMIZATION_TYPE.MAXIMIZE){
            Arrays.sort(sortedIndividuals, (i1, i2) -> Double.compare(i2.getFitness(), i1.getFitness()));
        }else{
            Arrays.sort(sortedIndividuals, Comparator.comparingDouble(Individual::getFitness));
        }

        int numberOfSelected = (int) (truncationThreshold * population.size());

        Individual selectedIndividual = sortedIndividuals[i % numberOfSelected];
        i = (i + 1) % numberOfSelected;
        return selectedIndividual;
    }
}
