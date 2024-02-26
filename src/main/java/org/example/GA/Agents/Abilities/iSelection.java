package org.example.GA.Agents.Abilities;

import org.example.GA.Agents.Individual;
import org.example.GA.Agents.Population;

public interface iSelection {
    Individual selectIndividual(Population population);
}
