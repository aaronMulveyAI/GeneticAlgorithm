package org.example.GA.Agents.Abilities.Selection;

import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.Agents.Individual;
import org.example.GA.Agents.Population;
import org.example.GA.Constants;

import static org.example.GA.Constants.RANDOM;

public class TournamentSelection implements iSelection {

    int TournamentSize;
    public TournamentSelection(int TournamentSize) {
        this.TournamentSize = TournamentSize;
    }

    @Override
    public Individual selectIndividual(Population population) {

        Population newPopulation = new Population(
                population.getIndividuals()[0].getProblem(),
                this.TournamentSize
        );

        for (int i = 0; i < this.TournamentSize; i++) {
            int randomIndex = (int) (RANDOM.nextDouble() * population.size());
            newPopulation.saveIndividual(i, population.getIndividual(randomIndex));
        }
        return newPopulation.getFittestIndividual();
    }
}
