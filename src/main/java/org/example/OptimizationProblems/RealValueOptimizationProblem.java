package org.example.OptimizationProblems;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class RealValueOptimizationProblem extends AbstractProblem {
    public static final int CHROMOSOME_LENGTH = 32; // Longitud del cromosoma para representar números reales
    public static final double MIN_RANGE = -100; // Límite inferior del rango de valores reales
    public static final double MAX_RANGE = 100; // Límite superior del rango de valores reales
    public static Function<Double, Double> objectiveFunction; // Función objetivo a optimizar

    private static final List<Function<Double, Double>> functions = new ArrayList<>();
    private static final Random RANDOM = new Random();

    static {
        // Inicializar el pool de funciones objetivo
        functions.add(x -> (Math.sin(x) * ((x - 2) * (x - 2) + 3)) / x + 0.5);

        // Añadir más funciones según sea necesario
    }

    public RealValueOptimizationProblem() {
        super("Real Value Optimization Problem", OptimizationMethod.COMBINATORIAL, CHROMOSOME_LENGTH, 2);
        objectiveFunction = selectRandomFunction();
    }

    @Override
    public int[] sampleSolution() {
        int[] solution = new int[CHROMOSOME_LENGTH];
        for (int i = 0; i < CHROMOSOME_LENGTH; i++) {
            solution[i] = RANDOM.nextInt(2);
        }
        return solution;
    }

    @Override
    public double solve(int[] solution) {
        double realValue = binaryToReal(solution);
        return objectiveFunction.apply(realValue);
    }

    private double binaryToReal(int[] binary) {
        long value = 0;
        for (int i = 0; i < CHROMOSOME_LENGTH; i++) {
            if (binary[i] == 1) {
                value += (long) Math.pow(2, CHROMOSOME_LENGTH - i - 1);
            }
        }
        return MIN_RANGE + ((MAX_RANGE - MIN_RANGE) / (Math.pow(2, CHROMOSOME_LENGTH) - 1)) * value;
    }

    private Function<Double, Double> selectRandomFunction() {
        // Selecciona y devuelve una función aleatoria del pool
        return functions.get(RANDOM.nextInt(functions.size()));
    }

    public static RealValueOptimizationProblem generateRandom() {
        // Crear una nueva instancia de KnapsackProblem con los datos generados
        return new RealValueOptimizationProblem();
    }
}
