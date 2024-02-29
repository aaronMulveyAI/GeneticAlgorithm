package org.example.OptimizationProblems.Modelling;

import org.example.OptimizationProblems.OptimizationMethod;
import org.example.OptimizationProblems.VisualModelling.NQueensVisualization;

import java.util.Random;

public class NQueensProblem extends AbstractProblem {
    public static int BOARD_SIZE;

    public NQueensProblem(){
        this(8);
    }
    public NQueensProblem(int n) {

        super(new NQueensVisualization(), n + "-Queens Problem", OptimizationMethod.COMBINATORIAL, n);
        BOARD_SIZE = n;
    }

    @Override
    public int[] sampleSolution() {
        int[] solution = new int[BOARD_SIZE];
        Random random = new Random();
        for (int i = 0; i < BOARD_SIZE; i++) {
            solution[i] = random.nextInt(BOARD_SIZE);
        }
        return solution;
    }

    @Override
    public double solve(int[] solution) {
        int clashes = 0;
        for (int i = 0; i < solution.length; i++) {
            for (int j = i + 1; j < solution.length; j++) {

                if (solution[i] == solution[j]) {
                    clashes++;
                }

                if (Math.abs(solution[i] - solution[j]) == Math.abs(i - j)) {
                    clashes++;
                }
            }
        }

        return (double) (BOARD_SIZE * (BOARD_SIZE - 1)) / 2 - clashes;
    }

    @Override
    public AbstractProblem generateRandom(int n) {
        return new NQueensProblem(n);
    }

}
