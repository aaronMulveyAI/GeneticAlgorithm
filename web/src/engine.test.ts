import { describe, expect, it } from 'vitest';
import seedrandom from 'seedrandom';
import { DEFAULT_CONFIG, PROBLEMS, readConfig, shareUrl, validateConfig } from './config';
import { binaryToReal, createProblem, cross, evaluate, GeneticEngine, MAX_GENERATIONS, mutate, objective, selectionWeights, validateGenes } from './engine';
import { distanceField, flyRocket, remainingDistance } from './rockets';
import { BODIES, geneCount, simulateWalker, terrainHeight } from './walker';
import { decodePoint, LANDSCAPES, normalizedHeight } from './landscapes';
import { ALPHABET, DEFAULT_PHRASE, normalizePhrase, PALETTE, phraseGenes, spriteGenes, SPRITES } from './sequence';
import type { Config, Crossover, ProblemId, Scenario, Selection } from './types';

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
    const problem = createProblem({ ...DEFAULT_CONFIG, problem: 'sequence', size: 3, sequenceMode: 'digits' }, seedrandom('fixture'));
    problem.target = [4, 5, 6];
    expect(evaluate(problem, [4, 0, 6])).toBe(2);
  });
  it('decodifica los extremos de 32 bits y elimina la singularidad en cero', () => {
    expect(binaryToReal(Array(32).fill(0))).toBe(-100);
    expect(binaryToReal(Array(32).fill(1))).toBe(100);
    expect(objective(0)).toBe(7.5);
  });
});

describe('Frase y pixel art', () => {
  it('normaliza la frase: mayúsculas, sin tildes, con Ñ y sin símbolos', () => {
    expect(normalizePhrase('  ¡Hola, señor   Pingüino!  ')).toBe('HOLA SEÑOR PINGUINO');
    expect(normalizePhrase('evolución')).toBe('EVOLUCION');
    expect(normalizePhrase('x'.repeat(60))).toHaveLength(40);
    expect(normalizePhrase('123 ?')).toBe('');
    expect(normalizePhrase(DEFAULT_PHRASE)).toBe(DEFAULT_PHRASE);
  });
  it('usa la frase como objetivo y el alfabeto como dominio', () => {
    const problem = createProblem({ ...DEFAULT_CONFIG, problem: 'sequence', sequenceMode: 'phrase', phrase: 'HOLA MUNDO' }, seedrandom('fixture'));
    expect(problem.size).toBe(10);
    expect(problem.domain).toBe(ALPHABET.length);
    expect(problem.target.map(gene => ALPHABET[gene]).join('')).toBe('HOLA MUNDO');
    expect(evaluate(problem, phraseGenes('HOLA MUNDO'))).toBe(10);
    expect(evaluate(problem, phraseGenes('HOLA LUNES'))).toBe(7);
  });
  it('dibujos de 16 × 16 con colores de la paleta', () => {
    for (const sprite of Object.keys(SPRITES) as (keyof typeof SPRITES)[]) {
      const genes = spriteGenes(sprite);
      expect(genes).toHaveLength(256);
      expect(genes.every(gene => gene >= 0 && gene < PALETTE.length)).toBe(true);
      expect(new Set(genes).size).toBeGreaterThan(1);
      const problem = createProblem({ ...DEFAULT_CONFIG, problem: 'sequence', sequenceMode: 'pixels', sprite }, seedrandom('fixture'));
      expect(problem.size).toBe(256);
      expect(problem.domain).toBe(PALETTE.length);
      expect(evaluate(problem, genes)).toBe(256);
    }
  });
  it('descubre la frase objetivo', () => {
    const engine = new GeneticEngine({ ...DEFAULT_CONFIG, problem: 'sequence', sequenceMode: 'phrase', mutationRate: 0.035 });
    engine.evolve(100); engine.evolve(100);
    expect(engine.snapshot().best.fitness).toBe(DEFAULT_PHRASE.length);
  });
});

describe('Funciones de dos variables', () => {
  const landscapes = Object.keys(LANDSCAPES) as (keyof typeof LANDSCAPES)[];
  it('decodifica 16 bits por eje en los extremos del dominio', () => {
    for (const landscape of landscapes) {
      const [low, high] = LANDSCAPES[landscape].range;
      expect(decodePoint(landscape, Array(32).fill(0))).toEqual({ x: low, y: low });
      expect(decodePoint(landscape, Array(32).fill(1))).toEqual({ x: high, y: high });
      expect(decodePoint(landscape, [...Array(16).fill(1), ...Array(16).fill(0)])).toEqual({ x: high, y: low });
    }
  });
  it('vale 0 en los óptimos globales conocidos', () => {
    for (const landscape of landscapes) for (const optimum of LANDSCAPES[landscape].optima) {
      expect(LANDSCAPES[landscape].f(optimum.x, optimum.y)).toBeCloseTo(0, 3);
      expect(normalizedHeight(landscape, LANDSCAPES[landscape].f(optimum.x, optimum.y))).toBeLessThan(0.01);
    }
  });
  it('minimiza las funciones 2D y mantiene la original como en Java', () => {
    const rastrigin = createProblem({ ...DEFAULT_CONFIG, problem: 'function', size: 32, landscape: 'rastrigin' }, seedrandom('fixture'));
    expect(rastrigin.minimize).toBe(true);
    const genes = [0, ...Array(15).fill(1), 0, ...Array(15).fill(1)];
    const point = decodePoint('rastrigin', genes);
    expect(evaluate(rastrigin, genes)).toBe(LANDSCAPES.rastrigin.f(point.x, point.y));
    const original = createProblem({ ...DEFAULT_CONFIG, problem: 'function', size: 32, landscape: 'original' }, seedrandom('fixture'));
    expect(original.minimize).toBe(false);
    expect(evaluate(original, genes)).toBe(objective(binaryToReal(genes)));
  });
  it('encuentra el valle de Ackley', () => {
    const engine = new GeneticEngine({ ...DEFAULT_CONFIG, problem: 'function', size: 32, landscape: 'ackley', mutationRate: 0.01 });
    engine.evolve(60);
    expect(engine.snapshot().best.fitness).toBeLessThan(0.05);
  });
});

describe('Cohetes inteligentes', () => {
  const rockets = (scenario: Scenario, seed = 7) => createProblem(
    { ...DEFAULT_CONFIG, problem: 'rockets', size: 140, scenario, seed }, seedrandom(String(seed)));
  const straightUp = Array(140).fill(0);
  it('vuela en línea recta, aterriza y premia llegar antes', () => {
    const problem = rockets('wall');
    problem.world!.obstacles = [];
    const flight = flyRocket(problem.world!, straightUp);
    expect(flight.outcome).toBe('landed');
    expect(flight.path.every(point => Math.abs(point.x - 50) < 1e-9)).toBe(true);
    expect(evaluate(problem, straightUp)).toBeGreaterThan(100);
    const detour = [...Array.from({ length: 40 }, (_, i) => i % 2 ? 6 : 2), ...Array(100).fill(0)];
    expect(flyRocket(problem.world!, detour).outcome).toBe('landed');
    expect(evaluate(problem, detour)).toBeLessThan(evaluate(problem, straightUp));
  });
  it('se estrella contra el muro y lo penaliza', () => {
    const problem = rockets('wall');
    expect(flyRocket(problem.world!, straightUp).outcome).toBe('crashed');
    expect(evaluate(problem, straightUp)).toBeLessThan(50);
    expect(evaluate(problem, Array(140).fill(4))).toBe(0);
  });
  it('mide el avance rodeando los obstáculos', () => {
    const world = rockets('wall').world!;
    expect(remainingDistance(world, world.goal)).toBe(0);
    expect(remainingDistance(world, world.start)).toBeGreaterThan(world.start.y - world.goal.y + 10);
    expect(remainingDistance(world, { x: 50, y: 52.5 })).toBeGreaterThan(remainingDistance(world, { x: 8, y: 52.5 }));
  });
  it('genera campos de asteroides reproducibles y con pasillo hasta la diana', () => {
    for (let seed = 0; seed < 40; seed++) {
      const world = rockets('asteroids', seed).world!;
      expect(world.obstacles.length).toBeGreaterThan(0);
      expect(Number.isFinite(distanceField(world, 5)[93 * 100 + 50])).toBe(true);
      expect(rockets('asteroids', seed).world).toEqual(world);
    }
  });
  it('aprende a rodear el muro', () => {
    const engine = new GeneticEngine({ ...DEFAULT_CONFIG, problem: 'rockets', size: 140, mutationRate: 0.01, scenario: 'wall' });
    engine.evolve(100);
    expect(engine.snapshot().best.fitness).toBeGreaterThan(100);
  });
});

describe('Criatura que aprende a andar', () => {
  const walker = (creature: 'quadruped' | 'worm', terrain: 'flat' | 'hills' = 'flat') =>
    createProblem({ ...DEFAULT_CONFIG, problem: 'walker', size: 10, creature, terrain }, seedrandom('fixture'));
  it('ajusta el cromosoma al cuerpo y usa la duración como tamaño configurable', () => {
    expect(walker('quadruped').size).toBe(geneCount('quadruped'));
    expect(walker('worm').size).toBe(1 + 2 * BODIES.worm.muscles.length);
    expect(walker('worm').walker).toEqual({ creature: 'worm', terrain: 'flat', duration: 10 });
    expect(walker('quadruped').domain).toBe(16);
  });
  it('sin músculos activos se queda de pie y no avanza', () => {
    for (const creature of ['quadruped', 'worm'] as const) {
      const problem = walker(creature);
      const genes = Array(problem.size).fill(0);
      const result = simulateWalker(problem.walker!, genes);
      expect(result.fallen).toBe(false);
      expect(Math.abs(result.distance)).toBeLessThan(0.05);
    }
  });
  it('es determinista y conserva la longitud de los huesos', () => {
    const problem = walker('quadruped');
    const random = seedrandom('genes');
    for (let trial = 0; trial < 20; trial++) {
      const genes = Array.from({ length: problem.size }, () => Math.floor(random() * 16));
      const run = simulateWalker(problem.walker!, genes, true);
      expect(simulateWalker(problem.walker!, genes).distance).toBe(run.distance);
      expect(evaluate(problem, genes)).toBe(run.distance);
      const body = BODIES.quadruped;
      for (const frame of run.frames!) for (const [a, b] of body.bones) {
        const rest = Math.hypot(body.nodes[b].x - body.nodes[a].x, body.nodes[b].y - body.nodes[a].y);
        expect(Math.abs(Math.hypot(frame[2 * b] - frame[2 * a], frame[2 * b + 1] - frame[2 * a + 1]) / rest - 1)).toBeLessThan(0.25);
      }
    }
  });
  it('el terreno de colinas empieza llano', () => {
    expect(terrainHeight('hills', 1)).toBe(0);
    expect(terrainHeight('hills', 2 + Math.PI / 0.9)).toBeCloseTo(0.4);
    expect(terrainHeight('flat', 5)).toBe(0);
  });
  it('aprende a desplazarse', () => {
    const engine = new GeneticEngine({ ...DEFAULT_CONFIG, problem: 'walker', size: 10, mutationRate: 0.05 });
    const initial = engine.snapshot().average;
    engine.evolve(30);
    const snapshot = engine.snapshot();
    expect(snapshot.best.fitness).toBeGreaterThan(4);
    expect(snapshot.average).toBeGreaterThan(initial + 2);
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
  it('las semillas cuyos dígitos se repiten generan ejecuciones distintas', () => {
    for (const [first, second] of [[1, 11], [1, 111], [12, 1212], [7, 77777]]) {
      const instance = (seed: number) => new GeneticEngine({ ...DEFAULT_CONFIG, problem: 'tsp', size: 24, seed }).snapshot();
      expect(instance(first).problem.points).not.toEqual(instance(second).problem.points);
      expect(instance(first).population).not.toEqual(instance(second).population);
    }
  });
  it('permite conservar el mejor individuo sin alterar la población anterior', () => {
    const engine = new GeneticEngine({ ...DEFAULT_CONFIG, elitism: true, mutationRate: 1 });
    const before = engine.snapshot().current.fitness;
    engine.evolve(10);
    expect(engine.snapshot().current.fitness).toBeGreaterThanOrEqual(before);
  });
  it('muta bits, dígitos y columnas en sus dominios, incluidos tamaños pequeños', () => {
    for (const [id, size] of [['queens', 1], ['sequence', 1], ['sequence', 40], ['knapsack', 3], ['function', 32], ['rockets', 40]] as const) {
      const problem = createProblem({ ...DEFAULT_CONFIG, problem: id, size, sequenceMode: 'digits' }, seedrandom('fixture'));
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
    const config = { ...DEFAULT_CONFIG, seed: 987, elitism: true, selection: 'residual' as const, scenario: 'slalom' as const,
      creature: 'worm' as const, terrain: 'hills' as const, landscape: 'himmelblau' as const,
      sequenceMode: 'pixels' as const, phrase: 'HOLA MUNDO', sprite: 'invader' as const };
    const url = shareUrl(config, { origin: 'https://demo.vercel.app', pathname: '/' });
    expect(readConfig(new URL(url).search)).toEqual(config);
  });
  it('rechaza enlaces incorrectos y parámetros fuera de los límites', () => {
    expect(readConfig('?config=null')).toEqual(DEFAULT_CONFIG);
    expect(readConfig('?config=%7B')).toEqual(DEFAULT_CONFIG);
    expect(readConfig('?config={"seed":-1}')).toEqual(DEFAULT_CONFIG);
    expect(readConfig('?config={"problem":"__proto__"}')).toEqual(DEFAULT_CONFIG);
    expect(readConfig('?config={"problem":"rockets","size":140,"scenario":"toString"}')).toEqual(DEFAULT_CONFIG);
    expect(readConfig('?config={"problem":"walker","size":10,"creature":"__proto__"}')).toEqual(DEFAULT_CONFIG);
    expect(readConfig('?config={"problem":"walker","size":10,"terrain":"lava"}')).toEqual(DEFAULT_CONFIG);
    expect(readConfig('?config={"problem":"function","size":32,"landscape":"valueOf"}')).toEqual(DEFAULT_CONFIG);
    expect(readConfig('?config={"problem":"sequence","phrase":"hola"}')).toEqual(DEFAULT_CONFIG);
    expect(readConfig('?config={"problem":"sequence","phrase":""}')).toEqual(DEFAULT_CONFIG);
    expect(readConfig('?config={"problem":"sequence","sprite":"constructor"}')).toEqual(DEFAULT_CONFIG);
    for (const invalid of [{ size: 0 }, { mutationRate: NaN }, { crossoverRate: 2 }, { seed: -1 }, { populationSize: 501 }]) {
      expect(() => validateConfig({ ...DEFAULT_CONFIG, ...invalid })).toThrow();
    }
  });
});
