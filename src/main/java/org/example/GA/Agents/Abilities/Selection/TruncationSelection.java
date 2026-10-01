package org.example.GA.Agents.Abilities.Selection;

import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.Agents.Individual;
import org.example.GA.Agents.Population;
import org.example.GA.OPTIMIZATION_TYPE;
import java.util.Arrays;
import java.util.Comparator;
import static org.example.GA.Constants.RANDOM;

public class TruncationSelection implements iSelection {
    private final double truncationThreshold;

    public TruncationSelection(double threshold) {
        if (!Double.isFinite(threshold) || threshold <= 0 || threshold > 1) {
            throw new IllegalArgumentException("Truncation threshold must be in (0, 1]");
        }
        this.truncationThreshold = threshold;
    }

    @Override
    public Individual selectIndividual(Population population) {
        Individual[] sorted = population.getIndividuals();
        Comparator<Individual> order = Comparator.comparingDouble(Individual::getFitness);
        if (population.getOptimizationType() == OPTIMIZATION_TYPE.MAXIMIZE) order = order.reversed();
        Arrays.sort(sorted, order);
        int count = Math.max(1, (int) (truncationThreshold * population.size()));
        return sorted[RANDOM.nextInt(count)];
    }
}
