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
The framework includes several selection methods (e.g., tournament, truncation) and crossover strategies (e.g., one-point, uniform). Although it currently implements bit-flip mutation, the design allows for additional mutations.

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
Experiments highlight the importance of selecting the right configurations based on problem characteristics. For instance:
- Uniform crossover with tournament selection excels in TSP.
- Truncation selection outperforms in KP for faster convergence.

## Conclusion
This framework serves as a powerful tool for exploring and optimizing genetic algorithms. Its adaptability, combined with the GUI's experimentation capabilities, provides a comprehensive platform for solving a wide range of optimization problems.



