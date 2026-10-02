import { decodePoint, heightGrid, LANDSCAPES, normalizedHeight } from './landscapes';
import type { Painter } from './Canvas';
import type { Landscape, Point, Snapshot } from './types';

type Landscape2d = Exclude<Landscape, 'original'>;
const INK = '#23272f';
const BEST = '#d35a62';
const OPTIMUM = '#ffd166';
const MUTED = '#818b98';

// Del valle (azul profundo) a los picos (arena clara).
const STOPS: [number, [number, number, number]][] = [
  [0, [23, 63, 115]], [0.22, [42, 111, 176]], [0.45, [58, 163, 138]], [0.72, [201, 194, 90]], [1, [241, 230, 200]],
];

function colour(t: number, light = 1): [number, number, number] {
  const value = Math.min(1, Math.max(0, t));
  let i = 1;
  while (i < STOPS.length - 1 && STOPS[i][0] < value) i++;
  const [t0, c0] = STOPS[i - 1], [t1, c1] = STOPS[i];
  const k = (value - t0) / (t1 - t0);
  return c0.map((channel, j) => Math.round(Math.min(255, (channel + (c1[j] - channel) * k) * light))) as [number, number, number];
}

const reducedMotion = () => typeof matchMedia === 'function' && matchMedia('(prefers-reduced-motion: reduce)').matches;

// Al llegar una generación nueva, cada punto se desliza desde la posición que se veía en pantalla.
let tween: { landscape: Landscape2d; snapshot: Snapshot; from: Point[]; to: Point[]; start: number } | null = null;
const TWEEN_MS = 350;

function interpolate(time: number): Point[] {
  const { from, to, start } = tween!;
  const t = reducedMotion() ? 1 : Math.min(1, Math.max(0, (time - start) / TWEEN_MS));
  const ease = 1 - (1 - t) ** 3;
  return to.map((point, i) => ({ x: from[i].x + (point.x - from[i].x) * ease, y: from[i].y + (point.y - from[i].y) * ease }));
}

function populationPoints(snapshot: Snapshot, time: number): Point[] {
  const landscape = snapshot.problem.landscape as Landscape2d;
  if (!tween || tween.snapshot !== snapshot) {
    const target = snapshot.population.map(individual => decodePoint(landscape, individual.genes));
    const from = tween && tween.landscape === landscape && tween.to.length === target.length ? interpolate(time) : target;
    tween = { landscape, snapshot, from, to: target, start: time };
  }
  return interpolate(time);
}

const view = { yaw: 0.75, pitch: 0.78 };

export function rotateSurface(dx: number, dy: number): void {
  view.yaw -= dx * 0.008;
  view.pitch = Math.min(1.3, Math.max(0.3, view.pitch + dy * 0.006));
}

const GRID = 48;
const RELIEF = 0.55;
const shading = new Map<Landscape2d, { heights: Float64Array; light: Float64Array }>();

// Alturas normalizadas de la malla y una iluminación fija sobre el terreno para dar relieve.
function surfaceModel(landscape: Landscape2d) {
  let model = shading.get(landscape);
  if (!model) {
    const grid = heightGrid(landscape, GRID);
    const heights = grid.values.map(value => normalizedHeight(landscape, value) * RELIEF);
    const light = new Float64Array((GRID - 1) * (GRID - 1));
    const step = 2 / (GRID - 1);
    const sun = [-0.45, -0.55, 0.7];
    const length = Math.hypot(...sun);
    for (let row = 0; row < GRID - 1; row++) for (let column = 0; column < GRID - 1; column++) {
      const h = (r: number, c: number) => heights[r * GRID + c];
      const dx = (h(row, column + 1) + h(row + 1, column + 1) - h(row, column) - h(row + 1, column)) / (2 * step);
      const dy = (h(row + 1, column) + h(row + 1, column + 1) - h(row, column) - h(row, column + 1)) / (2 * step);
      const norm = Math.hypot(dx, dy, 1);
      const lambert = (-dx * sun[0] - dy * sun[1] + sun[2]) / (norm * length);
      light[row * (GRID - 1) + column] = 0.62 + 0.45 * Math.max(0, lambert);
    }
    model = { heights, light };
    shading.set(landscape, model);
  }
  return model;
}

function dot(context: CanvasRenderingContext2D, x: number, y: number) {
  context.fillStyle = '#ffffffe6';
  context.strokeStyle = '#23272fb0';
  context.lineWidth = 1;
  context.beginPath(); context.arc(x, y, 3.2, 0, Math.PI * 2); context.fill(); context.stroke();
}

function star(context: CanvasRenderingContext2D, x: number, y: number) {
  context.fillStyle = OPTIMUM;
  context.strokeStyle = INK;
  context.lineWidth = 1.2;
  context.beginPath();
  for (let i = 0; i < 10; i++) {
    const radius = i % 2 ? 3.5 : 8.5;
    const angle = -Math.PI / 2 + i * Math.PI / 5;
    if (i) context.lineTo(x + radius * Math.cos(angle), y + radius * Math.sin(angle));
    else context.moveTo(x + radius * Math.cos(angle), y + radius * Math.sin(angle));
  }
  context.closePath(); context.fill(); context.stroke();
}

function bestMarker(context: CanvasRenderingContext2D, x: number, y: number) {
  context.fillStyle = BEST;
  context.strokeStyle = '#ffffff';
  context.lineWidth = 2.5;
  context.beginPath(); context.arc(x, y, 6.5, 0, Math.PI * 2); context.fill(); context.stroke();
}

function markers(context: CanvasRenderingContext2D, points: Point[], best: Point, optima: Point[],
  place: (point: Point) => [number, number]) {
  points.forEach(point => dot(context, ...place(point)));
  optima.forEach(point => star(context, ...place(point)));
  bestMarker(context, ...place(best));
}

function label(context: CanvasRenderingContext2D, value: string, x: number, y: number, align: CanvasTextAlign = 'left') {
  context.font = '10px system-ui, sans-serif';
  context.fillStyle = MUTED;
  context.textAlign = align;
  context.fillText(value, x, y);
  context.textAlign = 'left';
}

const axisLabel = (value: number) => Number.isInteger(value) ? String(value) : value.toFixed(2).replace('.', ',');

export function surfacePainter(snapshot: Snapshot): Painter {
  return (context, width, height, time) => {
    const landscape = snapshot.problem.landscape as Landscape2d;
    const { range: [low, high], f, optima } = LANDSCAPES[landscape];
    const { heights, light } = surfaceModel(landscape);
    const yaw = view.yaw + (reducedMotion() ? 0 : time * 0.00008);
    const cosYaw = Math.cos(yaw), sinYaw = Math.sin(yaw), cosPitch = Math.cos(view.pitch), sinPitch = Math.sin(view.pitch);
    const scale = Math.min((width - 24) / 2.9, (height - 30) / (2.83 * sinPitch + (RELIEF + 0.06) * cosPitch + 0.1));
    const centreX = width / 2;
    const centreY = height / 2 + (RELIEF - 0.06) * cosPitch * scale / 2;
    const toUnit = (value: number) => (value - low) / (high - low) * 2 - 1;
    const project = (u: number, v: number, w: number): [number, number, number] => {
      const rx = u * cosYaw - v * sinYaw;
      const rz = u * sinYaw + v * cosYaw;
      return [centreX + rx * scale, centreY + (rz * sinPitch - w * cosPitch) * scale, rz * cosPitch + w * sinPitch];
    };

    const vertices = new Float64Array(GRID * GRID * 3);
    for (let row = 0; row < GRID; row++) for (let column = 0; column < GRID; column++) {
      const index = row * GRID + column;
      const [x, y, depth] = project(column / (GRID - 1) * 2 - 1, row / (GRID - 1) * 2 - 1, heights[index]);
      vertices[3 * index] = x; vertices[3 * index + 1] = y; vertices[3 * index + 2] = depth;
    }

    // Algoritmo del pintor: caras del terreno y tramos de pared se ordenan juntos por profundidad.
    // Los marcadores se dibujan siempre encima para que la población no desaparezca en los pozos.
    const items: { depth: number; draw: () => void }[] = [];
    context.lineJoin = 'round';
    for (let row = 0; row < GRID - 1; row++) for (let column = 0; column < GRID - 1; column++) {
      const corners = [row * GRID + column, row * GRID + column + 1, (row + 1) * GRID + column + 1, (row + 1) * GRID + column];
      const depth = corners.reduce((sum, corner) => sum + vertices[3 * corner + 2], 0) / 4;
      items.push({ depth, draw: () => {
        const t = corners.reduce((sum, corner) => sum + heights[corner], 0) / 4 / RELIEF;
        const [r, g, b] = colour(t, light[row * (GRID - 1) + column]);
        context.fillStyle = context.strokeStyle = `rgb(${r} ${g} ${b})`;
        context.lineWidth = 0.7;
        context.beginPath();
        corners.forEach((corner, i) => i ? context.lineTo(vertices[3 * corner], vertices[3 * corner + 1])
          : context.moveTo(vertices[3 * corner], vertices[3 * corner + 1]));
        context.closePath(); context.fill(); context.stroke();
      } });
    }
    // Paredes laterales hasta una base algo por debajo del valle más profundo.
    const unit = (i: number) => i / (GRID - 1) * 2 - 1;
    const edges: [number, number, number, number][] = [[0, 0, 1, 0], [0, GRID - 1, 1, 0], [0, 0, 0, 1], [GRID - 1, 0, 0, 1]];
    const centre = project(0, 0, 0)[2];
    for (const [column0, row0, dc, dr] of edges) {
      // Solo las paredes que miran hacia la cámara.
      if (project(unit(column0 + dc * (GRID - 1) / 2), unit(row0 + dr * (GRID - 1) / 2), 0)[2] <= centre) continue;
      for (let i = 0; i < GRID - 1; i++) {
        const top = [i, i + 1].map(k => (row0 + dr * k) * GRID + column0 + dc * k);
        const bottom = [i + 1, i].map(k => project(unit(column0 + dc * k), unit(row0 + dr * k), -0.06));
        const depth = (vertices[3 * top[0] + 2] + vertices[3 * top[1] + 2] + bottom[0][2] + bottom[1][2]) / 4;
        items.push({ depth, draw: () => {
          context.fillStyle = context.strokeStyle = '#c9d2dc';
          context.lineWidth = 0.7;
          context.beginPath();
          context.moveTo(vertices[3 * top[0]], vertices[3 * top[0] + 1]);
          context.lineTo(vertices[3 * top[1]], vertices[3 * top[1] + 1]);
          bottom.forEach(([x, y]) => context.lineTo(x, y));
          context.closePath(); context.fill(); context.stroke();
        } });
      }
    }
    items.sort((a, b) => a.depth - b.depth);
    for (const item of items) item.draw();
    markers(context, populationPoints(snapshot, time), decodePoint(landscape, snapshot.best.genes), optima, point => {
      const [x, y] = project(toUnit(point.x), toUnit(point.y), normalizedHeight(landscape, f(point.x, point.y)) * RELIEF);
      return [x, y];
    });
    label(context, 'Arrastra para girar', 12, 18);
  };
}

const maps = new Map<Landscape2d, HTMLCanvasElement>();
const MAP_SIZE = 180;
const BANDS = 14;

// Mapa de calor con bandas de color; el borde entre bandas se oscurece y hace de curva de nivel.
function heatImage(landscape: Landscape2d): HTMLCanvasElement {
  let canvas = maps.get(landscape);
  if (!canvas) {
    canvas = document.createElement('canvas');
    canvas.width = canvas.height = MAP_SIZE;
    const context = canvas.getContext('2d')!;
    const image = context.createImageData(MAP_SIZE, MAP_SIZE);
    const { range: [low, high], f } = LANDSCAPES[landscape];
    const band = new Int16Array(MAP_SIZE * MAP_SIZE);
    const t = new Float64Array(MAP_SIZE * MAP_SIZE);
    for (let row = 0; row < MAP_SIZE; row++) for (let column = 0; column < MAP_SIZE; column++) {
      const x = low + (high - low) * column / (MAP_SIZE - 1);
      const y = high - (high - low) * row / (MAP_SIZE - 1);
      const index = row * MAP_SIZE + column;
      t[index] = normalizedHeight(landscape, f(x, y));
      band[index] = Math.min(BANDS - 1, Math.floor(t[index] * BANDS));
    }
    for (let index = 0; index < band.length; index++) {
      const column = index % MAP_SIZE;
      const edge = (column + 1 < MAP_SIZE && band[index + 1] !== band[index])
        || (index + MAP_SIZE < band.length && band[index + MAP_SIZE] !== band[index]);
      const [r, g, b] = colour((band[index] + 0.5) / BANDS, edge ? 0.78 : 1);
      image.data.set([r, g, b, 255], index * 4);
    }
    context.putImageData(image, 0, 0);
    maps.set(landscape, canvas);
  }
  return canvas;
}

export function heatmapPainter(snapshot: Snapshot): Painter {
  return (context, width, height, time) => {
    const landscape = snapshot.problem.landscape as Landscape2d;
    const { range: [low, high], optima } = LANDSCAPES[landscape];
    const legend = width > 420;
    const side = Math.max(60, Math.min(width - (legend ? 110 : 44), height - 40));
    const left = (width - side - (legend ? 56 : 0)) / 2 + 8;
    const top = (height - side) / 2 - 4;
    context.imageSmoothingEnabled = true;
    context.drawImage(heatImage(landscape), left, top, side, side);
    context.strokeStyle = '#c7d0db';
    context.strokeRect(left - 0.5, top - 0.5, side + 1, side + 1);
    const px = (x: number) => left + (x - low) / (high - low) * side;
    const py = (y: number) => top + (high - y) / (high - low) * side;
    for (const value of [low, (low + high) / 2, high]) {
      label(context, axisLabel(value), px(value), top + side + 14, 'center');
      label(context, axisLabel(value), left - 6, py(value) + 3, 'right');
    }
    label(context, 'x', left + side + 8, top + side + 14);
    label(context, 'y', left - 6, top + side / 4 + 3, 'right');
    if (legend) {
      const barLeft = left + side + 30, barTop = top + 8, barHeight = side - 16;
      for (let i = 0; i < barHeight; i++) {
        const [r, g, b] = colour(1 - i / barHeight);
        context.fillStyle = `rgb(${r} ${g} ${b})`;
        context.fillRect(barLeft, barTop + i, 10, 1.5);
      }
      label(context, 'alto', barLeft + 14, barTop + 8);
      label(context, 'bajo', barLeft + 14, barTop + barHeight);
    }
    markers(context, populationPoints(snapshot, time), decodePoint(landscape, snapshot.best.genes), optima,
      point => [px(point.x), py(point.y)]);
  };
}
