package org.example.OptimizationProblems;

import java.util.Random;

public class NQueensProblem extends AbstractProblem {
    public static int BOARD_SIZE;

    public NQueensProblem(){
        this(8);
    }
    public NQueensProblem(int n) {

        super(n + "-Queens Problem", OptimizationMethod.COMBINATORIAL, n, n);
        BOARD_SIZE = n;
    }

    @Override
    public int[] sampleSolution() {
        int[] solution = new int[BOARD_SIZE];
        Random random = new Random();
        for (int i = 0; i < BOARD_SIZE; i++) {
            solution[i] = random.nextInt(BOARD_SIZE); // Posición de la reina en la fila i
        }
        return solution;
    }

    @Override
    public double solve(int[] solution) {
        int clashes = 0;
        for (int i = 0; i < solution.length; i++) {
            for (int j = i + 1; j < solution.length; j++) {
                // Comprobar si las reinas se atacan en la misma columna
                if (solution[i] == solution[j]) {
                    clashes++;
                }
                // Comprobar si las reinas se atacan en diagonales
                if (Math.abs(solution[i] - solution[j]) == Math.abs(i - j)) {
                    clashes++;
                }
            }
        }
        // La solución ideal tiene 0 choques, pero para hacerlo compatible con la función de fitness,
        // donde más alto es mejor, restamos los choques del número total de pares de reinas
        return (double) (BOARD_SIZE * (BOARD_SIZE - 1)) / 2 - clashes;
    }


    public static NQueensProblem generateRandom(int n) {
        return new NQueensProblem(n);
    }

}
