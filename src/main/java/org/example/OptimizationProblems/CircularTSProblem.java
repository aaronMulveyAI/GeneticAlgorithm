package org.example.OptimizationProblems;

import static org.example.GA.Constants.RANDOM;

public class CircularTSProblem extends AbstractProblem {
    public static double[][] distances;


    public CircularTSProblem() {
        this(10);
    }

    public CircularTSProblem(int numberOfCities) {
        super("Circular Traveling Salesman Problem", OptimizationMethod.PERMUTATION, numberOfCities, numberOfCities);
        distances = generateCircularDistances(numberOfCities);
    }

    public int[] sampleSolution(){
        int[] solution = new int[modelSize];
        for (int i = 0; i < modelSize; i++){
            solution[i] = i;
        }
        // Mezcla aleatoriamente la secuencia para crear un recorrido inicial válido
        for (int i = solution.length - 1; i > 0; i--){
            int indexToSwap = RANDOM.nextInt(i + 1);
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
            totalDistance += distances[solution[i]][solution[i + 1]];
        }
        totalDistance += distances[solution[solution.length - 1]][solution[0]]; // Completa el ciclo volviendo al punto inicial
        return totalDistance;
    }

    private static double[][] generateCircularDistances(int numberOfCities) {
        double[][] distances = new double[numberOfCities][numberOfCities];
        double angleIncrement = 2 * Math.PI / numberOfCities;

        for (int i = 0; i < numberOfCities; i++) {
            for (int j = i + 1; j < numberOfCities; j++) {
                double angle = angleIncrement * Math.abs(i - j);
                distances[i][j] = Math.sqrt(2 - 2 * Math.cos(angle));
                distances[j][i] = distances[i][j]; // La matriz es simétrica
            }
        }

        return distances;
    }

    public static CircularTSProblem generateRandom(int n) {
        return new CircularTSProblem(n);
    }
}
