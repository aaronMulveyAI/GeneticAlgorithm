package org.example.GA.Agents.Abilities.Selection;

import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.Agents.Individual;
import org.example.GA.Agents.Population;
import java.util.Arrays;
import static org.example.GA.Constants.RANDOM;

public class BrindleSelection implements iSelection {
    @Override
    public Individual selectIndividual(Population population) {
        double[] weights = RouletteSelection.selectionWeights(population);
        double total = Arrays.stream(weights).sum();
        double[] integerParts = new double[weights.length];
        double[] residuals = new double[weights.length];
        int integerTotal = 0;
        for (int i = 0; i < weights.length; i++) {
            double expected = weights[i] / total * population.size();
            integerParts[i] = Math.floor(expected);
            residuals[i] = expected - integerParts[i];
            integerTotal += (int) integerParts[i];
        }
        // Sample the integer copies or the normalized fractional remainder.
        double[] pool = RANDOM.nextInt(population.size()) < integerTotal ? integerParts : residuals;
        return population.getIndividual(RouletteSelection.draw(pool));
    }
}
