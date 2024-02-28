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
            int x = 15 + random.nextInt(panelWidth - 100);
            int y = 75 + random.nextInt(panelHeight - 100);
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

        // Título de la leyenda
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.BLACK);
        g.drawString("Distance Heat Map", 200, 25);
        g.setFont(new Font("Arial", Font.PLAIN, 12));

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

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        // Etiquetas de porcentaje
        g.drawString("0%", getWidth() - 35, getHeight() - 25);
        g.drawString("50%", getWidth() - 45, getHeight() / 2);
        g.drawString("100%", getWidth() - 50, 35);

        // Título de la leyenda
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



    public void clear() {
        this.population = null;
        repaint(); // Vuelve a dibujar el panel para limpiarlo
    }
}
