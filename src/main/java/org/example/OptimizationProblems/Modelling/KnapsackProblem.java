package org.example.OptimizationProblems.Modelling;

import org.example.OptimizationProblems.OptimizationMethod;
import org.example.OptimizationProblems.VisualModelling.KnapsackVisualization;

import static org.example.GA.Constants.RANDOM;

public class KnapsackProblem extends AbstractProblem {
    public static int[] weights;
    public static int[] values;
    public static int maxWeight;
    public int getMaxWeight;

    public KnapsackProblem(){

        this(
                new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20},
                new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21},
                20
        );
    }
    public KnapsackProblem(int[] weights, int[] values, int maxWeight){
        super(new KnapsackVisualization(values.length), "Knapsack AbstractProblem", OptimizationMethod.COMBINATORIAL, weights.length);
        KnapsackProblem.weights = weights;
        KnapsackProblem.values = values;
        KnapsackProblem.maxWeight = maxWeight;
        getMaxWeight = maxWeight;
    }

    @Override
    public int[] sampleSolution(){
        int[] solution = new int[this.getModelSize()];
        for (int i = 0; i < this.getModelSize(); i++){
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

        return 0;
    }

    @Override
    public KnapsackProblem generateRandom(int n) {
        int[] weights = new int[n];
        int[] values = new int[n];
        int totalWeight = 0;


        for (int i = 0; i < n; i++) {
            weights[i] = RANDOM.nextInt(10) + 1;
            values[i] = RANDOM.nextInt(20) + 1;
            totalWeight += weights[i];
        }


        int maxWeight = (int) (totalWeight * 0.8);


        return new KnapsackProblem(weights, values, maxWeight);
    }
}
