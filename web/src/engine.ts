import seedrandom from 'seedrandom';
import { validateConfig } from './config';
import { createWorld, DIRECTIONS, rocketFitness } from './rockets';
import type { Config, Crossover, Discovery, HistoryPoint, Individual, Problem, Snapshot, Status } from './types';

export const MAX_GENERATIONS = 5000;
type Random = () => number;
const integer = (random: Random, bound: number) => Math.floor(random() * bound);

export function createProblem(config: Config, random: Random): Problem {
  const permutation = config.problem === 'tsp' || config.problem === 'circular';
  const problem: Problem = {
    id: config.problem, size: config.size, permutation, minimize: permutation,
    domain: permutation || config.problem === 'queens' ? config.size : config.problem === 'sequence' ? 10
      : config.problem === 'rockets' ? DIRECTIONS : 2,
    points: [], target: [], weights: [], values: [], capacity: 0,
  };
  for (let i = 0; i < config.size; i++) {
    if (config.problem === 'tsp') problem.points.push({ x: 8 + random() * 84, y: 8 + random() * 84 });
    if (config.problem === 'circular') {
      const angle = 2 * Math.PI * i / config.size;
      problem.points.push({ x: 50 + 40 * Math.cos(angle), y: 50 + 40 * Math.sin(angle) });
    }
    if (config.problem === 'sequence') problem.target.push(integer(random, 10));
    if (config.problem === 'knapsack') {
      problem.weights.push(integer(random, 10) + 1);
      problem.values.push(integer(random, 20) + 1);
    }
  }
  problem.capacity = Math.floor(problem.weights.reduce((sum, weight) => sum + weight, 0) * 0.8);
  if (config.problem === 'rockets') problem.world = createWorld(config.scenario, random);
  return problem;
}

export function validateGenes(problem: Problem, genes: number[]): void {
  if (genes.length !== problem.size || genes.some(gene => !Number.isInteger(gene) || gene < 0 || gene >= problem.domain)
      || (problem.permutation && new Set(genes).size !== genes.length)) {
    throw new Error('Cromosoma no válido para este problema.');
  }
}

export function binaryToReal(genes: number[]): number {
  return -100 + 200 * genes.reduce((value, gene) => value * 2 + gene, 0) / (2 ** genes.length - 1);
}

export function objective(x: number): number {
  return (x === 0 ? 1 : Math.sin(x) / x) * ((x - 2) ** 2 + 3) + 0.5;
}

export function evaluate(problem: Problem, genes: number[]): number {
  validateGenes(problem, genes);
  switch (problem.id) {
    case 'tsp':
    case 'circular':
      return genes.reduce((distance, city, index) => {
        const next = problem.points[genes[(index + 1) % genes.length]];
        return distance + Math.hypot(problem.points[city].x - next.x, problem.points[city].y - next.y);
      }, 0);
    case 'queens': {
      let clashes = 0;
      for (let i = 0; i < genes.length; i++) {
        for (let j = i + 1; j < genes.length; j++) {
          if (genes[i] === genes[j] || Math.abs(genes[i] - genes[j]) === j - i) clashes++;
        }
      }
      return genes.length * (genes.length - 1) / 2 - clashes;
    }
    case 'knapsack': {
      const weight = genes.reduce((sum, selected, index) => sum + selected * problem.weights[index], 0);
      return weight > problem.capacity ? 0 : genes.reduce((sum, selected, index) => sum + selected * problem.values[index], 0);
    }
    case 'sequence':
      return genes.reduce((sum, gene, index) => sum + Number(gene === problem.target[index]), 0);
    case 'function':
      return objective(binaryToReal(genes));
    case 'rockets':
      return rocketFitness(problem.world!, genes);
  }
}

function sample(problem: Problem, random: Random): number[] {
  const genes = Array.from({ length: problem.size }, (_, index) =>
    problem.permutation ? index : integer(random, problem.domain));
  if (problem.permutation) {
    for (let i = genes.length - 1; i > 0; i--) {
      const j = integer(random, i + 1);
      [genes[i], genes[j]] = [genes[j], genes[i]];
    }
  }
  return genes;
}

export function cross(problem: Problem, father: number[], mother: number[], method: Crossover, random: Random): number[] {
  const length = father.length;
  let start = 0;
  let end = method === 'single' ? (length === 1 ? 1 : integer(random, length - 1) + 1) : 0;
  if (method === 'double') {
    start = integer(random, length + 1);
    end = integer(random, length);
    if (end >= start) end++;
    [start, end] = [Math.min(start, end), Math.max(start, end)];
  }
  if (!problem.permutation) {
    return father.map((gene, index) => method === 'uniform' ? (random() < 0.5 ? gene : mother[index])
      : method === 'single' ? (index < end ? gene : mother[index])
      : (index >= start && index < end ? gene : mother[index]));
  }
  const genes = Array<number>(length).fill(-1);
  const taken = new Set<number>();
  for (let i = 0; i < length; i++) {
    if (method === 'uniform' ? random() < 0.5 : i >= start && i < end) {
      genes[i] = father[i];
      taken.add(father[i]);
    }
  }
  let position = method === 'uniform' ? 0 : end % length;
  for (let i = 0; i < length; i++) {
    const gene = mother[method === 'uniform' ? i : (end + i) % length];
    if (!taken.has(gene)) {
      while (genes[position] !== -1) position = (position + 1) % length;
      genes[position] = gene;
      taken.add(gene);
    }
  }
  return genes;
}

export function mutate(problem: Problem, genes: number[], rate: number, random: Random): void {
  if (problem.permutation) {
    if (genes.length > 1 && random() < rate) {
      const first = integer(random, genes.length);
      let second = integer(random, genes.length - 1);
      if (second >= first) second++;
      [genes[first], genes[second]] = [genes[second], genes[first]];
    }
  } else if (problem.domain > 1) {
    for (let i = 0; i < genes.length; i++) {
      if (random() < rate) {
        const replacement = integer(random, problem.domain - 1);
        genes[i] = replacement >= genes[i] ? replacement + 1 : replacement;
      }
    }
  }
}

export function selectionWeights(population: Individual[], minimize: boolean): number[] {
  const fitness = population.map(individual => individual.fitness);
  const scale = Math.max(...fitness.map(Math.abs));
  if (scale === 0) return fitness.map(() => 1);
  const scaled = fitness.map(value => value / scale);
  const minimum = Math.min(...scaled);
  const maximum = Math.max(...scaled);
  if (minimum === maximum) return fitness.map(() => 1);
  return scaled.map(value => (minimize ? maximum - value : value - Math.min(minimum, 0)) + 1e-12);
}

function weightedPicker(weights: number[], random: Random): () => number {
  let total = 0;
  const cumulative = weights.map(weight => (total += weight));
  return () => {
    const target = random() * total;
    let low = 0;
    let high = cumulative.length - 1;
    while (low < high) {
      const middle = Math.floor((low + high) / 2);
      if (target < cumulative[middle]) high = middle;
      else low = middle + 1;
    }
    return low;
  };
}

export class GeneticEngine {
  readonly config: Config;
  readonly problem: Problem;
  private readonly random: Random;
  private population: Individual[];
  private generation = 0;
  private best: Individual;
  private readonly initialFitness: number;
  private history: HistoryPoint[] = [];
  private discoveries: Discovery[] = [];

  constructor(config: Config) {
    validateConfig(config);
    this.config = { ...config };
    this.random = seedrandom(String(config.seed));
    this.problem = createProblem(config, this.random);
    this.population = Array.from({ length: config.populationSize }, () => this.individual(sample(this.problem, this.random)));
    this.best = this.currentBest();
    this.initialFitness = this.best.fitness;
    this.record();
  }

  private individual(genes: number[]): Individual {
    return { genes, fitness: evaluate(this.problem, genes) };
  }

  private better(candidate: number, reference: number): boolean {
    return this.problem.minimize ? candidate < reference : candidate > reference;
  }

  private currentBest(): Individual {
    return this.population.reduce((best, candidate) => this.better(candidate.fitness, best.fitness) ? candidate : best);
  }

  private selector(): () => Individual {
    const population = this.population;
    if (this.config.selection === 'tournament') {
      return () => {
        let best = population[integer(this.random, population.length)];
        for (let i = 1; i < this.config.tournamentSize; i++) {
          const candidate = population[integer(this.random, population.length)];
          if (this.better(candidate.fitness, best.fitness)) best = candidate;
        }
        return best;
      };
    }
    if (this.config.selection === 'truncation') {
      const sorted = [...population].sort((a, b) => this.problem.minimize ? a.fitness - b.fitness : b.fitness - a.fitness);
      const count = Math.max(1, Math.floor(population.length / 2));
      return () => sorted[integer(this.random, count)];
    }
    const weights = selectionWeights(population, this.problem.minimize);
    const pick = weightedPicker(weights, this.random);
    if (this.config.selection === 'roulette') return () => population[pick()];
    const total = weights.reduce((sum, value) => sum + value, 0);
    const expected = weights.map(weight => weight / total * population.length);
    const integral = expected.map(Math.floor);
    const remaining = expected.map((value, index) => value - integral[index]);
    const integralTotal = integral.reduce((sum, value) => sum + value, 0);
    const pickIntegral = weightedPicker(integral, this.random);
    const pickRemaining = weightedPicker(remaining, this.random);
    return () => population[integer(this.random, population.length) < integralTotal ? pickIntegral() : pickRemaining()];
  }

  evolve(count = 1): void {
    if (!Number.isInteger(count) || count < 1 || count > 100) throw new Error('Número de generaciones no válido.');
    for (let iteration = 0; iteration < count && this.generation < MAX_GENERATIONS; iteration++) {
      const select = this.selector();
      const next: Individual[] = [];
      if (this.config.elitism) {
        const elite = this.currentBest();
        next.push({ genes: [...elite.genes], fitness: elite.fitness });
      }
      while (next.length < this.population.length) {
        const father = select();
        const mother = select();
        const genes = this.random() < this.config.crossoverRate
          ? cross(this.problem, father.genes, mother.genes, this.config.crossover, this.random) : [...father.genes];
        mutate(this.problem, genes, this.config.mutationRate, this.random);
        next.push(this.individual(genes));
      }
      this.population = next;
      this.generation++;
      const current = this.currentBest();
      if (this.better(current.fitness, this.best.fitness)) this.best = current;
      this.record();
    }
  }

  private record(): void {
    const current = this.currentBest();
    const average = this.population.reduce((sum, individual) => sum + individual.fitness, 0) / this.population.length;
    this.history.push({ generation: this.generation, best: this.best.fitness, average, current: current.fitness });
    if (this.history.length > 1001) this.history.splice(1, 1);
    if (!this.discoveries.length || this.discoveries[0].fitness !== this.best.fitness) {
      this.discoveries.unshift({ generation: this.generation, fitness: this.best.fitness });
      this.discoveries = this.discoveries.slice(0, 6);
    }
  }

  snapshot(status: Status = 'ready'): Snapshot {
    const improvement = (this.problem.minimize ? this.initialFitness - this.best.fitness : this.best.fitness - this.initialFitness)
      / (Math.abs(this.initialFitness) || 1) * 100;
    return {
      generation: this.generation, status: this.generation >= MAX_GENERATIONS ? 'completed' : status,
      problem: this.problem, best: this.best, current: this.currentBest(),
      average: this.history[this.history.length - 1].average,
      diversity: new Set(this.population.map(individual => individual.genes.join(','))).size / this.population.length * 100,
      improvement, population: this.population, history: [...this.history], discoveries: [...this.discoveries],
    };
  }
}
