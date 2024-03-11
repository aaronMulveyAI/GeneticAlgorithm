package org.example.OptimizationProblems.Modelling;

import org.example.OptimizationProblems.OptimizationMethod;
import org.example.OptimizationProblems.VisualModelling.TravellingSalesmanVisualization;

import static org.example.GA.Constants.RANDOM;

public class TravelingSalesmanProblem extends AbstractProblem {
    public static double[][] distances;

    public TravelingSalesmanProblem(){
        this(generateDistances(50));

    }

    public TravelingSalesmanProblem(double[][] distances) {
        super(new TravellingSalesmanVisualization(), "Traveling Salesman Problem", OptimizationMethod.PERMUTATION, distances.length);
        TravelingSalesmanProblem.distances = distances;
    }


    public int[] sampleSolution(){
        int[] solution = new int[this.getModelSize()];

        for (int i = 0; i < this.getModelSize(); i++){
            solution[i] = i;
        }

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
            if (solution[i] >= 0 && solution[i + 1] >= 0) {
                totalDistance += distances[solution[i]][solution[i + 1]];
            }
        }
        if (solution[solution.length - 1] >= 0 && solution[0] >= 0) {
            totalDistance += distances[solution[solution.length - 1]][solution[0]];
        }
        return totalDistance;
    }

    public AbstractProblem generateRandom(int n) {
        return new TravelingSalesmanProblem(generateDistances(n));
    }

    public static double[][] generateDistances(int n) {

        double[][] distances = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double distance = RANDOM.nextDouble() * 100;
                distances[i][j] = distance;
                distances[j][i] = distance;
            }
        }
        return distances;
    }
}
