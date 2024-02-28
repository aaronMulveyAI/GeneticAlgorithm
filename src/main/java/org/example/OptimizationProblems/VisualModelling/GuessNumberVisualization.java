package org.example.OptimizationProblems.VisualModelling;

import org.example.GA.Agents.Population;
import org.example.OptimizationProblems.Modelling.GuessNumberProblem;

import javax.swing.*;
import java.awt.*;

public class GuessNumberVisualization extends AbstractVisualization {
    private Population population;
    private static final int FONT_SIZE = 20; // Tamaño de fuente para los números
    private static final int PADDING = 10; // Padding en píxeles alrededor de los números

    public GuessNumberVisualization() {
    }

    @Override
    public void setPopulation(Population population) {
        this.population = population;
        repaint(); // Solicita que el componente se repinte
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (population == null) return;

        int[] guess = population.getFittestIndividual().getGenes();
        int[] target = GuessNumberProblem.targetSequence;

        g.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
        FontMetrics metrics = g.getFontMetrics();
        int charWidth = metrics.charWidth('0');
        int charHeight = metrics.getHeight();

        // Calcula el número máximo de números por línea
        int maxNumbersPerLine = (getWidth() - PADDING * 2) / charWidth;
        int linesNeeded = (int) Math.ceil((double)GuessNumberProblem.sequenceLength / maxNumbersPerLine);

        // Calcula la posición inicial y para el centrado vertical
        int totalHeight = linesNeeded * 2 * charHeight; // Total de líneas (objetivo + adivinanza) * altura
        int startY = (getHeight() - totalHeight) / 2;

        // Dibuja la secuencia en líneas, ajustando según sea necesario
        for (int line = 0; line < linesNeeded; line++) {
            int startIdx = line * maxNumbersPerLine;
            int endIdx = Math.min(startIdx + maxNumbersPerLine, GuessNumberProblem.sequenceLength);
            int lineWidth = (endIdx - startIdx) * charWidth;
            int x = (getWidth() - lineWidth) / 2;

            for (int i = startIdx; i < endIdx; i++) {
                // Dibuja número objetivo
                g.setColor(Color.BLACK);
                g.drawString(String.valueOf(target[i]), x, startY + line * 2 * charHeight);

                // Dibuja número adivinado con color según coincidencia
                g.setColor(guess[i] == target[i] ? Color.GREEN : Color.RED);
                g.drawString(String.valueOf(guess[i]), x, startY + (line * 2 + 1) * charHeight);

                x += charWidth;
            }
        }
    }

    @Override
    public void clear() {
        setPopulation(null);
        repaint(); // Vuelve a dibujar el panel para limpiarlo
    }
}
