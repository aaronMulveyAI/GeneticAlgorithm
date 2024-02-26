package org.example.OptimizationProblems;

import org.example.OptimizationProblems.Modelling.OptimizationMethod;

import static org.example.GA.Constants.RANDOM;

public class TravelingSalesmanProblem extends AbstractProblem {
    public static double[][] distances;

    public TravelingSalesmanProblem(){
        super("Traveling Salesman Problem", OptimizationMethod.PERMUTATION, 0, 0);
        distances = new double[][]{
                {0, 1, 2, 3, 4},
                {1, 0, 1, 2, 3},
                {2, 1, 0, 1, 2},
                {3, 2, 1, 0, 1},
                {4, 3, 2, 1, 0}
        };
    }

    public TravelingSalesmanProblem(double[][] distances) {
        super("Traveling Salesman Problem", OptimizationMethod.PERMUTATION, distances.length, distances.length);
        TravelingSalesmanProblem.distances = distances;
    }

    public int[] sampleSolution(){
        int[] solution = new int[modelSize];
        // Inicializar los genes con una secuencia de 0 a modelSize - 1
        for (int i = 0; i < modelSize; i++){
            solution[i] = i;
        }
        // Mezclar aleatoriamente la secuencia para crear un recorrido inicial válido
        for (int i = 0; i < modelSize; i++){
            int indexToSwap = RANDOM.nextInt(modelSize);
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

}
