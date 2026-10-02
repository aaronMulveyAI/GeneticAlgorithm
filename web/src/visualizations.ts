import { binaryToReal, objective } from './engine';
import { DIRECTIONS, flyRocket, WORLD_SIZE } from './rockets';
import type { Flight } from './rockets';
import { BODIES, simulateWalker, STEPS_PER_SECOND, terrainHeight } from './walker';
import type { WalkResult } from './walker';
import type { Snapshot } from './types';
import type { Painter } from './Canvas';

const GREEN = '#168153';
const BLUE = '#3973d6';
const RED = '#d35a62';
const INK = '#23272f';
const GRID = '#e9edf1';

function text(context: CanvasRenderingContext2D, value: string, x: number, y: number, size = 12, color = INK) {
  context.font = `${size}px system-ui, sans-serif`;
  context.fillStyle = color;
  context.fillText(value, x, y);
}

const flights = new WeakMap<Snapshot, { population: Flight[]; best: Flight }>();

export function rocketFlights(snapshot: Snapshot) {
  let cached = flights.get(snapshot);
  if (!cached) {
    const world = snapshot.problem.world!;
    cached = {
      population: snapshot.population.map(individual => flyRocket(world, individual.genes)),
      best: flyRocket(world, snapshot.best.genes),
    };
    flights.set(snapshot, cached);
  }
  return cached;
}

const walks = new WeakMap<Snapshot, { best: WalkResult; ghosts: WalkResult[] }>();
const GHOSTS = 10;

// El mejor histórico y una muestra de la generación actual repartida por todo el ranking, sin
// cromosomas repetidos: cuando la población converge, los mejores suelen ser copias del mejor.
export function walkerRuns(snapshot: Snapshot) {
  let cached = walks.get(snapshot);
  if (!cached) {
    const world = snapshot.problem.walker!;
    const seen = new Set([snapshot.best.genes.join(',')]);
    const distinct = [...snapshot.population].sort((a, b) => b.fitness - a.fitness).filter(individual => {
      const key = individual.genes.join(',');
      return !seen.has(key) && Boolean(seen.add(key));
    });
    const sample = distinct.length <= GHOSTS ? distinct
      : Array.from({ length: GHOSTS }, (_, i) => distinct[Math.round(i * (distinct.length - 1) / (GHOSTS - 1))]);
    cached = {
      best: simulateWalker(world, snapshot.best.genes, true),
      ghosts: sample.map(individual => simulateWalker(world, individual.genes, true)),
    };
    walks.set(snapshot, cached);
  }
  return cached;
}

const reducedMotion = () => typeof matchMedia === 'function' && matchMedia('(prefers-reduced-motion: reduce)').matches;
const flightStarts = new WeakMap<Snapshot, number>();
const HOLD_MS = 900;

// Mientras la simulación avanza, el vuelo se repite con un reloj continuo y cada vuelta muestra la
// generación más reciente. En pausa, cada generación nueva se ve despegar desde el principio.
function flightFrame(snapshot: Snapshot, time: number, steps = snapshot.problem.size, stepMs = Math.min(24, 4200 / steps)): number {
  if (reducedMotion()) return steps;
  if (snapshot.status !== 'running' && !flightStarts.has(snapshot)) flightStarts.set(snapshot, time);
  const elapsed = Math.max(0, time - (flightStarts.get(snapshot) ?? 0));
  return Math.min(steps, Math.floor(elapsed % (steps * stepMs + HOLD_MS) / stepMs));
}

function rocketPainter(snapshot: Snapshot): Painter {
  return (context, width, height, time) => {
    const world = snapshot.problem.world!;
    const { population, best } = rocketFlights(snapshot);
    const frame = flightFrame(snapshot, time);
    const scale = Math.min(width - 24, height - 24) / WORLD_SIZE;
    const left = (width - scale * WORLD_SIZE) / 2;
    const top = (height - scale * WORLD_SIZE) / 2;
    const px = (x: number) => left + x * scale;
    const py = (y: number) => top + y * scale;
    const at = (flight: Flight) => Math.min(frame, flight.path.length - 1);

    context.fillStyle = '#f7f9fb';
    context.fillRect(left, top, WORLD_SIZE * scale, WORLD_SIZE * scale);
    context.strokeStyle = '#eef1f4';
    context.lineWidth = 1;
    for (let i = 10; i < WORLD_SIZE; i += 10) {
      context.beginPath(); context.moveTo(px(i), top); context.lineTo(px(i), py(WORLD_SIZE)); context.stroke();
      context.beginPath(); context.moveTo(left, py(i)); context.lineTo(px(WORLD_SIZE), py(i)); context.stroke();
    }
    context.strokeStyle = '#dfe5ec';
    context.strokeRect(left + 0.5, top + 0.5, WORLD_SIZE * scale - 1, WORLD_SIZE * scale - 1);

    context.fillStyle = '#3d4652';
    for (const rect of world.obstacles) {
      context.beginPath();
      if (context.roundRect) context.roundRect(px(rect.x), py(rect.y), rect.width * scale, rect.height * scale, 3);
      else context.rect(px(rect.x), py(rect.y), rect.width * scale, rect.height * scale);
      context.fill();
    }

    const goal = { x: px(world.goal.x), y: py(world.goal.y) };
    for (const [radius, color] of [[1, RED], [0.66, '#ffffff'], [0.33, RED]] as const) {
      context.fillStyle = color;
      context.beginPath(); context.arc(goal.x, goal.y, world.goalRadius * scale * radius, 0, Math.PI * 2); context.fill();
    }
    context.fillStyle = '#c3ccd6';
    context.fillRect(px(world.start.x - 5), py(world.start.y + 2), 10 * scale, 1.2 * scale);

    const trail = (flight: Flight, color: string, lineWidth: number) => {
      const end = at(flight);
      context.strokeStyle = color;
      context.lineWidth = lineWidth;
      context.beginPath();
      for (let i = 0; i <= end; i++) {
        const point = flight.path[i];
        if (i === 0) context.moveTo(px(point.x), py(point.y)); else context.lineTo(px(point.x), py(point.y));
      }
      context.stroke();
    };
    context.lineJoin = 'round';
    population.slice(0, 150).forEach(flight => trail(flight, '#3973d61f', 1));
    trail(best, '#168153b0', 2.2);

    const rocket = (flight: Flight, genes: number[], color: string, size: number) => {
      const index = at(flight);
      const point = flight.path[index];
      const previous = flight.path[Math.max(0, index - 1)];
      const x = px(point.x), y = py(point.y);
      const finished = index === flight.path.length - 1 && flight.outcome !== 'flying';
      if (finished && flight.outcome === 'crashed') {
        context.strokeStyle = '#d35a62a0';
        context.lineWidth = 1.5;
        context.beginPath();
        context.moveTo(x - 3, y - 3); context.lineTo(x + 3, y + 3);
        context.moveTo(x + 3, y - 3); context.lineTo(x - 3, y + 3);
        context.stroke();
        return;
      }
      const heading = index === 0 ? -Math.PI / 2 : Math.atan2(point.y - previous.y, point.x - previous.x);
      context.save();
      context.translate(x, y);
      if (!finished && index > 0 && index <= genes.length) {
        const thrust = genes[index - 1] * 2 * Math.PI / DIRECTIONS - Math.PI / 2;
        const flicker = 0.75 + 0.25 * Math.sin(index * 2.7 + size);
        context.fillStyle = '#f0a03ccc';
        context.beginPath();
        context.moveTo(-Math.cos(thrust) * size * 1.25 * flicker, -Math.sin(thrust) * size * 1.25 * flicker);
        context.lineTo(Math.cos(thrust + Math.PI / 2) * size * 0.3, Math.sin(thrust + Math.PI / 2) * size * 0.3);
        context.lineTo(Math.cos(thrust - Math.PI / 2) * size * 0.3, Math.sin(thrust - Math.PI / 2) * size * 0.3);
        context.fill();
      }
      context.rotate(heading);
      context.fillStyle = finished ? GREEN : color;
      context.beginPath();
      context.moveTo(size, 0); context.lineTo(-size * 0.7, size * 0.55); context.lineTo(-size * 0.4, 0); context.lineTo(-size * 0.7, -size * 0.55);
      context.closePath(); context.fill();
      context.restore();
    };
    population.forEach((flight, i) => rocket(flight, snapshot.population[i].genes, '#3973d6c0', 5));
    rocket(best, snapshot.best.genes, GREEN, 7);

    const landed = population.filter(flight => flight.outcome === 'landed' && at(flight) === flight.path.length - 1).length;
    const crashed = population.filter(flight => flight.outcome === 'crashed' && at(flight) === flight.path.length - 1).length;
    text(context, `Paso ${frame} / ${snapshot.problem.size}`, left + 8, top + 16, 10, '#6a7482');
    context.textAlign = 'right';
    text(context, `${landed} en la diana`, px(WORLD_SIZE) - 8, top + 16, 10, GREEN);
    text(context, `${crashed} estrellados`, px(WORLD_SIZE) - 8, top + 30, 10, '#b83e4c');
    context.textAlign = 'left';
  };
}

function walkerPainter(snapshot: Snapshot): Painter {
  return (context, width, height, time) => {
    const world = snapshot.problem.walker!;
    const body = BODIES[world.creature];
    const { best, ghosts } = walkerRuns(snapshot);
    const steps = world.duration * STEPS_PER_SECOND;
    const frame = flightFrame(snapshot, time, steps, 1000 / STEPS_PER_SECOND);
    const pose = (run: WalkResult) => run.frames![Math.min(frame, run.frames!.length - 1)];
    const centre = (positions: Float32Array) => {
      let sum = 0;
      for (let i = 0; i < positions.length; i += 2) sum += positions[i];
      return sum / (positions.length / 2);
    };
    const current = pose(best);
    const startX = centre(best.frames![0]);
    const bestX = centre(current);
    // Unos 7 m de pista en escritorio; en pantallas estrechas se acerca la cámara.
    const scale = Math.min(Math.max(width / 7, Math.min(width / 4.5, 90)), height / 2.8);
    const ground = height * 0.8;
    const cameraX = bestX - width / scale * 0.4;
    const sx = (x: number) => (x - cameraX) * scale;
    const sy = (y: number) => ground - y * scale;
    const viewEnd = cameraX + width / scale;

    context.fillStyle = '#f7f9fb';
    context.fillRect(0, 0, width, ground);
    context.fillStyle = '#e4e9ef';
    context.beginPath();
    context.moveTo(0, height);
    for (let x = cameraX; x <= viewEnd + 0.1; x += 0.1) context.lineTo(sx(x), sy(terrainHeight(world.terrain, x)));
    context.lineTo(width, height);
    context.closePath(); context.fill();
    context.strokeStyle = '#c7d0db';
    context.lineWidth = 1.5;
    context.beginPath();
    for (let x = cameraX; x <= viewEnd + 0.1; x += 0.1) {
      if (x === cameraX) context.moveTo(sx(x), sy(terrainHeight(world.terrain, x)));
      else context.lineTo(sx(x), sy(terrainHeight(world.terrain, x)));
    }
    context.stroke();
    context.textAlign = 'center';
    for (let metre = Math.ceil(cameraX - startX + 0.2); startX + metre <= viewEnd - 0.2; metre++) {
      const x = startX + metre;
      const y = sy(terrainHeight(world.terrain, x));
      context.strokeStyle = metre === 0 ? GREEN : '#b6c0cc';
      context.lineWidth = metre === 0 ? 2 : 1;
      context.beginPath(); context.moveTo(sx(x), y); context.lineTo(sx(x), y + 7); context.stroke();
      text(context, metre === 0 ? 'Salida' : `${metre} m`, sx(x), y + 20, 10, metre === 0 ? GREEN : '#818b98');
    }
    context.textAlign = 'left';

    const down = (run: WalkResult) => run.fallen && frame >= run.frames!.length - 1;
    const creature = (positions: Float32Array, ghost: boolean, fallen: boolean) => {
      const point = (i: number) => [sx(positions[2 * i]), sy(positions[2 * i + 1])] as const;
      context.lineCap = 'round';
      for (const [a, b] of body.muscles) {
        context.strokeStyle = ghost ? (fallen ? '#d35a6222' : '#3973d633') : '#d35a62bb';
        context.lineWidth = ghost ? 1.5 : 3;
        context.beginPath(); context.moveTo(...point(a)); context.lineTo(...point(b)); context.stroke();
      }
      for (const [a, b] of body.bones) {
        context.strokeStyle = ghost ? (fallen ? '#d35a6255' : '#3973d655') : fallen ? '#9a5a60' : INK;
        context.lineWidth = ghost ? 2 : 4;
        context.beginPath(); context.moveTo(...point(a)); context.lineTo(...point(b)); context.stroke();
      }
      if (ghost) return;
      body.nodes.forEach((_, i) => {
        const [x, y] = point(i);
        context.fillStyle = body.fragile.includes(i) ? GREEN : '#ffffff';
        context.strokeStyle = INK;
        context.lineWidth = 1.5;
        context.beginPath(); context.arc(x, y, 4, 0, Math.PI * 2); context.fill(); context.stroke();
      });
    };
    ghosts.forEach(run => creature(pose(run), true, down(run)));
    const fallen = down(best);
    creature(current, false, fallen);

    // Barra de carrera con la posición de todos los participantes, aunque queden fuera de cámara.
    const positions = ghosts.map(run => centre(pose(run)) - startX);
    const low = Math.min(0, ...positions, bestX - startX) - 0.5;
    const high = Math.max(...positions, bestX - startX) + 0.5;
    const barLeft = 12, barRight = width - 12, barY = 44;
    const bx = (distance: number) => barLeft + (distance - low) / (high - low) * (barRight - barLeft);
    context.strokeStyle = '#dfe5ec';
    context.lineWidth = 2;
    context.beginPath(); context.moveTo(barLeft, barY); context.lineTo(barRight, barY); context.stroke();
    context.fillStyle = GREEN;
    context.fillRect(bx(0) - 1, barY - 5, 2, 10);
    ghosts.forEach((run, i) => {
      context.fillStyle = down(run) ? '#d35a62aa' : '#3973d699';
      context.beginPath(); context.arc(bx(positions[i]), barY, 3.5, 0, Math.PI * 2); context.fill();
    });
    context.fillStyle = fallen ? '#9a5a60' : INK;
    context.beginPath(); context.arc(bx(bestX - startX), barY, 5, 0, Math.PI * 2); context.fill();

    text(context, `t = ${(Math.min(frame, steps) / STEPS_PER_SECOND).toFixed(1)} s / ${world.duration} s`, 12, 18, 10, '#6a7482');
    context.textAlign = 'right';
    text(context, `${(bestX - startX).toFixed(2)} m`, width - 12, 18, 12, GREEN);
    if (fallen) text(context, 'Se ha caído', width - 12, 34, 10, '#b83e4c');
    context.textAlign = 'left';
  };
}

export function solutionPainter(snapshot: Snapshot): Painter {
  if (snapshot.problem.id === 'rockets') return rocketPainter(snapshot);
  if (snapshot.problem.id === 'walker') return walkerPainter(snapshot);
  return (context, width, height) => {
    const { problem, best } = snapshot;
    const genes = best.genes;
    if (problem.id === 'queens') {
      const cell = Math.min((width - 50) / problem.size, (height - 42) / problem.size);
      const left = (width - cell * problem.size) / 2;
      const top = (height - cell * problem.size) / 2;
      const conflicts = new Set<number>();
      for (let i = 0; i < genes.length; i++) for (let j = i + 1; j < genes.length; j++) {
        if (genes[i] === genes[j] || Math.abs(genes[i] - genes[j]) === j - i) {
          conflicts.add(i); conflicts.add(j);
        }
      }
      for (let row = 0; row < problem.size; row++) for (let column = 0; column < problem.size; column++) {
        context.fillStyle = (row + column) % 2 === 0 ? '#f3f5f7' : '#e0e6ec';
        context.fillRect(left + column * cell, top + row * cell, cell, cell);
      }
      for (let row = 0; row < problem.size; row++) {
        const x = left + genes[row] * cell;
        const y = top + row * cell;
        context.fillStyle = conflicts.has(row) ? '#f9dfe2' : '#d6eee1';
        context.fillRect(x + 1, y + 1, cell - 2, cell - 2);
        context.textAlign = 'center';
        context.textBaseline = 'middle';
        text(context, '\u265b', x + cell / 2, y + cell / 2 + 1, cell * 0.73, conflicts.has(row) ? '#b83e4c' : GREEN);
        context.textBaseline = 'alphabetic';
      }
      context.textAlign = 'center';
      for (let i = 0; i < problem.size; i++) {
        text(context, String(i + 1), left + i * cell + cell / 2, top + cell * problem.size + 17, 10, '#7b8491');
      }
      context.textAlign = 'left';
    } else if (problem.permutation) {
      const scale = Math.min(width - 70, height - 46) / 100;
      const left = (width - scale * 100) / 2;
      const top = (height - scale * 100) / 2;
      const position = (city: number) => ({ x: left + problem.points[city].x * scale, y: top + problem.points[city].y * scale });
      context.strokeStyle = GRID;
      context.lineWidth = 1;
      for (let i = 0; i <= 100; i += 20) {
        context.beginPath(); context.moveTo(left + i * scale, top); context.lineTo(left + i * scale, top + 100 * scale); context.stroke();
        context.beginPath(); context.moveTo(left, top + i * scale); context.lineTo(left + 100 * scale, top + i * scale); context.stroke();
      }
      context.strokeStyle = BLUE;
      context.lineWidth = 2;
      context.lineJoin = 'round';
      context.beginPath();
      genes.forEach((city, index) => {
        const point = position(city);
        if (index === 0) context.moveTo(point.x, point.y);
        else context.lineTo(point.x, point.y);
      });
      context.closePath(); context.stroke();
      problem.points.forEach((_, city) => {
        const point = position(city);
        context.fillStyle = city === genes[0] ? GREEN : INK;
        context.beginPath(); context.arc(point.x, point.y, 4.5, 0, 2 * Math.PI); context.fill();
        text(context, String(city + 1), point.x + 7, point.y - 7, 10, '#6a7482');
      });
    } else if (problem.id === 'sequence') {
      const columns = Math.min(problem.size, Math.max(4, Math.floor((width - 35) / 36)));
      const cell = Math.min(44, (width - 35) / columns);
      const rows = Math.ceil(problem.size / columns);
      const blockHeight = Math.min(96, (height - 45) / rows);
      const top = (height - rows * blockHeight) / 2;
      const left = (width - columns * cell) / 2;
      for (let i = 0; i < problem.size; i++) {
        const x = left + (i % columns) * cell;
        const y = top + Math.floor(i / columns) * blockHeight;
        context.fillStyle = '#f0f3f6';
        context.fillRect(x, y, cell - 4, 30);
        context.fillStyle = genes[i] === problem.target[i] ? '#d6eee1' : '#f9dfe2';
        context.fillRect(x, y + 36, cell - 4, 30);
        context.textAlign = 'center';
        text(context, String(problem.target[i]), x + (cell - 4) / 2, y + 21, 19, INK);
        text(context, String(genes[i]), x + (cell - 4) / 2, y + 57, 19, genes[i] === problem.target[i] ? GREEN : RED);
      }
      context.textAlign = 'left';
    } else if (problem.id === 'knapsack') {
      const columns = Math.max(4, Math.floor((width - 48) / 65));
      const rows = Math.ceil(problem.size / columns);
      const gap = 8;
      const cellWidth = (width - 48 - (columns - 1) * gap) / columns;
      const cellHeight = Math.min(74, (height - 50 - (rows - 1) * gap) / rows);
      const top = (height - rows * cellHeight - (rows - 1) * gap) / 2;
      for (let i = 0; i < problem.size; i++) {
        const x = 24 + (i % columns) * (cellWidth + gap);
        const y = top + Math.floor(i / columns) * (cellHeight + gap);
        context.fillStyle = genes[i] ? '#e4f3eb' : '#f2f4f7';
        context.fillRect(x, y, cellWidth, cellHeight);
        if (cellHeight > 43) text(context, String(i + 1).padStart(2, '0'), x + 8, y + 15, 10, genes[i] ? GREEN : '#818b98');
        text(context, `v ${problem.values[i]}`, x + 8, y + cellHeight * 0.63, Math.min(14, cellHeight * 0.4), genes[i] ? GREEN : INK);
        if (cellWidth > 55 && cellHeight > 48) text(context, `p ${problem.weights[i]}`, x + 8, y + cellHeight - 8, 10, '#818b98');
      }
    } else {
      const left = 40, right = width - 22, top = 22, bottom = height - 32;
      const samples = Array.from({ length: 501 }, (_, i) => ({ x: -100 + i * 0.4, y: objective(-100 + i * 0.4) }));
      const minY = Math.min(...samples.map(point => point.y));
      const maxY = Math.max(...samples.map(point => point.y));
      const px = (x: number) => left + (x + 100) / 200 * (right - left);
      const py = (y: number) => bottom - (y - minY) / (maxY - minY) * (bottom - top);
      context.strokeStyle = GRID;
      for (let tick = 0; tick <= 4; tick++) {
        const y = top + tick * (bottom - top) / 4;
        context.beginPath(); context.moveTo(left, y); context.lineTo(right, y); context.stroke();
      }
      context.strokeStyle = '#7a8799';
      context.lineWidth = 1.5;
      context.beginPath();
      samples.forEach((point, i) => i ? context.lineTo(px(point.x), py(point.y)) : context.moveTo(px(point.x), py(point.y)));
      context.stroke();
      snapshot.population.slice(0, 80).forEach(individual => {
        const x = binaryToReal(individual.genes);
        context.fillStyle = '#3973d650';
        context.beginPath(); context.arc(px(x), py(individual.fitness), 2.5, 0, Math.PI * 2); context.fill();
      });
      const x = binaryToReal(genes);
      context.fillStyle = GREEN;
      context.beginPath(); context.arc(px(x), py(best.fitness), 6, 0, Math.PI * 2); context.fill();
      for (const tick of [-100, -50, 0, 50, 100]) text(context, String(tick), px(tick) - 8, height - 10, 10, '#818b98');
    }
  };
}

export function populationPainter(snapshot: Snapshot): Painter {
  return (context, width, height) => {
    const columns = Math.ceil(Math.sqrt(snapshot.population.length * width / height));
    const rows = Math.ceil(snapshot.population.length / columns);
    const size = Math.min((width - 40) / columns, (height - 32) / rows);
    const left = (width - columns * size) / 2;
    const top = (height - rows * size) / 2;
    const scores = snapshot.population.map(individual => individual.fitness);
    const min = Math.min(...scores), max = Math.max(...scores);
    snapshot.population.forEach((individual, i) => {
      let value = min === max ? 0.5 : (individual.fitness - min) / (max - min);
      if (snapshot.problem.minimize) value = 1 - value;
      context.fillStyle = `hsl(${10 + value * 140} 47% ${86 - value * 40}%)`;
      context.fillRect(left + (i % columns) * size + 2, top + Math.floor(i / columns) * size + 2, size - 4, size - 4);
      if (size > 35) {
        context.textAlign = 'center';
        text(context, individual.fitness.toFixed(snapshot.problem.permutation || snapshot.problem.id === 'function' ? 1 : 0),
          left + (i % columns + 0.5) * size, top + (Math.floor(i / columns) + 0.58) * size, 10, value > 0.7 ? '#fff' : INK);
      }
    });
    context.textAlign = 'left';
  };
}

export function evolutionPainter(snapshot: Snapshot): Painter {
  return (context, width, height) => {
    const left = 43, right = width - 15, top = 16, bottom = height - 27;
    const allValues = snapshot.history.flatMap(point => [point.best, point.average]);
    const low = Math.min(...allValues);
    const high = Math.max(...allValues);
    const margin = Math.max((high - low) * 0.12, 0.5);
    const min = low - margin, max = high + margin;
    const last = Math.max(1, snapshot.generation);
    const px = (generation: number) => left + generation / last * (right - left);
    const py = (fitness: number) => bottom - (fitness - min) / (max - min) * (bottom - top);
    context.lineWidth = 1;
    for (let tick = 0; tick <= 3; tick++) {
      const fitness = min + (max - min) * tick / 3;
      const y = py(fitness);
      context.strokeStyle = GRID;
      context.beginPath(); context.moveTo(left, y); context.lineTo(right, y); context.stroke();
      text(context, fitness.toFixed(max > 100 ? 0 : 1), 3, y + 4, 10, '#818b98');
      const generation = Math.round(last * tick / 3);
      text(context, String(generation), px(generation) - 3, height - 6, 10, '#818b98');
    }
    for (const [field, color] of [['average', BLUE], ['best', GREEN]] as const) {
      context.strokeStyle = color;
      context.lineWidth = 2;
      context.beginPath();
      snapshot.history.forEach((point, i) => i ? context.lineTo(px(point.generation), py(point[field])) : context.moveTo(px(point.generation), py(point[field])));
      context.stroke();
      const point = snapshot.history[snapshot.history.length - 1];
      context.fillStyle = color;
      context.beginPath(); context.arc(px(point.generation), py(point[field]), 3, 0, Math.PI * 2); context.fill();
    }
  };
}

export function histogramPainter(snapshot: Snapshot): Painter {
  return (context, width, height) => {
    const values = snapshot.population.map(individual => individual.fitness);
    const minimum = Math.min(...values), maximum = Math.max(...values);
    const low = minimum === maximum ? minimum - 0.5 : minimum;
    const high = minimum === maximum ? maximum + 0.5 : maximum;
    const bins = Array<number>(10).fill(0);
    values.forEach(value => bins[Math.min(9, Math.floor((value - low) / (high - low) * 10))]++);
    const peak = Math.max(...bins, 1);
    const left = 25, right = width - 20, bottom = height - 28;
    const barWidth = (right - left) / bins.length;
    context.strokeStyle = GRID;
    for (let tick = 0; tick <= 3; tick++) {
      const y = bottom - tick * (bottom - 22) / 3;
      context.beginPath(); context.moveTo(left, y); context.lineTo(right, y); context.stroke();
    }
    bins.forEach((count, i) => {
      const barHeight = count / peak * (bottom - 22);
      context.fillStyle = '#96b5e9';
      context.fillRect(left + i * barWidth + 2, bottom - barHeight, barWidth - 4, barHeight);
    });
    text(context, low.toFixed(maximum > 100 ? 0 : 1), left, height - 7, 10, '#818b98');
    context.textAlign = 'right';
    text(context, high.toFixed(maximum > 100 ? 0 : 1), right, height - 7, 10, '#818b98');
    context.textAlign = 'left';
  };
}
