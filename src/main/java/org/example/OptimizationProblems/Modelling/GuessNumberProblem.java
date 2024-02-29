package org.example.OptimizationProblems.Modelling;

import org.example.OptimizationProblems.OptimizationMethod;
import org.example.OptimizationProblems.VisualModelling.GuessNumberVisualization;

import static org.example.GA.Constants.RANDOM;

public class GuessNumberProblem extends AbstractProblem {
    public static int[] targetSequence;
    public static int sequenceLength;

    public GuessNumberProblem() {
        this(10);
    }
    public GuessNumberProblem(int sequenceLength) {
        super(new GuessNumberVisualization(), "Guess Number Problem", OptimizationMethod.COMBINATORIAL, sequenceLength); // Asumimos un rango de 0 a 9 para cada número en la secuencia
        GuessNumberProblem.sequenceLength = sequenceLength;
        GuessNumberProblem.targetSequence = sampleSolution();
    }

    @Override
    public int[] sampleSolution() {
        int[] solution = new int[sequenceLength];
        for (int i = 0; i < sequenceLength; i++) {
            solution[i] = RANDOM.nextInt(10);
        }
        return solution;
    }

    @Override
    public double solve(int[] solution) {
        int matchScore = 0;
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == targetSequence[i]) {
                matchScore += 1;
            }
        }
        return matchScore;
    }

    @Override
    public AbstractProblem generateRandom(int n) {
        return new GuessNumberProblem(n);
    }
}

