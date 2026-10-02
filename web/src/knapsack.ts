import type { KnapsackCapacity } from './types';

// Objetos de una mochila de excursión. Los pesos (kg) y las utilidades siguen saliendo de la
// semilla como en la versión Java; aquí solo se les da nombre y se calcula el óptimo exacto.
export const ITEMS = [
  'Tienda', 'Saco de dormir', 'Esterilla', 'Linterna', 'Frontal', 'Agua', 'Cantimplora', 'Hornillo',
  'Gas', 'Olla', 'Comida', 'Barritas', 'Fruta', 'Café', 'Botiquín', 'Crema solar',
  'Gafas de sol', 'Gorra', 'Chubasquero', 'Forro polar', 'Guantes', 'Calcetines', 'Camiseta', 'Mapa',
  'Brújula', 'GPS', 'Móvil', 'Batería', 'Cámara', 'Prismáticos', 'Libro', 'Navaja',
  'Cuerda', 'Bastones', 'Cerillas', 'Papel', 'Repelente', 'Silbato', 'Toalla', 'Bañador',
  'Chanclas', 'Cuaderno', 'Termo', 'Manta térmica', 'Ukelele', 'Hamaca', 'Bocadillo', 'Cepillo',
];

type Random = () => number;

// Fracción del peso total que admite la mochila. La holgada es la regla de la versión Java.
export const CAPACITIES: Record<KnapsackCapacity, { name: string; ratio: number }> = {
  loose: { name: 'Holgada · 80 % (versión Java)', ratio: 0.8 },
  medium: { name: 'Media · 50 %', ratio: 0.5 },
  tight: { name: 'Ajustada · 30 %', ratio: 0.3 },
};

export function itemNames(count: number, random: Random): string[] {
  const names = [...ITEMS];
  for (let i = names.length - 1; i > 0; i--) {
    const j = Math.floor(random() * (i + 1));
    [names[i], names[j]] = [names[j], names[i]];
  }
  return names.slice(0, count);
}

// Programación dinámica sobre la capacidad: exacta y rápida con pesos enteros pequeños.
export function knapsackOptimum(weights: number[], values: number[], capacity: number): { value: number; genes: number[] } {
  const count = weights.length;
  const width = capacity + 1;
  const table = new Int32Array((count + 1) * width);
  for (let i = 1; i <= count; i++) {
    for (let c = 0; c <= capacity; c++) {
      const skip = table[(i - 1) * width + c];
      const take = weights[i - 1] <= c ? table[(i - 1) * width + c - weights[i - 1]] + values[i - 1] : -1;
      table[i * width + c] = Math.max(skip, take);
    }
  }
  const genes = Array<number>(count).fill(0);
  for (let i = count, c = capacity; i > 0; i--) {
    if (table[i * width + c] !== table[(i - 1) * width + c]) {
      genes[i - 1] = 1;
      c -= weights[i - 1];
    }
  }
  return { value: table[count * width + capacity], genes };
}

export const packedWeight = (genes: number[], weights: number[]) =>
  genes.reduce((sum, selected, i) => sum + selected * weights[i], 0);
