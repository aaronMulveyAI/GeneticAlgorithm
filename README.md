## Introduction
This project presents advanced software developed to solve complex problems through genetic algorithms. Inspired by natural selection and genetics, our software provides a generic framework that allows users to tackle optimization problems efficiently without delving into the complexities of algorithm implementation.

![hola](https://private-user-images.githubusercontent.com/152191859/311836375-f7c2f24f-95f2-44c4-a1ce-a06a1ec3910c.gif?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3MTAxODQ2ODYsIm5iZiI6MTcxMDE4NDM4NiwicGF0aCI6Ii8xNTIxOTE4NTkvMzExODM2Mzc1LWY3YzJmMjRmLTk1ZjItNDRjNC1hMWNlLWEwNmExZWMzOTEwYy5naWY_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BS0lBVkNPRFlMU0E1M1BRSzRaQSUyRjIwMjQwMzExJTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI0MDMxMVQxOTEzMDZaJlgtQW16LUV4cGlyZXM9MzAwJlgtQW16LVNpZ25hdHVyZT1iYmQyZGRjMzMxNWE1ZDY1YmQ2NjI2MmZiYzA5N2QwNGJjZGEwOWE5MzdmZGRkODE2Y2YyYWEyYTYzOTEzY2E4JlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCZhY3Rvcl9pZD0wJmtleV9pZD0wJnJlcG9faWQ9MCJ9.OkgwRdKFzpd2TICGgiGXLWa1U3qcWXDZWxVcAaYryLw)

## Objectives
- Develop a Generic Genetic Algorithm framework.
- Facilitate problem modeling through a clear and concise interface.
- Implement essential methods crucial for evaluating solutions, generating problems, and creating random solutions.
- Demonstrate versatility and efficiency across a variety of optimization scenarios.

## Methodology
Our approach focuses on abstraction and flexibility, enabling the adaptation of the genetic algorithm to specific problems through the implementation of key methods:
- `double solve(int[] solution)`: Evaluates the fitness of a solution.
- `AbstractProblem generateRandom(int n)`: Generates random problem instances.
- `int[] sampleSolution()`: Generates random solutions for initialization.

