package org.example.OptimizationProblems;

import org.example.OptimizationProblems.Modelling.OptimizationMethod;

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
                50
        );
    }
    public KnapsackProblem(int[] weights, int[] values, int maxWeight){
        super("Knapsack AbstractProblem", OptimizationMethod.COMBINATORIAL, weights.length, 2);
        KnapsackProblem.weights = weights;
        KnapsackProblem.values = values;
        KnapsackProblem.maxWeight = maxWeight;
        getMaxWeight = maxWeight;
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

        return 0;
    }

    public static KnapsackProblem generateRandom(int n) {
        int[] weights = new int[n];
        int[] values = new int[n];
        int totalWeight = 0;

        // Generar pesos y valores aleatorios para cada ítem
        for (int i = 0; i < n; i++) {
            weights[i] = RANDOM.nextInt(10) + 1; // Genera pesos en el rango [1, 10]
            values[i] = RANDOM.nextInt(20) + 1; // Genera valores en el rango [1, 20]
            totalWeight += weights[i];
        }

        // Calcular la capacidad máxima de la mochila como el 80% del peso total
        int maxWeight = (int) (totalWeight * 0.8);

        // Crear una nueva instancia de KnapsackProblem con los datos generados
        return new KnapsackProblem(weights, values, maxWeight);
    }
}
