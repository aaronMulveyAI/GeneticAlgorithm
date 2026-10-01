package org.example.GA.Agents.Abilities.Selection;

import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.Agents.Individual;
import org.example.GA.Agents.Population;
import static org.example.GA.Constants.RANDOM;

public class TournamentSelection implements iSelection {
    private final int tournamentSize;

    public TournamentSelection(int tournamentSize) {
        if (tournamentSize < 1) throw new IllegalArgumentException("Tournament size must be positive");
        this.tournamentSize = tournamentSize;
    }

    @Override
    public Individual selectIndividual(Population population) {
        Individual best = population.getIndividual(RANDOM.nextInt(population.size()));
        for (int i = 1; i < tournamentSize; i++) {
            Individual candidate = population.getIndividual(RANDOM.nextInt(population.size()));
            if (population.isBetter(candidate.getFitness(), best.getFitness())) best = candidate;
        }
        return best;
    }
}
