import { useState } from 'react';
import {
  Activity, ArrowUpRight, Check, CodeXml, Dna, Download, FastForward, Link2,
  LoaderCircle, Pause, Play, RotateCcw, Shuffle, SkipForward, SlidersHorizontal, Target, Users, X,
} from 'lucide-react';
import { Canvas } from './Canvas';
import { DEFAULT_CONFIG, PROBLEMS, readConfig, shareUrl } from './config';
import { binaryToReal } from './engine';
import { useSimulation } from './useSimulation';
import { evolutionPainter, histogramPainter, populationPainter, solutionPainter } from './visualizations';
import type { Config, Crossover, ProblemId, Selection, Snapshot } from './types';

const formatters = Array.from({ length: 5 }, (_, digits) => new Intl.NumberFormat('es-ES', {
  maximumFractionDigits: digits, minimumFractionDigits: digits,
}));
const format = (value: number, digits = 2) => formatters[digits].format(value);

function NumberField({ label, value, min, max, disabled, change }: {
  label: string; value: number; min: number; max: number; disabled: boolean; change: (value: number) => void;
}) {
  const [draft, setDraft] = useState(String(value));
  return <label className="field">
    <span>{label}</span>
    <input type="number" min={min} max={max} step={1} required value={draft} disabled={disabled}
      onChange={event => setDraft(event.target.value)}
      onKeyDown={event => { if (event.key === 'Enter') event.currentTarget.blur(); }}
      onBlur={event => {
        if (event.target.validity.valid) change(Number(draft));
        else { event.target.reportValidity(); setDraft(String(value)); }
      }} />
  </label>;
}

function sceneDetail(snapshot: Snapshot): string {
  const { problem, best } = snapshot;
  if (problem.id === 'queens') {
    const conflicts = problem.size * (problem.size - 1) / 2 - best.fitness;
    return conflicts === 0 ? 'Tablero sin conflictos' : `${conflicts} pares en conflicto`;
  }
  if (problem.permutation) return `Ruta cerrada · ${problem.size} ciudades`;
  if (problem.id === 'sequence') return `${best.fitness} de ${problem.size} dígitos correctos`;
  if (problem.id === 'function') return `x = ${format(binaryToReal(best.genes), 4)}`;
  const weight = best.genes.reduce((sum, gene, i) => sum + gene * problem.weights[i], 0);
  return `Peso ${weight} / ${problem.capacity} · ${best.genes.filter(Boolean).length} objetos`;
}

export default function App() {
  const [config, setConfig] = useState<Config>(() => readConfig(window.location.search));
  const [speed, setSpeed] = useState(4);
  const [view, setView] = useState<'solution' | 'population'>('solution');
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [notice, setNotice] = useState('');
  const { snapshot, pending, error, command, restart } = useSimulation(config);
  const running = snapshot?.status === 'running' && !pending;
  const completed = snapshot?.status === 'completed';
  const problem = PROBLEMS[config.problem];
  const disabled = pending || !snapshot || Boolean(error);
  const update = <K extends keyof Config>(key: K, value: Config[K]) =>
    setConfig(previous => ({ ...previous, [key]: value }));

  function chooseProblem(id: ProblemId) {
    setConfig(previous => ({
      ...previous, problem: id, size: PROBLEMS[id].defaultSize,
      mutationRate: id === 'sequence' ? 0.035 : id === 'function' ? 0.01 : 0.025,
    }));
    setView('solution');
  }

  function randomProblem() {
    const seed = crypto.getRandomValues(new Uint32Array(1))[0];
    update('seed', seed);
  }

  async function share() {
    const url = shareUrl(config, window.location);
    window.history.replaceState(null, '', url);
    try {
      await navigator.clipboard.writeText(url);
      setNotice('Enlace de la configuración copiado');
    } catch {
      window.prompt('Enlace de la configuración', url);
    }
  }

  function download() {
    if (!snapshot || pending) return;
    const result = {
      version: 1, config, generation: snapshot.generation,
      problem: snapshot.problem, best: snapshot.best, history: snapshot.history,
    };
    const blob = new Blob([JSON.stringify(result, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = `genetic-algorithm-${config.problem}-${config.seed}.json`;
    anchor.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
    setNotice('Resultado descargado');
  }

  function navigateTabs(event: React.KeyboardEvent<HTMLButtonElement>) {
    if (!['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) return;
    event.preventDefault();
    const next = event.key === 'Home' ? 'solution' : event.key === 'End' ? 'population'
      : view === 'solution' ? 'population' : 'solution';
    setView(next);
    document.getElementById(`${next}-tab`)?.focus();
  }

  const status = pending ? 'Preparando' : running ? 'En ejecución' : completed ? 'Límite alcanzado'
    : snapshot?.generation ? 'En pausa' : 'Listo';

  return <div className="app">
    <header className="topbar">
      <div className="brand"><div className="brand-mark"><Dna size={25} strokeWidth={1.7} /></div>
        <div><h1>Genetic Algorithm</h1><span>Laboratorio de optimización</span></div>
      </div>
      <div className="header-actions">
        <button className="quiet-button share-button" onClick={share} title="Compartir configuración" aria-label="Compartir configuración">
          <Link2 size={16} /><span>Compartir</span>
        </button>
        <a className="icon-button" href="https://github.com/aaronMulveyAI/GeneticAlgorithm" target="_blank" rel="noreferrer"
          title="Código en GitHub" aria-label="Código en GitHub"><CodeXml size={19} /></a>
      </div>
    </header>

    <div className="workspace">
      <aside id="settings-panel" className={`sidebar ${settingsOpen ? 'is-open' : ''}`} aria-label="Parámetros de simulación">
        <div className="sidebar-title"><SlidersHorizontal size={15} /><h2>Configuración</h2>
          <button className="icon-button settings-dismiss" aria-label="Ocultar parámetros" onClick={() => setSettingsOpen(false)}><X size={16} /></button>
        </div>
        <fieldset disabled={running}>
          <label className="field"><span>Problema</span>
            <select value={config.problem} onChange={event => chooseProblem(event.target.value as ProblemId)}>
              {Object.entries(PROBLEMS).map(([id, definition]) => <option key={id} value={id}>{definition.name}</option>)}
            </select>
          </label>
          <div className="paired-fields">
            <NumberField key={`size-${config.size}-${config.problem}`} label={problem.sizeLabel} value={config.size}
              min={problem.min} max={problem.max} disabled={running || config.problem === 'function'} change={value => update('size', value)} />
            <NumberField key={`population-${config.populationSize}`} label="Población" value={config.populationSize}
              min={2} max={500} disabled={running} change={value => update('populationSize', value)} />
          </div>
          <div className="sidebar-divider" />
          <label className="field"><span>Selección</span>
            <select value={config.selection} onChange={event => update('selection', event.target.value as Selection)}>
              <option value="tournament">Torneo</option><option value="roulette">Ruleta</option>
              <option value="truncation">Truncamiento</option><option value="residual">Residual (Brindle)</option>
            </select>
          </label>
          {config.selection === 'tournament' ? <NumberField key={`tournament-${config.tournamentSize}`} label="Tamaño del torneo"
            value={config.tournamentSize} min={1} max={50} disabled={running} change={value => update('tournamentSize', value)} /> : null}
          <label className="field"><span>Cruce</span>
            <select value={config.crossover} onChange={event => update('crossover', event.target.value as Crossover)}>
              <option value="single">Un punto</option><option value="double">Dos puntos</option><option value="uniform">Uniforme</option>
            </select>
          </label>
          <label className="field range-field"><span>Tasa de cruce <output>{format(config.crossoverRate * 100, 0)} %</output></span>
            <input aria-label="Tasa de cruce" type="range" min={0} max={100} step={1}
              value={config.crossoverRate * 100} onChange={event => update('crossoverRate', Number(event.target.value) / 100)} />
          </label>
          <label className="field range-field"><span>Tasa de mutación <output>{format(config.mutationRate * 100, 1)} %</output></span>
            <input aria-label="Tasa de mutación" type="range" min={0} max={100} step={0.5}
              value={config.mutationRate * 100} onChange={event => update('mutationRate', Number(event.target.value) / 100)} />
          </label>
          <label className="checkbox-field"><input type="checkbox" checked={config.elitism}
            onChange={event => update('elitism', event.target.checked)} /><span>Conservar el mejor individuo</span></label>
          <div className="sidebar-divider" />
          <NumberField key={`seed-${config.seed}`} label="Semilla" value={config.seed}
            min={0} max={4294967295} disabled={running} change={value => update('seed', value)} />
          <button className="secondary-button full-width" type="button" onClick={randomProblem}><Shuffle size={15} />Nueva instancia</button>
          <button className="text-button full-width" type="button" onClick={() => { setConfig({ ...DEFAULT_CONFIG }); restart(); }}>
            <RotateCcw size={13} />Restablecer parámetros
          </button>
        </fieldset>
        <a className="project-link" href="https://github.com/aaronMulveyAI/GeneticAlgorithm" target="_blank" rel="noreferrer">
          Proyecto original <ArrowUpRight size={14} />
        </a>
      </aside>

      <main>
        <div className="experiment-heading">
          <div><div className="eyebrow">EXPERIMENTO <span>{problem.short.toUpperCase()}</span></div>
            <h2>{config.problem === 'queens' ? `${config.size} reinas` : problem.name}</h2>
            <p>{snapshot?.problem.minimize ? 'Minimizar' : 'Maximizar'} <span>·</span> {problem.unit}</p>
          </div>
          <div className={`status ${running ? 'is-running' : ''}`} role="status">
            {pending ? <LoaderCircle size={12} className="spin" /> : <span className="status-dot" />}{status}
          </div>
        </div>

        <div className="toolbar">
          <div className="run-actions">
            <button className="primary-button" disabled={disabled || completed} onClick={() =>
              command(running ? { type: 'pause' } : { type: 'start', speed })}>
              {running ? <Pause size={16} /> : <Play size={16} />}<span>{running ? 'Pausar' : 'Iniciar'}</span>
            </button>
            <button className="icon-button" title="Avanzar una generación" aria-label="Avanzar una generación"
              disabled={disabled || running || completed} onClick={() => command({ type: 'step', count: 1 })}><SkipForward size={18} /></button>
            <button className="quiet-button batch-button" aria-label="Avanzar 100 generaciones" title="Avanzar 100 generaciones" disabled={disabled || running || completed}
              onClick={() => command({ type: 'step', count: 100 })}><FastForward size={16} /><span>100 generaciones</span></button>
            <span className="toolbar-divider" />
            <button className="icon-button" title="Reiniciar simulación" aria-label="Reiniciar simulación" disabled={disabled || running}
              onClick={restart}><RotateCcw size={17} /></button>
          </div>
          <div className="toolbar-end">
            <div className="speed-control" role="group" aria-label="Velocidad">
              {[1, 4, 16].map(value => <button key={value} aria-pressed={speed === value} aria-label={`Velocidad ${value}x`}
                className={speed === value ? 'selected' : ''} onClick={() => {
                  setSpeed(value); command({ type: 'speed', speed: value });
                }}>{value}×</button>)}
            </div>
            <button className="icon-button" title="Descargar resultado" aria-label="Descargar resultado" disabled={disabled}
              onClick={download}><Download size={17} /></button>
            <button className="icon-button mobile-settings" title="Parámetros" aria-label="Mostrar parámetros"
              aria-controls="settings-panel" aria-expanded={settingsOpen} onClick={() => setSettingsOpen(value => !value)}><SlidersHorizontal size={17} /></button>
          </div>
        </div>

        {error ? <div className="error-message" role="alert">{error}<button className="text-button" onClick={restart}>Reintentar</button></div> : null}
        {snapshot ? <>
          <section className="metrics" aria-label="Resultados">
            <div><span><Activity size={13} />Generación</span><strong data-testid="generation">{snapshot.generation.toLocaleString('es-ES')}</strong><small>de 5.000</small></div>
            <div><span><Target size={13} />Mejor histórico</span><strong data-testid="best-fitness">{format(snapshot.best.fitness, snapshot.problem.permutation || config.problem === 'function' ? 2 : 0)}</strong>
              <small className="positive">{snapshot.history[0].best === 0
                ? `+${format(Math.abs(snapshot.best.fitness), 1)} de mejora`
                : `+${format(snapshot.improvement, 1)} % de mejora`}</small></div>
            <div><span>Fitness medio</span><strong>{format(snapshot.average, 2)}</strong><small>población actual</small></div>
            <div><span><Users size={13} />Diversidad</span><strong>{format(snapshot.diversity, 0)}<em> %</em></strong><small>{snapshot.population.length} individuos</small></div>
          </section>

          <section className="visual-section">
            <div className="scene-column">
              <div className="section-heading">
                <div className="tabs" role="tablist" aria-label="Visualización">
                  <button role="tab" id="solution-tab" aria-controls="scene-panel" aria-selected={view === 'solution'} tabIndex={view === 'solution' ? 0 : -1} onKeyDown={navigateTabs}
                    onClick={() => setView('solution')}>Mejor solución</button>
                  <button role="tab" id="population-tab" aria-controls="scene-panel" aria-selected={view === 'population'} tabIndex={view === 'population' ? 0 : -1} onKeyDown={navigateTabs}
                    onClick={() => setView('population')}>Población</button>
                </div>
                <span className="generation-tag">G{snapshot.generation}</span>
              </div>
              <div id="scene-panel" role="tabpanel" aria-labelledby={view === 'solution' ? 'solution-tab' : 'population-tab'}
                className={`scene ${config.problem === 'knapsack' && view === 'solution' ? 'scene-knapsack' : ''}`}
                style={{ '--knapsack-height': `${Math.max(310, Math.ceil(config.size / 4) * 42 + 60)}px` } as React.CSSProperties}>
                <Canvas draw={view === 'solution' ? solutionPainter(snapshot) : populationPainter(snapshot)}
                  label={view === 'solution' ? `Mejor solución de ${problem.name}: ${sceneDetail(snapshot)}` : 'Distribución del fitness de la población'}
                  testId="solution-canvas" className="solution-canvas" />
                <div className="scene-caption">
                  <span><span className="legend-dot green" />{view === 'solution' ? sceneDetail(snapshot) : `${snapshot.population.length} individuos`}</span>
                  {config.problem === 'sequence' && view === 'solution' ? <span>Objetivo / candidato</span> :
                    view === 'population' ? <span className="heat-legend">Menor <i /> Mayor aptitud</span> : <span>Mejor histórico</span>}
                </div>
              </div>
            </div>
            <aside className="results-column" aria-label="Detalle del resultado">
              <h3>Registro de mejoras</h3>
              <ol className="discoveries">{snapshot.discoveries.map((discovery, i) => <li key={discovery.generation}>
                <span className={`discovery-node ${i === 0 ? 'latest' : ''}`} />
                <div><strong>{format(discovery.fitness, snapshot.problem.permutation || config.problem === 'function' ? 2 : 0)}</strong>
                  <span>{discovery.generation === 0 ? 'Población inicial' : `Generación ${discovery.generation}`}</span></div>
                {i === 0 ? <span className="best-label">MEJOR</span> : null}
              </li>)}</ol>
              <div className="chromosome"><h3>Cromosoma</h3>
                <div className="gene-values" data-testid="chromosome">{snapshot.best.genes.map((gene, i) =>
                  <span key={i}>{snapshot.problem.permutation ? gene + 1 : gene}</span>)}</div>
                <span className="gene-count">{snapshot.best.genes.length} genes</span>
              </div>
            </aside>
          </section>

          <div className="charts-row">
            <section className="chart-section">
              <div className="section-heading"><h3>Evolución del fitness</h3>
                <div className="chart-legend"><span><i className="legend-dot green" />Mejor</span><span><i className="legend-dot blue" />Media</span></div>
              </div>
              <Canvas draw={evolutionPainter(snapshot)} label="Evolución del mejor fitness y fitness medio por generación"
                testId="evolution-canvas" className="chart-canvas" />
            </section>
            <section className="chart-section distribution">
              <div className="section-heading"><h3>Distribución actual</h3><span>{snapshot.population.length} individuos</span></div>
              <Canvas draw={histogramPainter(snapshot)} label="Histograma del fitness de la población actual"
                testId="histogram-canvas" className="chart-canvas" />
            </section>
          </div>
        </> : <div className="loading-scene"><LoaderCircle className="spin" size={25} /><span>Preparando experimento</span></div>}
        <footer><span>Genetic Algorithm <span className="footer-separator">/</span> Laboratorio interactivo</span><span>Semilla {config.seed}</span></footer>
      </main>
    </div>
    {notice ? <div className="toast" role="status"><Check size={16} />{notice}
      <button className="icon-button" aria-label="Cerrar aviso" onClick={() => setNotice('')}><X size={14} /></button>
    </div> : null}
  </div>;
}
