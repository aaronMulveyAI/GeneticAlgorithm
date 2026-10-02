import { ALPHABET, PALETTE, SPRITE_SIZE } from './sequence';
import type { Painter } from './Canvas';
import type { Individual, Snapshot } from './types';

const GREEN = '#168153';
const MUTED = '#818b98';
const TILE = '#2b313b';
const TILE_EDGE = '#1c2128';
const LETTER = '#ffffff';
const LETTER_OK = '#7fe0a8';

const reducedMotion = () => typeof matchMedia === 'function' && matchMedia('(prefers-reduced-motion: reduce)').matches;

// Los mejores individuos distintos de la generación actual.
function leaders(snapshot: Snapshot, count: number): Individual[] {
  const seen = new Set<string>();
  return [...snapshot.population].sort((a, b) => b.fitness - a.fitness).filter(individual => {
    const key = individual.genes.join(',');
    return !seen.has(key) && Boolean(seen.add(key));
  }).slice(0, count);
}

// Panel de aeropuerto: cada casilla gira letra a letra hasta la nueva. Si el salto es largo,
// solo se ven las últimas vueltas para que la casilla llegue en menos de medio segundo.
const STEP_MS = 55;
const MAX_STEPS = 8;
interface Roll { from: number; to: number; start: number; steps: number }
let board: { key: string; snapshot: Snapshot | null; rolls: Roll[] } = { key: '', snapshot: null, rolls: [] };

function rollState(roll: Roll, time: number): { current: number; next: number; progress: number } {
  if (reducedMotion()) return { current: roll.to, next: roll.to, progress: 0 };
  const elapsed = Math.max(0, time - roll.start) / STEP_MS;
  if (elapsed >= roll.steps) return { current: roll.to, next: roll.to, progress: 0 };
  const done = Math.floor(elapsed);
  const current = (roll.from + done) % ALPHABET.length;
  return { current, next: (current + 1) % ALPHABET.length, progress: elapsed - done };
}

function updateBoard(snapshot: Snapshot, time: number): Roll[] {
  const key = snapshot.problem.target.join(',');
  if (board.key !== key) board = { key, snapshot: null, rolls: snapshot.problem.target.map(() => ({ from: 0, to: 0, start: time, steps: 0 })) };
  if (board.snapshot !== snapshot) {
    board.snapshot = snapshot;
    board.rolls = board.rolls.map((roll, i) => {
      const to = snapshot.best.genes[i];
      if (to === roll.to) return roll;
      const shown = rollState(roll, time).current;
      const distance = (to - shown + ALPHABET.length) % ALPHABET.length;
      const steps = Math.min(distance, MAX_STEPS);
      return { from: (to - steps + ALPHABET.length) % ALPHABET.length, to, start: time, steps };
    });
  }
  return board.rolls;
}

function flapCell(context: CanvasRenderingContext2D, x: number, y: number, width: number, height: number,
  current: number, next: number, progress: number, colour: string) {
  const middle = y + height / 2;
  const half = (char: number, top: boolean, scale = 1) => {
    context.save();
    context.beginPath();
    context.rect(x, top ? y : middle, width, height / 2);
    context.clip();
    context.translate(0, middle);
    context.scale(1, scale);
    context.translate(0, -middle);
    context.fillStyle = TILE;
    context.fillRect(x, y, width, height);
    context.fillStyle = colour;
    context.fillText(ALPHABET[char] === ' ' ? '' : ALPHABET[char], x + width / 2, middle + 1);
    context.restore();
  };
  context.font = `600 ${Math.round(height * 0.62)}px "SFMono-Regular", Consolas, monospace`;
  context.textAlign = 'center';
  context.textBaseline = 'middle';
  if (!progress) {
    half(current, true);
    half(current, false);
  } else {
    half(next, true);
    half(current, false);
    // La solapa cae: primero muestra la mitad superior de la letra actual y luego la inferior de la siguiente.
    if (progress < 0.5) half(current, true, 1 - progress * 2);
    else half(next, false, progress * 2 - 1);
  }
  context.fillStyle = TILE_EDGE;
  context.fillRect(x, middle - 0.75, width, 1.5);
  context.textBaseline = 'alphabetic';
  context.textAlign = 'left';
}

export function phrasePainter(snapshot: Snapshot): Painter {
  return (context, width, height, time) => {
    const { target } = snapshot.problem;
    const rolls = updateBoard(snapshot, time);
    const count = target.length;
    const gap = 3;
    const fit = (width - 24 - (count - 1) * gap) / count;
    const columns = fit >= 22 ? count : Math.max(1, Math.floor((width - 24 + gap) / (22 + gap)));
    const cellWidth = Math.min(36, (width - 24 - (columns - 1) * gap) / columns);
    const cellHeight = cellWidth * 1.4;
    const rows = Math.ceil(count / columns);
    const boardWidth = columns * cellWidth + (columns - 1) * gap;
    const left = (width - boardWidth) / 2;
    const top = 40;

    context.font = '10px system-ui, sans-serif';
    context.fillStyle = MUTED;
    context.fillText('OBJETIVO', left, 22);
    context.font = '12px "SFMono-Regular", Consolas, monospace';
    context.fillStyle = '#4b5562';
    context.fillText(target.map(gene => ALPHABET[gene]).join(''), left + 66, 22);

    rolls.forEach((roll, i) => {
      const { current, next, progress } = rollState(roll, time);
      const x = left + (i % columns) * (cellWidth + gap);
      const y = top + Math.floor(i / columns) * (cellHeight + gap);
      const settled = !progress && current === target[i];
      flapCell(context, x, y, cellWidth, cellHeight, current, next, progress, settled ? LETTER_OK : LETTER);
    });

    // Terminal con los mejores candidatos distintos: en verde las letras acertadas.
    const listTop = top + rows * (cellHeight + gap) + 26;
    const lineHeight = 18;
    const lines = Math.max(0, Math.min(8, Math.floor((height - listTop) / lineHeight)));
    const size = Math.max(9, Math.min(13, (width - 70) / count / 0.62));
    context.font = '10px system-ui, sans-serif';
    context.fillStyle = MUTED;
    if (lines) context.fillText('MEJORES DE LA GENERACIÓN', left, listTop - 8);
    context.font = `${size}px "SFMono-Regular", Consolas, monospace`;
    const advance = context.measureText('M').width;
    leaders(snapshot, lines).forEach((individual, row) => {
      const y = listTop + 6 + row * lineHeight + size * 0.4;
      context.fillStyle = MUTED;
      context.fillText(String(individual.fitness).padStart(2, ' '), left, y);
      individual.genes.forEach((gene, i) => {
        const char = ALPHABET[gene] === ' ' ? '·' : ALPHABET[gene];
        context.fillStyle = gene === target[i] ? GREEN : '#b6c0cc';
        context.fillText(char, left + advance * 3 + i * advance, y);
      });
    });
  };
}

// Pixel art: al llegar un mejor nuevo, los píxeles que cambian se funden desde el anterior.
const FADE_MS = 300;
let fade: { key: string; snapshot: Snapshot | null; from: number[]; to: number[]; start: number } =
  { key: '', snapshot: null, from: [], to: [], start: 0 };

function sprite(context: CanvasRenderingContext2D, genes: number[], x: number, y: number, side: number,
  previous?: number[], alpha = 1) {
  const cell = side / SPRITE_SIZE;
  genes.forEach((gene, i) => {
    const px = x + (i % SPRITE_SIZE) * cell, py = y + Math.floor(i / SPRITE_SIZE) * cell;
    if (previous && previous[i] !== gene && alpha < 1) {
      context.fillStyle = PALETTE[previous[i]];
      context.fillRect(px, py, cell + 0.5, cell + 0.5);
      context.globalAlpha = alpha;
    }
    context.fillStyle = PALETTE[gene];
    context.fillRect(px, py, cell + 0.5, cell + 0.5);
    context.globalAlpha = 1;
  });
}

export function pixelPainter(snapshot: Snapshot): Painter {
  return (context, width, height, time) => {
    const { target } = snapshot.problem;
    const key = target.join(',');
    if (fade.key !== key) fade = { key, snapshot, from: snapshot.best.genes, to: snapshot.best.genes, start: time };
    if (fade.snapshot !== snapshot) {
      const alpha = Math.min(1, (time - fade.start) / FADE_MS);
      const shown = fade.to.map((gene, i) => alpha < 0.5 ? fade.from[i] : gene);
      fade = { key, snapshot, from: shown, to: snapshot.best.genes, start: time };
    }
    const alpha = reducedMotion() ? 1 : Math.min(1, (time - fade.start) / FADE_MS);

    const pad = 16;
    const side = Math.floor(Math.min(height - 2 * pad - 14, width * 0.56));
    const panel = width - side - 3 * pad;
    const left = Math.max(pad, (width - side - panel - pad) / 2);
    const top = (height - side) / 2 + 6;
    context.font = '10px system-ui, sans-serif';
    context.fillStyle = MUTED;
    context.fillText('MEJOR HISTÓRICO', left, top - 8);
    sprite(context, fade.to, left, top, side, fade.from, alpha);
    context.strokeStyle = '#c7d0db';
    context.strokeRect(left - 0.5, top - 0.5, side + 1, side + 1);
    const wrong = snapshot.best.genes.reduce((sum, gene, i) => sum + Number(gene !== target[i]), 0);
    context.strokeStyle = '#d35a62';
    context.lineWidth = 1.5;
    const cell = side / SPRITE_SIZE;
    snapshot.best.genes.forEach((gene, i) => {
      if (gene !== target[i] && wrong <= 24) {
        context.strokeRect(left + (i % SPRITE_SIZE) * cell + 1.5, top + Math.floor(i / SPRITE_SIZE) * cell + 1.5, cell - 3, cell - 3);
      }
    });
    context.lineWidth = 1;

    const panelLeft = left + side + pad * 1.5;
    const thumb = Math.max(24, Math.min(64, (panel - pad) / 3));
    context.fillStyle = MUTED;
    context.fillText('OBJETIVO', panelLeft, top - 8);
    sprite(context, target, panelLeft, top, thumb);
    context.strokeStyle = '#c7d0db';
    context.strokeRect(panelLeft - 0.5, top - 0.5, thumb + 1, thumb + 1);

    const small = Math.max(18, Math.min(44, (panel - 2 * 6) / 3));
    const columns = Math.max(1, Math.floor((panel + 6) / (small + 6)));
    const gridTop = top + thumb + 28;
    const rows = Math.max(0, Math.floor((top + side - gridTop + 6) / (small + 6)));
    context.fillStyle = MUTED;
    if (rows) context.fillText('GENERACIÓN ACTUAL', panelLeft, gridTop - 8);
    const sample = [...snapshot.population].sort((a, b) => b.fitness - a.fitness);
    for (let i = 0; i < Math.min(columns * rows, sample.length); i++) {
      const individual = sample[Math.round(i * (sample.length - 1) / Math.max(1, columns * rows - 1))];
      const x = panelLeft + (i % columns) * (small + 6), y = gridTop + Math.floor(i / columns) * (small + 6);
      sprite(context, individual.genes, x, y, small);
    }
  };
}
