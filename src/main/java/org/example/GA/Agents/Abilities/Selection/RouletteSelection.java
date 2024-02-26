package org.example.GA.Agents.Abilities.Selection;

import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.Agents.Individual;
import org.example.GA.Agents.Population;

public class RouletteSelection implements iSelection {
    @Override
    public Individual selectIndividual(Population population) {
        int totalFitness = 0;
        for (Individual individual : population.getIndividuals()) {
            totalFitness += Math.max(individual.getFitness(), 1); // Asegurar que el fitness mínimo contribuya es 1
        }
        if (totalFitness == 0) {
            // Manejar caso especial donde totalFitness es 0, por ejemplo, seleccionando un individuo al azar
            int randomIndex = (int) (Math.random() * population.size());
            return population.getIndividual(randomIndex);
        }
        int random = (int) (Math.random() * totalFitness);
        int current = 0;
        for (Individual individual : population.getIndividuals()) {
            current += Math.max(individual.getFitness(), 1); // Asegurar que el fitness mínimo contribuya es 1
            if (current > random) {
                return individual;
            }
        }
        // Como fallback, retornar el último individuo para evitar null
        return population.getIndividual(population.size() - 1);
    }

}
