package org.example.OptimizationProblems;

import static org.example.GA.Constants.RANDOM;

public class TravelingSalesmanProblem extends AbstractProblem {
    public static double[][] distances;


    public TravelingSalesmanProblem(){
        this(
                new double[][]{
                        {0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19},
                        {1,   0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18},
                        {2,   1,   0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17},
                        {3,   2,   1,   0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16},
                        {4,   3,   2,   1,   0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15},
                        {5,   4,   3,   2,   1,   0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14},
                        {6,   5,   4,   3,   2,   1,   0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13},
                        {7,   6,   5,   4,   3,   2,   1,   0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12},
                        {8,   7,   6,   5,   4,   3,   2,   1,   0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11},
                        {9,   8,   7,   6,   5,   4,   3,   2,   1,   0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10},
                        {10,  9,   8,   7,   6,   5,   4,   3,   2,   1,   0,   1,   2,   3,   4,   5,   6,   7,   8,   9},
                        {11, 10,   9,   8,   7,   6,   5,   4,   3,   2,   1,   0,   1,   2,   3,   4,   5,   6,   7,   8},
                        {12, 11,  10,   9,   8,   7,   6,   5,   4,   3,   2,   1,   0,   1,   2,   3,   4,   5,   6,   7},
                        {13, 12,  11,  10,   9,   8,   7,   6,   5,   4,   3,   2,   1,   0,   1,   2,   3,   4,   5,   6},
                        {14, 13,  12,  11,  10,   9,   8,   7,   6,   5,   4,   3,   2,   1,   0,   1,   2,   3,   4,   5},
                        {15, 14,  13,  12,  11,  10,   9,   8,   7,   6,   5,   4,   3,   2,   1,   0,   1,   2,   3,   4},
                        {16, 15,  14,  13,  12,  11,  10,   9,   8,   7,   6,   5,   4,   3,   2,   1,   0,   1,   2,   3},
                        {17, 16,  15,  14,  13,  12,  11,  10,   9,   8,   7,   6,   5,   4,   3,   2,   1,   0,   1,   2},
                        {18, 17,  16,  15,  14,  13,  12,  11,  10,   9,   8,   7,   6,   5,   4,   3,   2,   1,   0,   1},
                        {19, 18,  17,  16,  15,  14,  13,  12,  11,  10,   9,   8,   7,   6,   5,   4,   3,   2,   1,   0}
                }
        );
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

    public static TravelingSalesmanProblem generateRandom(int n) {
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
