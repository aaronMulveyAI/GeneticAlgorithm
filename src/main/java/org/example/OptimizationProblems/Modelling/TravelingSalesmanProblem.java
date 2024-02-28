package org.example.OptimizationProblems.Modelling;

import org.example.OptimizationProblems.OptimizationMethod;
import org.example.OptimizationProblems.VisualModelling.TravellingSalesmanVisualization;

import static org.example.GA.Constants.RANDOM;

public class TravelingSalesmanProblem extends AbstractProblem {
    public static double[][] distances;

    public TravelingSalesmanProblem(){
        this(new double[][]{
                {0, 10, 15, 20},
                {10, 0, 35, 25},
                {15, 35, 0, 30},
                {20, 25, 30, 0}
        });
    }

    public TravelingSalesmanProblem(double[][] distances) {
        super(new TravellingSalesmanVisualization(), "Traveling Salesman Problem", OptimizationMethod.PERMUTATION, distances.length);
        TravelingSalesmanProblem.distances = distances;
    }


    public int[] sampleSolution(){
        int[] solution = new int[this.getModelSize()];
        // Inicializar los genes con una secuencia de 0 a modelSize - 1
        for (int i = 0; i < this.getModelSize(); i++){
            solution[i] = i;
        }
        // Mezclar aleatoriamente la secuencia para crear un recorrido inicial válido
        for (int i = 0; i < this.getModelSize(); i++){
            int indexToSwap = RANDOM.nextInt(this.getModelSize());
            int temp = solution[i];
            solution[i] = solution[indexToSwap];
            solution[indexToSwap] = temp;
        }
        return solution;
    }

    @Override
    public double solve(int[] solution) {
        double totalDistance = 0;
        for (int i = 0; i < solution.length - 1; i++) {
            if (solution[i] >= 0 && solution[i + 1] >= 0) { // Verificar índices válidos
                totalDistance += distances[solution[i]][solution[i + 1]];
            }
        }
        if (solution[solution.length - 1] >= 0 && solution[0] >= 0) { // Verificar índices válidos
            totalDistance += distances[solution[solution.length - 1]][solution[0]];
        }
        return totalDistance;
    }

    public AbstractProblem generateRandom(int n) {
        double[][] distances = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double distance = RANDOM.nextDouble() * 100; // Genera una distancia aleatoria entre 0 y 100
                distances[i][j] = distance;
                distances[j][i] = distance; // Asegura que la matriz sea simétrica
            }
        }
        return new TravelingSalesmanProblem(distances);
    }
}
