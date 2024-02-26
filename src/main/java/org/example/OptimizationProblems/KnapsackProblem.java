package org.example.OptimizationProblems;

import org.example.OptimizationProblems.Modelling.OptimizationMethod;

import static org.example.GA.Constants.RANDOM;

public class KnapsackProblem extends AbstractProblem {
    public static int[] weights;
    public static int[] values;
    public static int maxWeight;

    public KnapsackProblem(){
        super("Knapsack AbstractProblem", OptimizationMethod.COMBINATORIAL, 0, 0);
        weights = new int[]{1, 2, 3, 4, 5};
        values = new int[]{1, 2, 3, 4, 5};
        maxWeight = 10;
    }
    public KnapsackProblem(int[] weights, int[] values, int maxWeight){
        super("Knapsack AbstractProblem", OptimizationMethod.COMBINATORIAL, weights.length, 2);
        KnapsackProblem.weights = weights;
        KnapsackProblem.values = values;
        KnapsackProblem.maxWeight = maxWeight;
    }

    @Override
    public int[] sampleSolution(){
        int[] solution = new int[modelSize];
        for (int i = 0; i < modelSize; i++){
            solution[i] = RANDOM.nextInt(2);
        }
        return solution;
    }

    @Override
    public double solve(int[] solution) {
        int weight = 0;
        int value = 0;
        for (int i = 0; i < solution.length; i++) {
            if(solution[i] == 1){
                weight += weights[i];
                value += values[i];
            }
        }
        if(weight <= maxWeight){
            return value;
        }
        return Integer.MIN_VALUE;
    }
}
