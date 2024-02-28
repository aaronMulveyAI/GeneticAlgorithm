package org.example.OptimizationProblems.VisualModelling;


import org.example.GA.Agents.Individual;
import org.example.GA.Agents.Population;
import org.example.OptimizationProblems.Modelling.KnapsackProblem;

import javax.swing.*;
import java.awt.*;

public class KnapsackVisualization extends AbstractVisualization {

    private Population population;
    private double capacity; // Capacidad total de la mochila
    private double filledPercentage; // Porcentaje del espacio lleno

    public KnapsackVisualization(int capacity) {
        this.capacity = capacity;
    }

    double totalWeight;

    @Override
    public void setPopulation(Population population) {
        this.population = population;
        if (population != null) {
            // Asumiendo que existe un método en Population para obtener el peso total de la mochila más apta
            Individual fittest = this.population.getFittestIndividual();

            totalWeight = 0;
            for (int i = 0; i < fittest.genes.length; i++) {
                if (fittest.genes[i] == 1) {
                    totalWeight += KnapsackProblem.weights[i];
                }
            }

            this.filledPercentage = totalWeight / capacity;
        } else {
            this.filledPercentage = 0;
        }
        repaint(); // Solicita que el componente se repinte
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawKnapsack(g);
    }

    private void drawKnapsack(Graphics g) {
        // Dimensiones de la caja
        int boxWidth = 200;
        int boxHeight = 100;
        int startX = (this.getWidth() - boxWidth) / 2; // Centrar la caja en el panel
        int startY = (this.getHeight() - boxHeight) / 2; // Centrar la caja en el panel

        // Calcular el espacio lleno
        int filledHeight = (int) (boxHeight * filledPercentage);
        int emptyHeight = boxHeight - filledHeight;

        // Dibujar el espacio lleno
        g.setColor(Color.BLUE);
        g.fillRect(startX, startY + emptyHeight, boxWidth, filledHeight);

        // Dibujar el espacio vacío
        g.setColor(Color.RED);
        g.fillRect(startX, startY, boxWidth, emptyHeight);

        // Dibujar el borde de la caja
        g.setColor(Color.BLACK);
        g.drawRect(startX, startY, boxWidth, boxHeight);

        g.drawString("Filled : " + totalWeight + " / " + capacity, 240, 140);
    }

    @Override
    public void clear() {
        setPopulation(null);
        repaint(); // Vuelve a dibujar el panel para limpiarlo
    }
}
