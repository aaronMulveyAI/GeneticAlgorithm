package org.example.OptimizationProblems.Modelling;

import org.example.OptimizationProblems.OptimizationMethod;
import org.example.OptimizationProblems.VisualModelling.RealValueOptimizationVisualization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class RealValueOptimizationProblem extends AbstractProblem {
    public static final int CHROMOSOME_LENGTH = 32;
    public static final double MIN_RANGE = -100;
    public static final double MAX_RANGE = 100;
    public static Function<Double, Double> objectiveFunction;

    private static final List<Function<Double, Double>> functions = new ArrayList<>();
    private static final Random RANDOM = new Random();

    static {
        functions.add(x -> (Math.sin(x) * ((x - 2) * (x - 2) + 3)) / x + 0.5);
    }

    public RealValueOptimizationProblem() {
        super(new RealValueOptimizationVisualization(),"Real Value Optimization Problem", OptimizationMethod.COMBINATORIAL, CHROMOSOME_LENGTH);
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

    @Override
    public AbstractProblem generateRandom(int n) {
        return new RealValueOptimizationProblem();
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

        return functions.get(RANDOM.nextInt(functions.size()));
    }


}
