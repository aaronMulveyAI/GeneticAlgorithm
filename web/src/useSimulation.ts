import { useCallback, useEffect, useRef, useState } from 'react';
import type { Config, Snapshot, WorkerCommand, WorkerResponse } from './types';

export function useSimulation(config: Config) {
  const worker = useRef<Worker | null>(null);
  const session = useRef(0);
  const [snapshot, setSnapshot] = useState<Snapshot | null>(null);
  const [pending, setPending] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    const instance = new Worker(new URL('./simulation.worker.ts', import.meta.url), { type: 'module' });
    worker.current = instance;
    instance.onmessage = ({ data }: MessageEvent<WorkerResponse>) => {
      if (data.session !== session.current) return;
      if (data.type === 'snapshot') {
        setSnapshot(data.snapshot);
        setPending(false);
      } else {
        setError(data.message);
        setPending(false);
        setSnapshot(previous => previous ? { ...previous, status: 'paused' } : null);
      }
    };
    instance.onerror = () => {
      setError('No se pudo cargar el motor de simulación. Recarga la página para intentarlo de nuevo.');
      setPending(false);
    };
    return () => {
      instance.terminate();
      worker.current = null;
    };
  }, []);

  useEffect(() => {
    const currentSession = ++session.current;
    setPending(true);
    setError(null);
    const timer = setTimeout(() => worker.current?.postMessage({
      type: 'initialize', config, session: currentSession,
    } satisfies WorkerCommand), 120);
    return () => clearTimeout(timer);
  }, [config, revision]);

  const command = useCallback((message: Exclude<WorkerCommand, { type: 'initialize' }>) => {
    worker.current?.postMessage(message);
    if (message.type === 'start' || (message.type === 'step' && message.count > 1)) {
      setSnapshot(previous => previous ? { ...previous, status: 'running' } : null);
    }
    if (message.type === 'pause') {
      setSnapshot(previous => previous ? { ...previous, status: 'paused' } : null);
    }
  }, []);

  return { snapshot, pending, error, command, restart: () => setRevision(value => value + 1) };
}
