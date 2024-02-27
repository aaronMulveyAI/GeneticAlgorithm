package org.example.GUI;

import org.example.GA.Agents.Population;

import javax.swing.*;
import java.awt.*;

public class PopulationPanel extends JPanel {
    private Population population;

    public double averageFitness = 0.5;
    public void setPopulation(Population population) {
        this.population = population;
        repaint(); // Vuelve a dibujar el panel cuando la población cambie
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (population == null) {
            drawColorLegend(g); // Dibuja la leyenda de color incluso si no hay población
            return;
        }

        int gridSize = (int) Math.sqrt(population.size());
        int boxSize = (Math.min(getWidth(), getHeight()) / gridSize) - 7;

        double maxFitness = population.getFittestIndividual().getFitness();
        double minFitness = population.getLeastFitIndividual().getFitness();

        double sum = 0;
        for (int i = 0; i < population.size(); i++) {
            int x = 120 + (i % gridSize) * boxSize;
            int y = 35 + (i / gridSize) * boxSize;

            double fitness = population.getIndividual(i).getFitness();
            Color color = getColor(fitness, minFitness, maxFitness);

            sum += (double) color.getRed() / 255;

            g.setColor(color);
            g.fillRect(x, y, boxSize, boxSize);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, boxSize, boxSize);
        }

        drawAvarageFitness(g, sum / population.size());
        drawColorLegend(g); // Dibuja la leyenda de color
    }

    private void drawAvarageFitness(Graphics g, double avarageFitness) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String a = String.format("%.2f", avarageFitness);
        g.drawString("Fitness promedio: " + a, 170, getHeight() - 15);
    }

    private void drawColorLegend(Graphics g) {
        int legendHeight = getHeight() - 50; // Altura de la leyenda
        int legendWidth = 20; // Ancho de la leyenda
        int legendX = getWidth() - legendWidth - 100; // Posición X de la leyenda
        int legendY = 25; // Posición Y de la leyenda

        for (int i = 0; i < legendHeight; i++) {
            float scale = (float) i / (legendHeight - 1);
            Color color = getColor(scale, 1, 0);
            g.setColor(color);
            g.fillRect(legendX, legendY + i, legendWidth, 1);
        }

        // Dibuja los bordes de la leyenda
        g.setColor(Color.BLACK);
        g.drawRect(legendX, legendY, legendWidth, legendHeight);
        g.setColor(Color.BLACK);
        g.drawString("0%", getWidth() - 70, getHeight() - 25);
        g.drawString("50%", getWidth() - 75, getHeight() / 2);
        g.drawString("100%", getWidth() - 80, 35);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Visualización de la Población", 150, 25);

    }


    private static Color getColor(double fitness, double minFitness, double maxFitness) {
        float scale = (float) ((fitness - minFitness) / (maxFitness - minFitness));

        // Interpola entre rojo (1,0,0) y verde (0,1,0) a través de amarillo (1,1,0)
        Color color;
        if (scale <= 0.5) { // De rojo a amarillo
            // Aumenta el componente verde gradualmente, manteniendo el rojo al máximo
            color = new Color(1, 2 * scale, 0);
        } else { // De amarillo a verde
            // Reduce el componente rojo gradualmente, manteniendo el verde al máximo
            color = new Color(2 * (1 - scale), 1, 0);
        }
        return color;
    }

    public void clear() {
        this.population = null;
        repaint(); // Vuelve a dibujar el panel para limpiarlo
    }
}

