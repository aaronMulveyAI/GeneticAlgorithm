import type { Creature, Point, Terrain, WalkerWorld } from './types';

// Criaturas de nodos unidos por huesos (longitud fija) y músculos (longitud que oscila).
// Integración de Verlet con restricciones de distancia, gravedad, suelo y rozamiento.
// Unidades: metros y segundos; el eje y apunta hacia arriba.
export const LEVELS = 16;
export const STEPS_PER_SECOND = 30;
const DT = 1 / STEPS_PER_SECOND;
const GRAVITY = 9.8;
const ITERATIONS = 4;
const AIR_DAMPING = 0.99;
const GROUND_FRICTION = 0.85;
const MUSCLE_STIFFNESS = 1;
const MAX_AMPLITUDE = 0.3;

// Si un nodo frágil (tronco o lomo) toca el suelo, la criatura se cae y la prueba termina.
export interface Body { nodes: Point[]; bones: [number, number][]; muscles: [number, number][]; fragile: number[] }

function ladder(segments: number, length: number, height: number): Body {
  const nodes: Point[] = [];
  for (let i = 0; i <= segments; i++) nodes.push({ x: i * length, y: 0.02 }, { x: i * length, y: height });
  const bones: [number, number][] = [];
  const muscles: [number, number][] = [];
  for (let i = 0; i <= segments; i++) bones.push([2 * i, 2 * i + 1]);
  for (let i = 0; i < segments; i++) {
    bones.push([2 * i + 1, 2 * i + 3]);
    muscles.push([2 * i, 2 * i + 2], [2 * i, 2 * i + 3]);
  }
  return { nodes, bones, muscles, fragile: nodes.map((_, i) => i).filter(i => i % 2) };
}

export const BODIES: Record<Creature, Body> = {
  // Tronco triangulado y dos patas con rodilla. Cada pata tiene un músculo de cadera y otro de rodilla.
  quadruped: {
    nodes: [
      { x: 0, y: 1 }, { x: 1.4, y: 1 }, { x: 0.7, y: 1.2 },
      { x: -0.05, y: 0.52 }, { x: 0, y: 0.02 },
      { x: 1.45, y: 0.52 }, { x: 1.4, y: 0.02 },
    ],
    bones: [[0, 1], [0, 2], [1, 2], [0, 3], [3, 4], [1, 5], [5, 6]],
    muscles: [[2, 3], [0, 4], [2, 5], [1, 6]],
    fragile: [0, 1, 2],
  },
  // Escalera de tres segmentos que se arrastra encogiendo y estirando la base.
  worm: ladder(3, 0.55, 0.4),
};

export const geneCount = (creature: Creature) => 1 + 2 * BODIES[creature].muscles.length;

export function terrainHeight(terrain: Terrain, x: number): number {
  return terrain === 'flat' || x <= 2 ? 0 : 0.2 * (1 - Math.cos(0.9 * (x - 2)));
}

// Gen 0: frecuencia común. Después, por músculo: amplitud y fase.
export function decodeGenes(genes: number[]) {
  const frequency = 0.6 + 1.6 * genes[0] / (LEVELS - 1);
  const muscles = [];
  for (let i = 1; i < genes.length; i += 2) {
    muscles.push({ amplitude: MAX_AMPLITUDE * genes[i] / (LEVELS - 1), phase: 2 * Math.PI * genes[i + 1] / LEVELS });
  }
  return { frequency, muscles };
}

export interface WalkResult { distance: number; fallen: boolean; frames?: Float32Array[] }

const centroid = (positions: Float64Array) => {
  let sum = 0;
  for (let i = 0; i < positions.length; i += 2) sum += positions[i];
  return sum / (positions.length / 2);
};

export function simulateWalker(world: WalkerWorld, genes: number[], record = false): WalkResult {
  const body = BODIES[world.creature];
  const count = body.nodes.length;
  const position = new Float64Array(count * 2);
  body.nodes.forEach((node, i) => { position[2 * i] = node.x; position[2 * i + 1] = node.y; });
  const previous = Float64Array.from(position);
  const length = (a: number, b: number) =>
    Math.hypot(position[2 * b] - position[2 * a], position[2 * b + 1] - position[2 * a + 1]);
  const bones = body.bones.map(([a, b]) => ({ a, b, rest: length(a, b) }));
  const muscles = body.muscles.map(([a, b]) => ({ a, b, rest: length(a, b) }));
  const { frequency, muscles: control } = decodeGenes(genes);
  const start = centroid(position);
  const steps = world.duration * STEPS_PER_SECOND;
  const frames: Float32Array[] = record ? [Float32Array.from(position)] : [];

  const solve = (a: number, b: number, target: number, stiffness: number) => {
    const dx = position[2 * b] - position[2 * a], dy = position[2 * b + 1] - position[2 * a + 1];
    const distance = Math.sqrt(dx * dx + dy * dy) || 1e-9;
    const correction = (distance - target) / distance * 0.5 * stiffness;
    position[2 * a] += dx * correction; position[2 * a + 1] += dy * correction;
    position[2 * b] -= dx * correction; position[2 * b + 1] -= dy * correction;
  };

  const targets = new Float64Array(muscles.length);
  let fallen = false;
  for (let step = 0; step < steps; step++) {
    const time = step * DT;
    for (let m = 0; m < muscles.length; m++) {
      targets[m] = muscles[m].rest * (1 + control[m].amplitude * Math.sin(2 * Math.PI * frequency * time + control[m].phase));
    }
    for (let i = 0; i < count; i++) {
      const x = position[2 * i], y = position[2 * i + 1];
      position[2 * i] += (x - previous[2 * i]) * AIR_DAMPING;
      position[2 * i + 1] += (y - previous[2 * i + 1]) * AIR_DAMPING - GRAVITY * DT * DT;
      previous[2 * i] = x; previous[2 * i + 1] = y;
    }
    for (let iteration = 0; iteration < ITERATIONS; iteration++) {
      for (const bone of bones) solve(bone.a, bone.b, bone.rest, 1);
      for (let m = 0; m < muscles.length; m++) solve(muscles[m].a, muscles[m].b, targets[m], MUSCLE_STIFFNESS);
      for (const bone of bones) solve(bone.a, bone.b, bone.rest, 1);
      for (let i = 0; i < count; i++) {
        const ground = terrainHeight(world.terrain, position[2 * i]);
        if (position[2 * i + 1] < ground) position[2 * i + 1] = ground;
      }
    }
    // Rozamiento: un nodo apoyado pierde casi toda su velocidad horizontal.
    for (let i = 0; i < count; i++) {
      if (position[2 * i + 1] <= terrainHeight(world.terrain, position[2 * i]) + 1e-3) {
        previous[2 * i] = position[2 * i] - (position[2 * i] - previous[2 * i]) * (1 - GROUND_FRICTION);
      }
    }
    if (record) frames.push(Float32Array.from(position));
    if (body.fragile.some(i => position[2 * i + 1] <= terrainHeight(world.terrain, position[2 * i]) + 1e-3)) {
      fallen = true;
      break;
    }
  }
  const distance = centroid(position) - start;
  return { distance: Number.isFinite(distance) ? distance : 0, fallen, frames: record ? frames : undefined };
}
