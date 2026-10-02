import { GeneticEngine, MAX_GENERATIONS } from './engine';
import type { Status, WorkerCommand, WorkerResponse } from './types';

const scope = self as unknown as {
  onmessage: ((event: MessageEvent<WorkerCommand>) => void) | null;
  postMessage(message: WorkerResponse): void;
};
let engine: GeneticEngine | null = null;
let status: Status = 'ready';
let timer: ReturnType<typeof setTimeout> | undefined;
let speed = 1;
let remaining = 0;
let session = 0;

function stop() {
  clearTimeout(timer);
  timer = undefined;
  remaining = 0;
}

function emit() {
  if (engine) scope.postMessage({ type: 'snapshot', snapshot: engine.snapshot(status), session });
}

function tick() {
  if (!engine || status !== 'running') return;
  try {
    const count = remaining > 0 ? Math.min(remaining, 8) : speed;
    engine.evolve(count);
    if (remaining > 0) {
      remaining -= count;
      if (remaining === 0) status = 'paused';
    }
    if (engine.snapshot().generation >= MAX_GENERATIONS) status = 'completed';
    emit();
    if (status === 'running') timer = setTimeout(tick, 50);
  } catch (error) {
    stop();
    status = 'paused';
    scope.postMessage({ type: 'error', message: error instanceof Error ? error.message : 'Error de simulación.', session });
    emit();
  }
}

scope.onmessage = ({ data }) => {
  try {
    if (data.type === 'initialize') {
      stop();
      session = data.session;
      engine = new GeneticEngine(data.config);
      status = 'ready';
      emit();
    } else if (data.type === 'speed') {
      speed = [1, 4, 16].includes(data.speed) ? data.speed : 1;
    } else if (engine && data.type === 'pause') {
      stop();
      status = 'paused';
      emit();
    } else if (engine && data.type === 'start') {
      stop();
      speed = [1, 4, 16].includes(data.speed) ? data.speed : 1;
      status = 'running';
      tick();
    } else if (engine && data.type === 'step') {
      if (!Number.isInteger(data.count) || data.count < 1 || data.count > 100) throw new Error('Número de generaciones no válido.');
      stop();
      if (data.count === 1) {
        engine.evolve();
        status = 'paused';
        emit();
      } else {
        remaining = data.count;
        status = 'running';
        tick();
      }
    }
  } catch (error) {
    stop();
    status = 'paused';
    scope.postMessage({ type: 'error', message: error instanceof Error ? error.message : 'No se pudo iniciar la simulación.', session });
  }
};
