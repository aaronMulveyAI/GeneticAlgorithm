import { packedWeight } from './knapsack';
import type { Painter } from './Canvas';
import type { Problem, Snapshot } from './types';

const INK = '#23272f';
const MUTED = '#818b98';
const GREEN = '#168153';
const RED = '#d35a62';
const ORANGE = '#e08a2e';

function text(context: CanvasRenderingContext2D, value: string, x: number, y: number, size = 11, colour = INK,
  align: CanvasTextAlign = 'left', weight = 400) {
  context.font = `${weight} ${size}px system-ui, sans-serif`;
  context.fillStyle = colour;
  context.textAlign = align;
  context.fillText(value, x, y);
  context.textAlign = 'left';
}

const ratios = (problem: Problem) => problem.values.map((value, i) => value / problem.weights[i]);

// Utilidad por kilo de 0 (la peor) a 1 (la mejor) dentro de esta mochila.
function efficiency(problem: Problem): number[] {
  const all = ratios(problem);
  const low = Math.min(...all), high = Math.max(...all);
  return all.map(ratio => high === low ? 0.5 : (ratio - low) / (high - low));
}

function blend(t: number): string {
  const from = [207, 231, 218], to = [22, 129, 83];
  return `rgb(${from.map((channel, i) => Math.round(channel + (to[i] - channel) * t)).join(' ')})`;
}

const reducedMotion = () => typeof matchMedia === 'function' && matchMedia('(prefers-reduced-motion: reduce)').matches;

// Cada objeto entra o sale de la mochila creciendo o encogiendo; la pila se reacomoda sola.
const GROW_MS = 380;
let packing: { key: string; snapshot: Snapshot | null; from: number[]; to: number[]; start: number } =
  { key: '', snapshot: null, from: [], to: [], start: 0 };

function packedShares(snapshot: Snapshot, time: number): number[] {
  const key = snapshot.problem.items!.join(',') + snapshot.problem.capacity;
  const target = snapshot.best.genes;
  const current = () => {
    const t = reducedMotion() ? 1 : Math.min(1, (time - packing.start) / GROW_MS);
    const ease = 1 - (1 - t) ** 3;
    return packing.to.map((value, i) => packing.from[i] + (value - packing.from[i]) * ease);
  };
  if (packing.key !== key) packing = { key, snapshot, from: [...target], to: [...target], start: time };
  else if (packing.snapshot !== snapshot) packing = { key, snapshot, from: current(), to: [...target], start: time };
  return current();
}

export function overweight(snapshot: Snapshot): number {
  const { weights, capacity } = snapshot.problem;
  return snapshot.population.filter(individual => packedWeight(individual.genes, weights) > capacity).length;
}

export function packPainter(snapshot: Snapshot): Painter {
  return (context, width, height, time) => {
    const problem = snapshot.problem;
    const { weights, values, capacity } = problem;
    const names = problem.items!;
    const shares = packedShares(snapshot, time);
    const tone = efficiency(problem);
    const narrow = width < 520;

    const bagWidth = Math.min(230, narrow ? width * 0.5 : width * 0.36);
    const bagLeft = narrow ? 16 : Math.max(24, width * 0.08);
    const bagTop = 58, bagBottom = height - 24;
    const capacityY = bagTop + (bagBottom - bagTop) * 0.14;
    const kg = (bagBottom - capacityY) / capacity;

    // Mochila: cuerpo, solapa y tirantes.
    context.fillStyle = '#e4e9ef';
    context.beginPath(); context.roundRect(bagLeft - 10, bagTop + 26, 12, (bagBottom - bagTop) * 0.55, 6); context.fill();
    context.beginPath(); context.roundRect(bagLeft + bagWidth - 2, bagTop + 26, 12, (bagBottom - bagTop) * 0.55, 6); context.fill();
    context.fillStyle = '#f3f5f8';
    context.strokeStyle = '#c7d0db';
    context.lineWidth = 2;
    context.beginPath(); context.roundRect(bagLeft, bagTop, bagWidth, bagBottom - bagTop, [26, 26, 12, 12]); context.fill(); context.stroke();
    context.strokeStyle = '#c7d0db';
    context.beginPath(); context.arc(bagLeft + bagWidth / 2, bagTop, 16, Math.PI, 0); context.stroke();

    const inner = { left: bagLeft + 10, width: bagWidth - 20 };
    let y = bagBottom - 8;
    let load = 0;
    context.save();
    context.beginPath(); context.roundRect(bagLeft, bagTop - 60, bagWidth, bagBottom - bagTop + 60, [26, 26, 12, 12]); context.clip();
    shares.forEach((share, i) => {
      if (share < 0.01) return;
      const blockHeight = weights[i] * kg * share;
      y -= blockHeight;
      load += weights[i] * share;
      const inset = (1 - share) * inner.width * 0.2;
      context.globalAlpha = Math.min(1, share * 1.4);
      context.fillStyle = blend(tone[i]);
      context.beginPath(); context.roundRect(inner.left + inset, y + 1, inner.width - 2 * inset, Math.max(0, blockHeight - 2), 4); context.fill();
      if (y < capacityY) {
        context.fillStyle = '#d35a6299';
        const overflow = Math.min(blockHeight, capacityY - y);
        context.fillRect(inner.left + inset, y + 1, inner.width - 2 * inset, overflow - 1);
      }
      if (blockHeight >= 13 && share > 0.9) {
        const colour = tone[i] > 0.55 ? '#ffffff' : INK;
        const size = Math.min(11, blockHeight - 3);
        text(context, `${names[i]} · ${weights[i]} kg`, inner.left + 8, y + blockHeight / 2 + size * 0.36, size, colour);
        text(context, String(values[i]), inner.left + inner.width - 8, y + blockHeight / 2 + size * 0.36, size, colour, 'right', 600);
      }
      context.globalAlpha = 1;
    });
    context.restore();

    context.strokeStyle = RED;
    context.lineWidth = 1.5;
    context.setLineDash([6, 4]);
    context.beginPath(); context.moveTo(bagLeft - 14, capacityY); context.lineTo(bagLeft + bagWidth + 14, capacityY); context.stroke();
    context.setLineDash([]);
    text(context, `capacidad ${capacity} kg`, bagLeft + bagWidth - 12, capacityY - 6, 10, RED, 'right');
    const weight = packedWeight(snapshot.best.genes, weights);
    const over = weight > capacity;
    text(context, 'MEJOR HISTÓRICO', bagLeft, 18, 10, MUTED);
    text(context, `${Math.round(load)} / ${capacity} kg`, bagLeft, 38, 15, over ? RED : INK, 'left', 600);
    text(context, `utilidad ${snapshot.best.fitness}`, bagLeft + bagWidth, 38, 12, GREEN, 'right', 600);

    // Objetos que se quedan fuera, de más a menos utilidad por kilo.
    const panelLeft = bagLeft + bagWidth + (narrow ? 22 : 64);
    const panelWidth = width - panelLeft - 12;
    const outside = names.map((_, i) => i).filter(i => !snapshot.best.genes[i])
      .sort((a, b) => values[b] / weights[b] - values[a] / weights[a]);
    const optimum = problem.optimum!;
    text(context, `SE QUEDAN FUERA (${outside.length})`, panelLeft, 18, 10, MUTED);
    const columnWidth = narrow ? panelWidth : Math.max(150, Math.min(190, panelWidth / 2));
    const columns = Math.max(1, Math.floor(panelWidth / columnWidth));
    const rowHeight = narrow ? 15 : 17;
    const rows = Math.max(1, Math.floor((height - (narrow ? 125 : 110)) / rowHeight));
    outside.slice(0, rows * columns).forEach((item, n) => {
      const x = panelLeft + Math.floor(n / rows) * columnWidth;
      const rowY = 40 + (n % rows) * rowHeight;
      const missing = optimum.genes[item] === 1;
      context.fillStyle = missing ? ORANGE : '#c7d0db';
      context.beginPath(); context.arc(x + 4, rowY - 4, 3.5, 0, Math.PI * 2); context.fill();
      text(context, names[item], x + 13, rowY, narrow ? 10 : 11, missing ? INK : '#4b5562');
      if (!narrow || columnWidth > 140) text(context, `${weights[item]} kg · ${values[item]}`, x + columnWidth - 14, rowY, 10, MUTED, 'right');
    });
    if (outside.length > rows * columns) text(context, `y ${outside.length - rows * columns} más…`, panelLeft, 40 + rows * rowHeight, 10, MUTED);

    const percent = optimum.value ? snapshot.best.fitness / optimum.value * 100 : 100;
    const lines = percent >= 100 ? ['Ha encontrado el', `óptimo exacto: ${optimum.value}`]
      : [`Óptimo exacto ${optimum.value}`, `el algoritmo lleva el ${percent.toFixed(1).replace('.', ',')} %`];
    context.font = '600 11px system-ui, sans-serif';
    const oneLine = lines.join(percent >= 100 ? ' ' : ' · ');
    const colour = percent >= 100 ? GREEN : INK;
    if (context.measureText(oneLine).width <= panelWidth) text(context, oneLine, panelLeft, height - 14, 11, colour, 'left', 600);
    else lines.map(line => line.replace('el algoritmo lleva', 'lleva'))
      .forEach((line, i) => text(context, line, panelLeft, height - 30 + i * 15, 11, colour, 'left', 600));
  };
}

export function scatterPainter(snapshot: Snapshot): Painter {
  return (context, width, height) => {
    const problem = snapshot.problem;
    const { weights, values } = problem;
    const optimum = problem.optimum!.genes;
    const chosen = snapshot.best.genes;
    const left = 44, right = width - 18, top = 34, bottom = height - 34;
    const maxWeight = Math.max(...weights) + 1, maxValue = Math.max(...values) + 1;
    const px = (weight: number) => left + weight / maxWeight * (right - left);
    const py = (value: number) => bottom - value / maxValue * (bottom - top);

    context.strokeStyle = '#eef1f4';
    context.lineWidth = 1;
    for (let w = 0; w <= maxWeight; w += 2) {
      context.beginPath(); context.moveTo(px(w), top); context.lineTo(px(w), bottom); context.stroke();
      text(context, String(w), px(w), bottom + 14, 10, MUTED, 'center');
    }
    for (let v = 0; v <= maxValue; v += 5) {
      context.beginPath(); context.moveTo(left, py(v)); context.lineTo(right, py(v)); context.stroke();
      text(context, String(v), left - 8, py(v) + 3, 10, MUTED, 'right');
    }
    text(context, 'peso (kg)', right, bottom + 28, 10, MUTED, 'right');
    text(context, 'utilidad', 8, top - 10, 10, MUTED);

    // Rayos de utilidad por kilo: cuanto más empinado, mejor negocio.
    context.save();
    context.beginPath(); context.rect(left, top, right - left, bottom - top); context.clip();
    for (const ratio of [1, 2, 4]) {
      context.strokeStyle = '#dfe5ec';
      context.setLineDash([4, 4]);
      context.beginPath(); context.moveTo(px(0), py(0)); context.lineTo(px(maxWeight), py(maxWeight * ratio)); context.stroke();
      context.setLineDash([]);
      const labelWeight = Math.min(maxWeight * 0.92, maxValue * 0.92 / ratio);
      text(context, `${ratio} por kg`, px(labelWeight) + 4, py(labelWeight * ratio) - 4, 10, '#a6b0bc');
    }
    context.restore();

    // Puntos repetidos (mismo peso y utilidad) se reparten alrededor de su posición.
    const seen = new Map<string, number>();
    const labels: { x: number; y: number; name: string; colour: string }[] = [];
    weights.forEach((weight, i) => {
      const key = `${weight}:${values[i]}`;
      const repeat = seen.get(key) ?? 0;
      seen.set(key, repeat + 1);
      const angle = repeat * 2.4;
      const x = px(weight) + (repeat ? Math.cos(angle) * 7 : 0);
      const y = py(values[i]) + (repeat ? Math.sin(angle) * 7 : 0);
      const inside = chosen[i] === 1, best = optimum[i] === 1;
      context.lineWidth = 2;
      context.beginPath(); context.arc(x, y, 6, 0, Math.PI * 2);
      context.fillStyle = inside ? GREEN : '#ffffff';
      context.strokeStyle = inside ? (best ? GREEN : RED) : (best ? ORANGE : '#b6c0cc');
      context.fill(); context.stroke();
      if (inside !== best) labels.push({ x, y, name: problem.items![i], colour: inside ? RED : ORANGE });
    });
    for (const label of labels.slice(0, 8)) text(context, label.name, label.x + 10, label.y + 4, 10, label.colour, 'left', 600);

    const legend: [string, string, string][] = [[GREEN, GREEN, 'en la mochila'], ['#ffffff', '#b6c0cc', 'fuera'],
      ['#ffffff', ORANGE, 'el óptimo lo lleva'], [GREEN, RED, 'el óptimo no lo lleva']];
    let x = left + 30;
    for (const [fill, stroke, name] of legend) {
      if (x > right - 60) break;
      context.lineWidth = 2;
      context.beginPath(); context.arc(x, 14, 5, 0, Math.PI * 2);
      context.fillStyle = fill; context.strokeStyle = stroke; context.fill(); context.stroke();
      text(context, name, x + 9, 18, 10, MUTED);
      context.font = '10px system-ui, sans-serif';
      x += context.measureText(name).width + 28;
    }
  };
}
