package org.example.OptimizationProblems.VisualModelling;

import org.example.GA.Agents.Population;
import org.example.OptimizationProblems.Modelling.CircularTSProblem;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CircularTSPVisualization extends AbstractVisualization {

    private double[][] distances;
    private int[] visitOrder;
    private Map<Integer, Point> nodePositions;

    public Population population;

    public boolean inicialiced = true;

    public CircularTSPVisualization() {

    }
    @Override
    public void setPopulation(Population population){
        this.population = population;
        this.distances = CircularTSProblem.distances;
        this.visitOrder = population.getFittestIndividual().getGenes();

        // Solo genera posiciones de nodo en la inicialización o reinicio
        if(nodePositions == null || nodePositions.isEmpty()){
            generateNodePositions();
        }

        repaint(); // Solicita que el componente se repinte
    }


    private void generateNodePositions() {
        nodePositions = new HashMap<>();

        // Calcula el centro del panel
        int centerX = this.getSize().width / 2;
        int centerY = this.getSize().height / 2;

        // Define el radio del círculo en el que se colocarán los nodos
        // Asegúrate de que el círculo se ajuste dentro del panel
        int radius = Math.min(centerX, centerY) - 50; // 50 píxeles de margen

        // Calcula la posición de cada nodo distribuyéndolos uniformemente alrededor del círculo
        for (int i = 0; i < distances.length; i++) {
            // Calcula el ángulo para el nodo actual
            double angle = 2 * Math.PI * i / distances.length;

            // Calcula las coordenadas (x, y) basándonos en el ángulo y el radio
            int x = centerX + (int) (radius * Math.cos(angle));
            int y = centerY + (int) (radius * Math.sin(angle));

            // Almacena la posición del nodo
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
            double distance = distances[visitOrder[i]][visitOrder[i+1]];

            // Calcula la intensidad del color basado en la distancia
            float intensity = Math.min(1.0f, (float)distance / 5.0f); // Ajustar el divisor según el rango de tus distancias
            g.setColor(new Color(1.0f, 0.0f, 0.0f, intensity)); // Usa la intensidad para el color rojo
            g.drawLine(start.x, start.y, end.x, end.y);
        }

        // Dibujar nodos al final para que estén encima de las líneas
        for (Point point : nodePositions.values()) {
            g.setColor(Color.BLUE);
            g.fillOval(point.x - 5, point.y - 5, 10, 10);
        }

        drawColorLegend(g);
    }
    private void drawColorLegend(Graphics g) {
        int legendHeight = getHeight() - 50; // Altura de la leyenda
        int legendWidth = 20; // Ancho de la leyenda
        int legendX = getWidth() - legendWidth - 60; // Posición X de la leyenda hacia el lado derecho
        int legendY = 25; // Posición Y de la leyenda en la parte superior

        // Generar y dibujar la gradación de colores en la leyenda
        for (int i = 0; i < legendHeight; i++) {
            float scale = (float) i / (legendHeight - 1);
            // Asumiendo que tienes una función getColor que devuelve el color según la escala
            Color color = getColor(scale, 1, 0);
            g.setColor(color);
            g.fillRect(legendX, legendY + i, legendWidth, 1);
        }

        // Dibuja los bordes de la leyenda
        g.setColor(Color.BLACK);
        g.drawRect(legendX, legendY, legendWidth, legendHeight);

        // Etiquetas de porcentaje
        g.drawString("0%", getWidth() - 35, getHeight() - 25);
        g.drawString("50%", getWidth() - 45, getHeight() / 2);
        g.drawString("100%", getWidth() - 50, 35);

        // Título de la leyenda
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Distance Heat Map", 200, 25);
        g.setFont(new Font("Arial", Font.BOLD, 12));

    }



    private static Color getColor(double value, double minValue, double maxValue) {
        // Calcula la escala entre 0.0 y 1.0
        float scale = (float) ((value - minValue) / (maxValue - minValue));

        // Interpola dentro del espectro de rojos
        // En este caso, variamos el componente RGB del rojo para obtener tonalidades más claras u oscuras
        // Mantenemos el componente rojo al máximo (1) y variamos el verde y el azul de forma idéntica para oscurecer el rojo
        // A medida que scale se acerca a 1, el color se hace más oscuro.

        // Para una escala de rojos, podemos pensar en hacerlo más claro o más oscuro ajustando la luminosidad.
        // Aquí, en lugar de ir hacia el verde o el azar, ajustaremos la intensidad del rojo para oscurecerlo.
        // Esto se puede lograr ajustando los componentes verde y azul de manera simétrica para mantener el tono rojo.
        // Comenzaremos con un rojo puro y lo haremos gradualmente más oscuro.
        float intensity = 1 - scale; // Invertimos la escala para que más oscuro sea más cerca de 1

        Color color = new Color(1, intensity, intensity);

        return color;
    }


    @Override
    public void clear() {
        this.population = null;
        repaint(); // Vuelve a dibujar el panel para limpiarlo
    }
}

