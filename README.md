# Genetic Algorithm Framework

## Introduction
This project presents advanced software designed to solve complex problems through genetic algorithms. Inspired by principles of natural selection and genetics, this framework enables users to tackle optimization problems efficiently without delving into the intricacies of algorithm implementation.

![Description GIF 1](https://github.com/aaronMulveyAI/GeneticAlgorithm/blob/aaron/GA.gif?raw=true)


## Objectives
- **Generic Framework Development:** Provide a robust platform for implementing genetic algorithms.
- **Simplified Problem Modeling:** Allow users to focus on defining problems via a clear interface.
- **Essential Method Implementation:** Incorporate key functionalities:
  - `double solve(int[] solution)`: Evaluate a solution's fitness.
  - `AbstractProblem generateRandom(int n)`: Generate random problem instances.
  - `int[] sampleSolution()`: Initialize random solutions.
- **Showcase Versatility and Efficiency:** Demonstrate applicability across diverse optimization problems.

![Description GIF 2](https://github.com/aaronMulveyAI/GeneticAlgorithm/blob/aaron/csp.gif?raw=true)


## Methodology
The framework's design prioritizes abstraction and flexibility. Users customize key methods to adapt the genetic algorithm to specific problems, including:
- **Fitness Evaluation:** `solve(int[] solution)` defines how solutions are scored.
- **Random Problem Generation:** `generateRandom(int n)` creates problem instances.
- **Initial Solution Sampling:** `sampleSolution()` generates the starting population.

The framework supports a variety of problems, such as:
- **Traveling Salesman Problem (TSP):** Models routes as arrays representing city order.
- **Knapsack Problem (KP):** Uses binary encoding to represent item inclusion.
- **N-Queens Problem (NQ):** Represents column positions of queens on a chessboard.
- **Function Optimization (FO):** Encodes numbers for mathematical optimization.
- **Sequence Matching (SM):** Matches generated sequences against a target.

## Features
### Modular Architecture
- **AbstractProblem Class:** Facilitates problem-specific modeling.
- **iSelection and iReproduction Interfaces:** Modularize selection and crossover strategies.
- **Constants Class:** Allows fine-tuning algorithm parameters like mutation rates and population size.

![Description GIF 2](https://github.com/aaronMulveyAI/GeneticAlgorithm/blob/aaron/Nqueens.gif?raw=true)

### Genetic Operators
The framework includes tournament, truncation, roulette and residual selection, plus one-point, two-point and uniform crossover. Mutation respects each problem's domain: bit flips, digit/column replacement or permutation swaps.

### Graphical User Interface (GUI)
The intuitive GUI offers:
- Problem selection and random instance generation.
- Configurable selection, crossover methods, and parameters.
- Real-time visualization of algorithm progress, including:
  - Fitness evolution graph.
  - Population heat map.
  - Best chromosome visualization.
 
![Description GIF 2](https://github.com/aaronMulveyAI/GeneticAlgorithm/blob/aaron/number.gif?raw=true)

## Experimentation
The software enables rigorous testing of genetic algorithm configurations. Experimentation evaluates:
- **Selection Methods:** Tournament, truncation, and roulette selection.
- **Crossover Methods:** One-point, two-point, and uniform crossover.
- **Problem Scenarios:** TSP, KP, and others.

Each configuration undergoes multiple runs to assess convergence speed and solution quality.

## Results and Insights
Operator performance depends on the problem and its parameters. Run the corrected experiments before drawing conclusions about which configuration performs best. The reported best generation is the first generation that reached the best fitness observed during a run, not proof of convergence to the global optimum.

## Build and Test

Requires Java 17 or newer and Maven.

```sh
mvn test
mvn package
```

Tests run without a display by default. To also run the desktop workflow tests on a machine with a graphical session:

```sh
mvn -Djava.awt.headless=false test
```

Run `org.example.GUI.GeneticAlgorithmGUI` from your IDE to open the simulator.

## Algorithm Semantics

- Each problem declares its optimization direction and gene domain independently of its encoding.
- Knapsack and real-value chromosomes use bits; sequence matching uses digits; N-Queens uses board columns; TSP uses permutations.
- Crossover rate is the probability of crossing a pair of parents. Uniform crossover chooses each parent's genes with equal probability.
- Mutation rate applies per gene for combinatorial chromosomes and per chromosome for permutation swap mutation.
- Each algorithm captures its rates at construction. The legacy `Constants` rates supply defaults only.
- Problem data and chromosome arrays are copied to prevent changes in another instance from corrupting fitness. Fitness is cached until genes change.
- The algorithm uses generational replacement without elitism, so the best fitness of a generation can regress.
- Roulette and residual selection normalize finite fitness, support negative and equal scores, and favor the correct optimization direction.

The core still uses a shared random generator and problem classes create Swing visualizations. A future web version should separate those visualizations and provide a random generator per execution for reproducible concurrent runs.

## Conclusion
This framework serves as a powerful tool for exploring and optimizing genetic algorithms. Its adaptability, combined with the GUI's experimentation capabilities, provides a comprehensive platform for solving a wide range of optimization problems.

