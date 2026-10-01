package org.example.GUI;

import org.example.GA.Agents.Population;
import org.example.OptimizationProblems.Modelling.*;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class VisualizationTest {
    @Test
    void everyVisualizationPaintsSmallAndLargePopulationsAndCanBeCleared() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AbstractProblem[] problems = {
                    new TravelingSalesmanProblem(new double[][]{{0, 1}, {1, 0}}),
                    new CircularTSProblem(2), new KnapsackProblem(new int[]{1}, new int[]{2}, 0),
                    new NQueensProblem(1), new GuessNumberProblem(1), new RealValueOptimizationProblem()
            };
            for (AbstractProblem problem : problems) {
                JComponent visualization = problem.getVisualization();
                visualization.setSize(80, 80);
                problem.getVisualization().setPopulation(new Population(problem, 1));
                paint(visualization);
                visualization.setSize(700, 500);
                paint(visualization);
                problem.getVisualization().clear();
                paint(visualization);
            }
        });
    }

    @Test
    void populationMapHandlesEqualFitnessAndNonsquarePopulationSizes() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PopulationPanel panel = new PopulationPanel();
            for (int size : new int[]{1, 3, 17}) {
                panel.setPopulation(new Population(new NQueensProblem(1), size));
                panel.setSize(80, 80);
                paint(panel);
                panel.setSize(700, 500);
                paint(panel);
                panel.clear();
                paint(panel);
            }
        });
    }

    private void paint(JComponent component) {
        BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            assertDoesNotThrow(() -> component.paint(graphics));
            assertTrue(java.util.stream.IntStream.range(0, image.getWidth()).anyMatch(x ->
                    java.util.stream.IntStream.range(0, image.getHeight()).anyMatch(y -> image.getRGB(x, y) != Color.BLACK.getRGB())));
        } finally {
            graphics.dispose();
        }
    }
}
