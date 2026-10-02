import type { Config, ProblemId } from './types';

export const PROBLEMS: Record<ProblemId, { name: string; short: string; unit: string; sizeLabel: string; defaultSize: number; min: number; max: number }> = {
  queens: { name: 'N-reinas', short: 'Reinas', unit: 'pares sin conflicto', sizeLabel: 'Reinas', defaultSize: 8, min: 1, max: 24 },
  tsp: { name: 'Viajante de comercio', short: 'Viajante', unit: 'unidades de distancia', sizeLabel: 'Ciudades', defaultSize: 24, min: 3, max: 60 },
  circular: { name: 'Viajante circular', short: 'Circular', unit: 'unidades de distancia', sizeLabel: 'Ciudades', defaultSize: 16, min: 3, max: 60 },
  knapsack: { name: 'Problema de la mochila', short: 'Mochila', unit: 'valor total', sizeLabel: 'Objetos', defaultSize: 20, min: 3, max: 48 },
  sequence: { name: 'Adivinar la secuencia', short: 'Secuencia', unit: 'dígitos correctos', sizeLabel: 'Dígitos', defaultSize: 12, min: 1, max: 40 },
  function: { name: 'Optimización de función', short: 'Función', unit: 'valor de f(x)', sizeLabel: 'Bits', defaultSize: 32, min: 32, max: 32 },
};

export const DEFAULT_CONFIG: Config = {
  problem: 'queens', size: 8, populationSize: 96, selection: 'tournament',
  crossover: 'uniform', mutationRate: 0.025, crossoverRate: 0.85,
  tournamentSize: 5, seed: 42, elitism: false,
};

export function validateConfig(config: Config): void {
  if (typeof config.problem !== 'string' || !Object.hasOwn(PROBLEMS, config.problem)) throw new Error('Problema no válido.');
  const problem = PROBLEMS[config.problem];
  if (!Number.isInteger(config.size) || config.size < problem.min || config.size > problem.max) {
    throw new Error(`El tamaño debe estar entre ${problem.min} y ${problem.max}.`);
  }
  if (!Number.isInteger(config.populationSize) || config.populationSize < 2 || config.populationSize > 500) {
    throw new Error('La población debe estar entre 2 y 500.');
  }
  if (!Number.isInteger(config.tournamentSize) || config.tournamentSize < 1 || config.tournamentSize > 50) {
    throw new Error('El torneo debe estar entre 1 y 50.');
  }
  if (!Number.isInteger(config.seed) || config.seed < 0 || config.seed > 4294967295) {
    throw new Error('La semilla debe ser un entero entre 0 y 4294967295.');
  }
  for (const rate of [config.mutationRate, config.crossoverRate]) {
    if (!Number.isFinite(rate) || rate < 0 || rate > 1) throw new Error('Las tasas deben estar entre 0 y 100 %.');
  }
  if (!['tournament', 'roulette', 'truncation', 'residual'].includes(config.selection)
      || !['single', 'double', 'uniform'].includes(config.crossover) || typeof config.elitism !== 'boolean') {
    throw new Error('Operadores no válidos.');
  }
}

export function readConfig(search: string): Config {
  const encoded = new URLSearchParams(search).get('config');
  if (!encoded) return { ...DEFAULT_CONFIG };
  try {
    const parsed = JSON.parse(encoded);
    const config: Config = Object.fromEntries(Object.keys(DEFAULT_CONFIG).map(key =>
      [key, parsed[key] ?? DEFAULT_CONFIG[key as keyof Config]])) as unknown as Config;
    validateConfig(config);
    return config;
  } catch {
    return { ...DEFAULT_CONFIG };
  }
}

export function shareUrl(config: Config, location: Pick<Location, 'origin' | 'pathname'>): string {
  const url = new URL(location.pathname, location.origin);
  url.searchParams.set('config', JSON.stringify(config));
  return url.toString();
}
