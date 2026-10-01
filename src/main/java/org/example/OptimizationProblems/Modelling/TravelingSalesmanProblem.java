package org.example.OptimizationProblems.Modelling;

import org.example.OptimizationProblems.OptimizationMethod;
import org.example.OptimizationProblems.VisualModelling.TravellingSalesmanVisualization;
import org.example.GA.OPTIMIZATION_TYPE;

import static org.example.GA.Constants.RANDOM;

public class TravelingSalesmanProblem extends AbstractProblem {
    private final double[][] distances;

    public TravelingSalesmanProblem(){
        this(generateDistances(50));

    }

    public TravelingSalesmanProblem(double[][] distances) {
        super(new TravellingSalesmanVisualization(), "Traveling Salesman Problem", OptimizationMethod.PERMUTATION, distances.length);
        this.distances = new double[distances.length][];
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] == null || distances[i].length != distances.length) {
                throw new IllegalArgumentException("Distance matrix must be square");
            }
            for (double distance : distances[i]) {
                if (!Double.isFinite(distance) || distance < 0) {
                    throw new IllegalArgumentException("Distances must be finite and nonnegative");
                }
            }
            this.distances[i] = distances[i].clone();
        }
    }

    @Override
    public OPTIMIZATION_TYPE getOptimizationType() { return OPTIMIZATION_TYPE.MINIMIZE; }
    public double[][] getDistances() {
        return java.util.Arrays.stream(distances).map(double[]::clone).toArray(double[][]::new);
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
        validateSolution(solution);
        double totalDistance = 0;
        for (int i = 0; i < solution.length - 1; i++) {
            totalDistance += distances[solution[i]][solution[i + 1]];
        }
        totalDistance += distances[solution[solution.length - 1]][solution[0]];
        return totalDistance;
    }

    public AbstractProblem generateRandom(int n) {
        return new TravelingSalesmanProblem(generateDistances(n));
    }

    public static double[][] generateDistances(int n) {
        if (n < 1) throw new IllegalArgumentException("Problem size must be positive");

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
