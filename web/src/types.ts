export type ProblemId = 'queens' | 'tsp' | 'circular' | 'knapsack' | 'sequence' | 'function';
export type Selection = 'tournament' | 'roulette' | 'truncation' | 'residual';
export type Crossover = 'single' | 'double' | 'uniform';
export type Status = 'ready' | 'running' | 'paused' | 'completed';

export interface Config {
  problem: ProblemId;
  size: number;
  populationSize: number;
  selection: Selection;
  crossover: Crossover;
  mutationRate: number;
  crossoverRate: number;
  tournamentSize: number;
  seed: number;
  elitism: boolean;
}

export interface Point { x: number; y: number }
export interface Problem {
  id: ProblemId;
  size: number;
  permutation: boolean;
  minimize: boolean;
  domain: number;
  points: Point[];
  target: number[];
  weights: number[];
  values: number[];
  capacity: number;
}

export interface Individual { genes: number[]; fitness: number }
export interface HistoryPoint { generation: number; best: number; average: number; current: number }
export interface Discovery { generation: number; fitness: number }
export interface Snapshot {
  generation: number;
  status: Status;
  problem: Problem;
  best: Individual;
  current: Individual;
  average: number;
  diversity: number;
  improvement: number;
  population: Individual[];
  history: HistoryPoint[];
  discoveries: Discovery[];
}

export type WorkerCommand =
  | { type: 'initialize'; config: Config; session: number }
  | { type: 'start'; speed: number }
  | { type: 'pause' }
  | { type: 'step'; count: number }
  | { type: 'speed'; speed: number };
export type WorkerResponse =
  | { type: 'snapshot'; snapshot: Snapshot; session: number }
  | { type: 'error'; message: string; session: number };
