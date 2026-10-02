import { describe, expect, it } from 'vitest';
import seedrandom from 'seedrandom';
import { DEFAULT_CONFIG, PROBLEMS, readConfig, shareUrl, validateConfig } from './config';
import { binaryToReal, createProblem, cross, evaluate, GeneticEngine, MAX_GENERATIONS, mutate, objective, selectionWeights, validateGenes } from './engine';
import type { Config, Crossover, ProblemId, Selection } from './types';

describe('Correspondencia con los problemas Java', () => {
  it('puntúa tableros válidos y conflictos conocidos', () => {
    const problem = createProblem({ ...DEFAULT_CONFIG, size: 4 }, seedrandom('fixture'));
    expect(evaluate(problem, [1, 3, 0, 2])).toBe(6);
    expect(evaluate(problem, [0, 0, 0, 0])).toBe(0);
  });
  it('incluye la arista que cierra la ruta y rechaza duplicados', () => {
    const problem = createProblem({ ...DEFAULT_CONFIG, problem: 'tsp', size: 4 }, seedrandom('fixture'));
    problem.points = [{ x: 0, y: 0 }, { x: 10, y: 0 }, { x: 10, y: 10 }, { x: 0, y: 10 }];
    expect(evaluate(problem, [0, 1, 2, 3])).toBe(40);
    expect(() => evaluate(problem, [0, 0, 2, 3])).toThrow();
  });
  it('respeta la capacidad de la mochila y el dominio binario', () => {
    const problem = createProblem({ ...DEFAULT_CONFIG, problem: 'knapsack', size: 3 }, seedrandom('fixture'));
    problem.weights = [1, 2, 3]; problem.values = [4, 5, 6]; problem.capacity = 3;
    expect(evaluate(problem, [1, 1, 0])).toBe(9);
    expect(evaluate(problem, [1, 1, 1])).toBe(0);
    expect(() => evaluate(problem, [2, 0, 0])).toThrow();
  });
  it('puntúa las coincidencias de la secuencia', () => {
    const problem = createProblem({ ...DEFAULT_CONFIG, problem: 'sequence', size: 3 }, seedrandom('fixture'));
    problem.target = [4, 5, 6];
    expect(evaluate(problem, [4, 0, 6])).toBe(2);
  });
  it('decodifica los extremos de 32 bits y elimina la singularidad en cero', () => {
    expect(binaryToReal(Array(32).fill(0))).toBe(-100);
    expect(binaryToReal(Array(32).fill(1))).toBe(100);
    expect(objective(0)).toBe(7.5);
  });
});

describe('Operadores y simulaciones', () => {
  const crossovers: Crossover[] = ['single', 'double', 'uniform'];
  const selections: Selection[] = ['tournament', 'roulette', 'truncation', 'residual'];
  for (const id of Object.keys(PROBLEMS) as ProblemId[]) {
    for (const crossover of crossovers) for (const selection of selections) {
      it(`${id} / ${selection} / ${crossover}: cromosomas válidos y mejor histórico`, () => {
        const config: Config = { ...DEFAULT_CONFIG, problem: id, size: PROBLEMS[id].defaultSize,
          populationSize: 20, crossover, selection };
        const engine = new GeneticEngine(config);
        const before = engine.snapshot();
        const parents = structuredClone(before.population);
        engine.evolve(25);
        const after = engine.snapshot();
        expect(after.generation).toBe(25);
        expect(after.population).toHaveLength(20);
        expect(after.diversity).toBeGreaterThan(0);
        expect(after.diversity).toBeLessThanOrEqual(100);
        expect(before.population).toEqual(parents);
        for (const individual of after.population) {
          validateGenes(engine.problem, individual.genes);
          expect(Number.isFinite(individual.fitness)).toBe(true);
        }
        expect(after.problem.minimize ? after.best.fitness <= before.best.fitness
          : after.best.fitness >= before.best.fitness).toBe(true);
      });
    }
  }
  for (const crossover of crossovers) {
    it(`${crossover}: cruces de permutación sin duplicados`, () => {
      const problem = createProblem({ ...DEFAULT_CONFIG, problem: 'circular', size: 8 }, seedrandom('fixture'));
      const random = seedrandom('cross');
      for (let i = 0; i < 100; i++) {
        expect(new Set(cross(problem, [0, 1, 2, 3, 4, 5, 6, 7], [7, 6, 5, 4, 3, 2, 1, 0], crossover, random)).size).toBe(8);
      }
    });
  }
  it('mantiene la semilla y la independencia de las ejecuciones', () => {
    const first = new GeneticEngine(DEFAULT_CONFIG);
    const second = new GeneticEngine(DEFAULT_CONFIG);
    first.evolve(15);
    new GeneticEngine({ ...DEFAULT_CONFIG, problem: 'sequence', size: 6 }).evolve(7);
    second.evolve(15);
    expect(first.snapshot()).toEqual(second.snapshot());
  });
  it('permite conservar el mejor individuo sin alterar la población anterior', () => {
    const engine = new GeneticEngine({ ...DEFAULT_CONFIG, elitism: true, mutationRate: 1 });
    const before = engine.snapshot().current.fitness;
    engine.evolve(10);
    expect(engine.snapshot().current.fitness).toBeGreaterThanOrEqual(before);
  });
  it('muta bits, dígitos y columnas en sus dominios, incluidos tamaños pequeños', () => {
    for (const [id, size] of [['queens', 1], ['sequence', 1], ['sequence', 40], ['knapsack', 3], ['function', 32]] as const) {
      const problem = createProblem({ ...DEFAULT_CONFIG, problem: id, size }, seedrandom('fixture'));
      const genes = Array(size).fill(0);
      mutate(problem, genes, 1, seedrandom('mutation'));
      validateGenes(problem, genes);
      expect(genes.every(gene => problem.domain === 1 ? gene === 0 : gene > 0)).toBe(true);
    }
  });
  it('normaliza fitness cero, negativo y de magnitud extrema', () => {
    for (const scores of [[0, 0], [-9, -1], [1e300, 3e300]]) {
      const population = scores.map(fitness => ({ genes: [0], fitness }));
      const maximizing = selectionWeights(population, false);
      expect(maximizing.every(weight => Number.isFinite(weight) && weight > 0)).toBe(true);
      expect(maximizing[1]).toBeGreaterThanOrEqual(maximizing[0]);
      const minimizing = selectionWeights(population, true);
      expect(minimizing[0]).toBeGreaterThanOrEqual(minimizing[1]);
    }
  });
  it('limita las generaciones y el historial sin perder la referencia inicial', () => {
    const engine = new GeneticEngine({ ...DEFAULT_CONFIG, size: 1, populationSize: 2 });
    for (let i = 0; i < 51; i++) engine.evolve(100);
    const snapshot = engine.snapshot();
    expect(snapshot.generation).toBe(MAX_GENERATIONS);
    expect(snapshot.status).toBe('completed');
    expect(snapshot.history).toHaveLength(1001);
    expect(snapshot.history[0].generation).toBe(0);
  });
});

describe('Configuración compartida y validación', () => {
  it('comparte y recupera todos los parámetros mediante la URL', () => {
    const config = { ...DEFAULT_CONFIG, seed: 987, elitism: true, selection: 'residual' as const };
    const url = shareUrl(config, { origin: 'https://demo.vercel.app', pathname: '/' });
    expect(readConfig(new URL(url).search)).toEqual(config);
  });
  it('rechaza enlaces incorrectos y parámetros fuera de los límites', () => {
    expect(readConfig('?config=null')).toEqual(DEFAULT_CONFIG);
    expect(readConfig('?config=%7B')).toEqual(DEFAULT_CONFIG);
    expect(readConfig('?config={"seed":-1}')).toEqual(DEFAULT_CONFIG);
    expect(readConfig('?config={"problem":"__proto__"}')).toEqual(DEFAULT_CONFIG);
    for (const invalid of [{ size: 0 }, { mutationRate: NaN }, { crossoverRate: 2 }, { seed: -1 }, { populationSize: 501 }]) {
      expect(() => validateConfig({ ...DEFAULT_CONFIG, ...invalid })).toThrow();
    }
  });
});
