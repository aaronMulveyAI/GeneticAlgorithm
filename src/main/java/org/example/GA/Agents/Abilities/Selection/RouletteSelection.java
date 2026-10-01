package org.example.GA.Agents.Abilities.Selection;

import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.Agents.Individual;
import org.example.GA.Agents.Population;
import org.example.GA.OPTIMIZATION_TYPE;
import java.util.Arrays;
import static org.example.GA.Constants.RANDOM;

public class RouletteSelection implements iSelection {
    @Override
    public Individual selectIndividual(Population population) {
        double[] weights = selectionWeights(population);
        return population.getIndividual(draw(weights));
    }

    static double[] selectionWeights(Population population) {
        double[] weights = new double[population.size()];
        double scale = 0;
        for (int i = 0; i < weights.length; i++) {
            weights[i] = population.getIndividual(i).getFitness();
            scale = Math.max(scale, Math.abs(weights[i]));
        }
        if (scale == 0) {
            Arrays.fill(weights, 1);
            return weights;
        }
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < weights.length; i++) {
            weights[i] /= scale;
            minimum = Math.min(minimum, weights[i]);
            maximum = Math.max(maximum, weights[i]);
        }
        if (minimum == maximum) {
            Arrays.fill(weights, 1);
            return weights;
        }
        // Scale first to avoid overflow, then make every sampling weight positive.
        for (int i = 0; i < weights.length; i++) {
            weights[i] = population.getOptimizationType() == OPTIMIZATION_TYPE.MINIMIZE
                    ? maximum - weights[i] + 1e-12
                    : weights[i] - Math.min(minimum, 0) + 1e-12;
        }
        return weights;
    }

    static int draw(double[] weights) {
        double total = Arrays.stream(weights).sum();
        double target = RANDOM.nextDouble() * total;
        for (int i = 0; i < weights.length; i++) {
            target -= weights[i];
            if (target < 0) return i;
        }
        return weights.length - 1;
    }
}
