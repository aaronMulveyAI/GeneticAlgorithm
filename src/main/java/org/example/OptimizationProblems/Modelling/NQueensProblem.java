package org.example.OptimizationProblems.Modelling;

import org.example.OptimizationProblems.OptimizationMethod;
import org.example.OptimizationProblems.VisualModelling.NQueensVisualization;

import static org.example.GA.Constants.RANDOM;

public class NQueensProblem extends AbstractProblem {

    public NQueensProblem(){
        this(8);
    }
    public NQueensProblem(int n) {

        super(new NQueensVisualization(), n + "-Queens Problem", OptimizationMethod.COMBINATORIAL, n);
    }

    @Override
    public int[] sampleSolution() {
        int[] solution = new int[getModelSize()];
        for (int i = 0; i < solution.length; i++) {
            solution[i] = RANDOM.nextInt(getModelSize());
        }
        return solution;
    }

    @Override
    public double solve(int[] solution) {
        validateSolution(solution);
        long clashes = 0;
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

        return (double) getModelSize() * (getModelSize() - 1) / 2 - clashes;
    }

    @Override
    public AbstractProblem generateRandom(int n) {
        return new NQueensProblem(n);
    }

}
