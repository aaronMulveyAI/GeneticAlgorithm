import type { Landscape, Point } from './types';

// Funciones de prueba clásicas de dos variables. Todas se minimizan y su óptimo global vale 0
// (Schwefel, aproximadamente). La función original de una variable se conserva aparte.
export interface LandscapeDefinition {
  name: string;
  range: [number, number];
  f: (x: number, y: number) => number;
  optima: Point[];
  // Escala para dibujar la altura: logarítmica cuando unos pocos picos aplastarían los valles.
  logarithmic: boolean;
}

export const BITS_PER_AXIS = 16;
const TAU = 2 * Math.PI;

export const LANDSCAPES: Record<Exclude<Landscape, 'original'>, LandscapeDefinition> = {
  rastrigin: {
    name: 'Rastrigin (muchos hoyos)',
    range: [-5.12, 5.12],
    f: (x, y) => 20 + x * x - 10 * Math.cos(TAU * x) + y * y - 10 * Math.cos(TAU * y),
    optima: [{ x: 0, y: 0 }],
    logarithmic: false,
  },
  ackley: {
    name: 'Ackley (embudo)',
    range: [-5, 5],
    f: (x, y) => -20 * Math.exp(-0.2 * Math.sqrt(0.5 * (x * x + y * y)))
      - Math.exp(0.5 * (Math.cos(TAU * x) + Math.cos(TAU * y))) + Math.E + 20,
    optima: [{ x: 0, y: 0 }],
    logarithmic: false,
  },
  himmelblau: {
    name: 'Himmelblau (cuatro valles)',
    range: [-5, 5],
    f: (x, y) => (x * x + y - 11) ** 2 + (x + y * y - 7) ** 2,
    optima: [
      { x: 3, y: 2 }, { x: -2.805118, y: 3.131312 },
      { x: -3.77931, y: -3.283186 }, { x: 3.584428, y: -1.848126 },
    ],
    logarithmic: true,
  },
  schwefel: {
    name: 'Schwefel (engañosa)',
    range: [-500, 500],
    f: (x, y) => 418.9829 * 2 - x * Math.sin(Math.sqrt(Math.abs(x))) - y * Math.sin(Math.sqrt(Math.abs(y))),
    optima: [{ x: 420.9687, y: 420.9687 }],
    logarithmic: false,
  },
};

export const LANDSCAPE_NAMES: Record<Landscape, string> = {
  ...Object.fromEntries(Object.entries(LANDSCAPES).map(([id, definition]) => [id, definition.name])) as Record<Exclude<Landscape, 'original'>, string>,
  original: 'Original de una variable (versión Java)',
};

function axis(bits: number[], [low, high]: [number, number]): number {
  return low + (high - low) * bits.reduce((value, bit) => value * 2 + bit, 0) / (2 ** bits.length - 1);
}

// Los 16 primeros bits codifican x y los 16 siguientes, y.
export function decodePoint(landscape: Exclude<Landscape, 'original'>, genes: number[]): Point {
  const range = LANDSCAPES[landscape].range;
  return { x: axis(genes.slice(0, BITS_PER_AXIS), range), y: axis(genes.slice(BITS_PER_AXIS), range) };
}

export function landscapeValue(landscape: Exclude<Landscape, 'original'>, genes: number[]): number {
  const { x, y } = decodePoint(landscape, genes);
  return LANDSCAPES[landscape].f(x, y);
}

export interface HeightGrid { size: number; values: Float64Array; low: number; high: number }
const grids = new Map<string, HeightGrid>();

// Muestreo regular de la función, compartido por la superficie 3D y el mapa de calor.
export function heightGrid(landscape: Exclude<Landscape, 'original'>, size: number): HeightGrid {
  const key = `${landscape}:${size}`;
  const cached = grids.get(key);
  if (cached) return cached;
  const { range: [low, high], f } = LANDSCAPES[landscape];
  const values = new Float64Array(size * size);
  let minimum = Infinity, maximum = -Infinity;
  for (let row = 0; row < size; row++) for (let column = 0; column < size; column++) {
    const value = f(low + (high - low) * column / (size - 1), low + (high - low) * row / (size - 1));
    values[row * size + column] = value;
    minimum = Math.min(minimum, value);
    maximum = Math.max(maximum, value);
  }
  const grid = { size, values, low: minimum, high: maximum };
  grids.set(key, grid);
  return grid;
}

// Altura normalizada entre 0 (valle más bajo) y 1 (pico más alto).
export function normalizedHeight(landscape: Exclude<Landscape, 'original'>, value: number): number {
  const { low, high } = heightGrid(landscape, 64);
  const offset = Math.max(0, value - low);
  const t = LANDSCAPES[landscape].logarithmic ? Math.log1p(offset) / Math.log1p(high - low) : offset / (high - low);
  return Math.min(1, Math.max(0, t));
}
