package org.example.OptimizationProblems.VisualModelling;

import org.example.GA.Agents.Population;

import javax.swing.*;

public abstract class AbstractVisualization extends JPanel {

    public Population population;
    public abstract void setPopulation(Population population);
    public abstract void clear();
}
