package org.example.OptimizationProblems;

import org.example.GA.Agents.Population;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TravellingSalesmanVisualization extends JPanel {

    private double[][] distances;
    private int[] visitOrder;
    private Map<Integer, Point> nodePositions;

    public Population population;

    public boolean inicialiced = true;

    public TravellingSalesmanVisualization() {

    }
    public void setPopulation(Population population){
        this.population = population;
        this.distances = TravelingSalesmanProblem.distances;
        this.visitOrder = population.getFittestIndividual().getGenes();

        // Solo genera posiciones de nodo en la inicialización o reinicio
        if(nodePositions == null || nodePositions.isEmpty()){
            generateNodePositions();
        }

        repaint(); // Solicita que el componente se repinte
    }


    private void generateNodePositions() {
        nodePositions = new HashMap<>();

        Random random = new Random();
        int panelWidth = this.getSize().width;
        int panelHeight = this.getSize().height;

        for (int i = 0; i < distances.length; i++) {
            // Asegúrate de dejar un margen para evitar que los nodos se dibujen demasiado cerca de los bordes
            int x = 50 + random.nextInt(Math.abs(panelWidth - 200) + 100);
            int y = 75 + random.nextInt(Math.abs(panelHeight - 200) + 100);
            nodePositions.put(i, new Point(x, y));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGraph(g);
    }

    private void drawGraph(Graphics g) {
        if (population == null) {
            return;
        }
        this.visitOrder = population.getFittestIndividual().getGenes();

        for (int i = 0; i < visitOrder.length - 1; i++) {
            Point start = nodePositions.get(visitOrder[i]);
            Point end = nodePositions.get(visitOrder[i + 1]);
            g.setColor(Color.RED);
            g.drawLine(start.x, start.y, end.x, end.y);

        }

        // Dibujar nodos al final para que estén encima de las líneas
        for (Point point : nodePositions.values()) {
            g.setColor(Color.BLUE);
            g.fillOval(point.x - 5, point.y - 5, 10, 10);
        }
    }

    public void clear() {
        this.population = null;
        repaint(); // Vuelve a dibujar el panel para limpiarlo
    }
}
