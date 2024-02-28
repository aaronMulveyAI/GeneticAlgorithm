package org.example.OptimizationProblems.VisualModelling;

import org.example.GA.Agents.Population;
import javax.swing.*;
import java.awt.*;
import static org.example.OptimizationProblems.Modelling.NQueensProblem.BOARD_SIZE;

public class NQueensVisualization extends AbstractVisualization {
    private int[] queensPositions; // Almacena las posiciones de las reinas

    public NQueensVisualization() {
    }

    @Override
    public void setPopulation(Population population) {
        if (population != null) {
            queensPositions = population.getFittestIndividual().getGenes();
        } else {
            queensPositions = null;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawQueens(g);
    }

    private void drawBoard(Graphics g) {
        // Hacer el tablero un poco más pequeño ajustando el tamaño de la celda
        int cellSize = (int) (Math.min(getWidth(), getHeight()) * 0.8 / BOARD_SIZE); // Usa el 80% del menor lado para el tablero
        // Calcular el inicio para centrar el tablero
        int startX = (getWidth() - (cellSize * BOARD_SIZE)) / 2;
        int startY = (getHeight() - (cellSize * BOARD_SIZE)) / 2;


        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if ((i + j) % 2 == 0) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.BLACK);
                }
                g.fillRect(startX + j * cellSize, startY + i * cellSize, cellSize, cellSize);
                g.setColor(Color.BLACK);
                g.drawRect(startX + j * cellSize, startY + i * cellSize, cellSize, cellSize);
            }
        }
    }

    private void drawQueens(Graphics g) {
        if (queensPositions == null) return;
        // Reutilizar el cálculo de cellSize y las posiciones iniciales para asegurar que las reinas se dibujen correctamente
        int cellSize = (int) (Math.min(getWidth(), getHeight()) * 0.8 / BOARD_SIZE);
        int startX = (getWidth() - (cellSize * BOARD_SIZE)) / 2;
        int startY = (getHeight() - (cellSize * BOARD_SIZE)) / 2;

        // Verificar si la solución es válida
        boolean isValid = isSolutionValid();

        g.setColor(isValid ? new Color(0x0D9F00) : Color.RED); // Verde si es válida, rojo
        for (int i = 0; i < BOARD_SIZE; i++) {
            int x = startX + queensPositions[i] * cellSize;
            int y = startY + i * cellSize;
            g.fillOval(x + cellSize / 4, y + cellSize / 4, cellSize / 2, cellSize / 2);
        }
    }

    private boolean isSolutionValid() {
        if (queensPositions == null) return false;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = i + 1; j < BOARD_SIZE; j++) {
                // Comprobar si las reinas están en la misma columna
                if (queensPositions[i] == queensPositions[j]) return false;
                // Comprobar si las reinas están en la misma diagonal
                if (Math.abs(queensPositions[i] - queensPositions[j]) == Math.abs(i - j)) return false;
            }
        }
        return true; // Si no se encontraron ataques, la solución es válida
    }

    @Override
    public void clear() {
        setPopulation(null);
        repaint();
    }
}
