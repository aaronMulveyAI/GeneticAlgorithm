package org.example.OptimizationProblems.VisualModelling;

import org.example.GA.Agents.Population;
import java.awt.*;
import javax.swing.*;

import static org.example.OptimizationProblems.Modelling.RealValueOptimizationProblem.*;

public class RealValueOptimizationVisualization extends AbstractVisualization {
    private Population population;
    private double minX = MIN_RANGE; // Mínimo del rango de x
    private double maxX = MAX_RANGE; // Máximo del rango de x
    private double minY = MIN_RANGE; // Valor mínimo estimado de y para el rango de la función
    private double maxY = MAX_RANGE; // Valor máximo estimado de y para el rango de la función

    public RealValueOptimizationVisualization() {

    }

    @Override
    public void setPopulation(Population population) {
        this.population = population;
        repaint(); // Solicita que el componente se repinte
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawAxes(g);
        drawFunction(g);
        if (population != null) {
            drawFittestIndividual(g);
        }
    }

    private void drawFunction(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.0f)); // Línea más fina

        int prevX = mapToScreenX(minX);
        int prevY = mapToScreenY(objectiveFunction.apply(minX));

        for (double x = minX + 0.1; x <= maxX; x += 0.1) {
            double y = objectiveFunction.apply(x);

            int plotX = mapToScreenX(x);
            int plotY = mapToScreenY(y);

            g2d.drawLine(prevX, prevY, plotX, plotY);

            prevX = plotX;
            prevY = plotY;
        }
    }

    private void drawFittestIndividual(Graphics g) {
        if (population.getFittestIndividual() == null) return;

        double realValue = binaryToReal(population.getFittestIndividual().getGenes());
        double y = objectiveFunction.apply(realValue);

        int plotX = mapToScreenX(realValue);
        int plotY = mapToScreenY(y);

        g.setColor(Color.RED);
        g.fillOval(plotX - 5, plotY - 5, 10, 10);
    }

    private void drawAxes(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        int originX = mapToScreenX(0);
        int originY = mapToScreenY(0);
        g.drawLine(originX, 0, originX, getHeight()); // Eje Y
        g.drawLine(0, originY, getWidth(), originY); // Eje X
    }

    private int mapToScreenX(double x) {
        double padding = 0.1 * getWidth(); // Agrega un poco de padding
        return (int) (((x - minX) / (maxX - minX)) * (getWidth() - 2 * padding) + padding);
    }

    private int mapToScreenY(double y) {
        double padding = 0.1 * getHeight(); // Agrega un poco de padding
        return (int) (((maxY - y) / (maxY - minY)) * (getHeight() - 2 * padding) + padding);
    }

    private double binaryToReal(int[] binary) {
        long value = 0;
        for (int i = 0; i < binary.length; i++) {
            if (binary[i] == 1) {
                value += Math.pow(2, binary.length - i - 1);
            }
        }
        return minX + ((maxX - minX) / (Math.pow(2, binary.length) - 1)) * value;
    }

    @Override
    public void clear() {
        this.population = null;
        repaint(); // Vuelve a dibujar el panel para limpiarlo
    }
}
