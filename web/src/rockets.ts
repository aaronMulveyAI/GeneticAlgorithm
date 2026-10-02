import type { Point, Rect, RocketWorld, Scenario } from './types';

// El mundo mide 100 × 100 unidades con el eje y hacia abajo. Cada gen es el impulso de un paso de vuelo.
export const WORLD_SIZE = 100;
export const DIRECTIONS = 8;
export const DIRECTION_ARROWS = ['↑', '↗', '→', '↘', '↓', '↙', '←', '↖'];
const THRUST = 0.12;
const MAX_SPEED = 1.8;
const CRASH_PENALTY = 0.5;
// El mapa de distancias trata como pared una franja de 3 unidades alrededor de cada obstáculo,
// porque un cohete con inercia no cruza huecos tan estrechos aunque geométricamente quepa.
const CLEARANCE = 3;

export type Outcome = 'flying' | 'crashed' | 'landed';
export interface Flight { path: Point[]; outcome: Outcome; steps: number }

type Random = () => number;
const integer = (random: Random, bound: number) => Math.floor(random() * bound);

export function createWorld(scenario: Scenario, random: Random): RocketWorld {
  const world: RocketWorld = { start: { x: 50, y: 93 }, goal: { x: 50, y: 9 }, goalRadius: 4, obstacles: [] };
  if (scenario === 'wall') {
    world.obstacles.push({ x: 12, y: 46, width: 76, height: 5 });
  } else if (scenario === 'slalom') {
    world.obstacles.push({ x: 0, y: 64, width: 64, height: 4 }, { x: 36, y: 34, width: 64, height: 4 });
  } else {
    // Los asteroides quedan entre las alturas 20 y 77, lejos de la base y de la diana. Se descartan
    // los campos sin un pasillo de 10 unidades hasta la diana y, si cuesta encontrarlo, se usan
    // menos asteroides. Las coordenadas son enteras para que coincidan con el mapa de distancias.
    for (let attempt = 0; attempt < 110; attempt++) {
      world.obstacles = Array.from({ length: 11 - Math.floor(attempt / 10) }, () => {
        const width = 8 + integer(random, 15);
        const height = 6 + integer(random, 9);
        return { x: integer(random, WORLD_SIZE - width), y: 20 + integer(random, 58 - height), width, height };
      });
      if (Number.isFinite(distanceField(world, 5)[cell(world.start)])) break;
    }
  }
  return world;
}

function nearRect(rect: Rect, point: Point, margin: number): boolean {
  return point.x > rect.x - margin && point.x < rect.x + rect.width + margin
    && point.y > rect.y - margin && point.y < rect.y + rect.height + margin;
}

export function blocked(world: RocketWorld, point: Point, margin = 0): boolean {
  return point.x < 0 || point.x > WORLD_SIZE || point.y < 0 || point.y > WORLD_SIZE
    || world.obstacles.some(rect => nearRect(rect, point, margin));
}

export function flyRocket(world: RocketWorld, genes: number[]): Flight {
  let x = world.start.x, y = world.start.y, vx = 0, vy = 0;
  const path: Point[] = [{ x, y }];
  for (let step = 0; step < genes.length; step++) {
    const angle = genes[step] * 2 * Math.PI / DIRECTIONS - Math.PI / 2;
    vx += THRUST * Math.cos(angle);
    vy += THRUST * Math.sin(angle);
    const speed = Math.hypot(vx, vy);
    if (speed > MAX_SPEED) {
      vx *= MAX_SPEED / speed;
      vy *= MAX_SPEED / speed;
    }
    x += vx;
    y += vy;
    path.push({ x, y });
    if (Math.hypot(x - world.goal.x, y - world.goal.y) <= world.goalRadius) return { path, outcome: 'landed', steps: step + 1 };
    if (blocked(world, { x, y })) return { path, outcome: 'crashed', steps: step + 1 };
  }
  return { path, outcome: 'flying', steps: genes.length };
}

const cell = (point: Point) => {
  const clamp = (value: number) => Math.min(WORLD_SIZE - 1, Math.max(0, Math.floor(value)));
  return clamp(point.y) * WORLD_SIZE + clamp(point.x);
};

const fields = new WeakMap<RocketWorld, Float64Array>();

// Distancia de navegación hasta la diana rodeando los obstáculos (Dijkstra sobre una cuadrícula
// de 8 vecinos). Con la distancia en línea recta los cohetes se quedarían atascados bajo los muros.
export function distanceField(world: RocketWorld, clearance = CLEARANCE): Float64Array {
  const cached = clearance === CLEARANCE ? fields.get(world) : undefined;
  if (cached) return cached;
  const size = WORLD_SIZE * WORLD_SIZE;
  const distance = new Float64Array(size).fill(Infinity);
  const free = new Uint8Array(size);
  for (let i = 0; i < size; i++) {
    free[i] = Number(!blocked(world, { x: i % WORLD_SIZE + 0.5, y: Math.floor(i / WORLD_SIZE) + 0.5 }, clearance));
  }
  const heap: [number, number][] = [];
  const push = (item: [number, number]) => {
    heap.push(item);
    for (let i = heap.length - 1; i > 0;) {
      const parent = (i - 1) >> 1;
      if (heap[parent][0] <= heap[i][0]) break;
      [heap[parent], heap[i]] = [heap[i], heap[parent]];
      i = parent;
    }
  };
  const pop = () => {
    const top = heap[0];
    const last = heap.pop()!;
    if (heap.length) {
      heap[0] = last;
      for (let i = 0; ;) {
        const left = 2 * i + 1, right = left + 1;
        let smallest = i;
        if (left < heap.length && heap[left][0] < heap[smallest][0]) smallest = left;
        if (right < heap.length && heap[right][0] < heap[smallest][0]) smallest = right;
        if (smallest === i) break;
        [heap[smallest], heap[i]] = [heap[i], heap[smallest]];
        i = smallest;
      }
    }
    return top;
  };
  const goal = cell(world.goal);
  distance[goal] = 0;
  push([0, goal]);
  while (heap.length) {
    const [current, index] = pop();
    if (current > distance[index]) continue;
    const x = index % WORLD_SIZE, y = Math.floor(index / WORLD_SIZE);
    for (let dy = -1; dy <= 1; dy++) for (let dx = -1; dx <= 1; dx++) {
      const nx = x + dx, ny = y + dy;
      if ((!dx && !dy) || nx < 0 || ny < 0 || nx >= WORLD_SIZE || ny >= WORLD_SIZE) continue;
      const next = ny * WORLD_SIZE + nx;
      // Las diagonales no atraviesan esquinas de obstáculos.
      if (!free[next] || (dx && dy && (!free[y * WORLD_SIZE + nx] || !free[ny * WORLD_SIZE + x]))) continue;
      const candidate = current + (dx && dy ? Math.SQRT2 : 1);
      if (candidate < distance[next]) {
        distance[next] = candidate;
        push([candidate, next]);
      }
    }
  }
  if (clearance === CLEARANCE) fields.set(world, distance);
  return distance;
}

// Las posiciones dentro de la franja de seguridad toman la distancia de la celda libre más cercana.
export function remainingDistance(world: RocketWorld, point: Point): number {
  const field = distanceField(world);
  const index = cell(point);
  if (Number.isFinite(field[index])) return field[index];
  let best = Infinity;
  const x = index % WORLD_SIZE, y = Math.floor(index / WORLD_SIZE), radius = CLEARANCE + 2;
  for (let ny = Math.max(0, y - radius); ny <= Math.min(WORLD_SIZE - 1, y + radius); ny++) {
    for (let nx = Math.max(0, x - radius); nx <= Math.min(WORLD_SIZE - 1, x + radius); nx++) {
      best = Math.min(best, field[ny * WORLD_SIZE + nx] + Math.hypot(nx - x, ny - y));
    }
  }
  return best;
}

// 0–100 según el avance hacia la diana (la mitad si se estrella) y 100–200 al aterrizar, más cuanto antes llegue.
export function rocketFitness(world: RocketWorld, genes: number[]): number {
  const flight = flyRocket(world, genes);
  if (flight.outcome === 'landed') return 100 + 100 * (1 - flight.steps / genes.length);
  const last = flight.path[flight.path.length - (flight.outcome === 'crashed' ? 2 : 1)];
  const remaining = remainingDistance(world, last);
  const initial = remainingDistance(world, world.start);
  const progress = Number.isFinite(remaining) ? Math.max(0, 1 - remaining / initial) * 100 : 0;
  return flight.outcome === 'crashed' ? progress * CRASH_PENALTY : progress;
}
