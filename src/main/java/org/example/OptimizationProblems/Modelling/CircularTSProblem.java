package org.example.OptimizationProblems.Modelling;

import org.example.OptimizationProblems.OptimizationMethod;
import org.example.OptimizationProblems.VisualModelling.CircularTSPVisualization;

import static org.example.GA.Constants.RANDOM;

public class CircularTSProblem extends AbstractProblem {
    public static double[][] distances;


    public CircularTSProblem() {
        this(10);
    }

    public CircularTSProblem(int numberOfCities) {
        super(new CircularTSPVisualization(), "Circular Traveling Salesman Problem", OptimizationMethod.PERMUTATION, numberOfCities);
        distances = generateCircularDistances(numberOfCities);
    }

    public int[] sampleSolution(){
        int[] solution = new int[this.getModelSize()];
        for (int i = 0; i < this.getModelSize(); i++){
            solution[i] = i;
        }

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
        totalDistance += distances[solution[solution.length - 1]][solution[0]];
        return totalDistance;
    }

    private static double[][] generateCircularDistances(int numberOfCities) {
        double[][] distances = new double[numberOfCities][numberOfCities];
        double angleIncrement = 2 * Math.PI / numberOfCities;

        for (int i = 0; i < numberOfCities; i++) {
            for (int j = i + 1; j < numberOfCities; j++) {
                double angle = angleIncrement * Math.abs(i - j);
                distances[i][j] = Math.sqrt(2 - 2 * Math.cos(angle));
                distances[j][i] = distances[i][j];
            }
        }

        return distances;
    }
    @Override
    public AbstractProblem generateRandom(int n) {
        return new CircularTSProblem(n);
    }
}
