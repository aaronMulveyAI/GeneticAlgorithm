import type { SequenceMode, Sprite } from './types';

// Modos del problema de la secuencia: frase (el programa de la comadreja de Dawkins), pixel art
// y los dígitos originales de la versión Java. Los tres puntúan cuántas posiciones coinciden.
export const ALPHABET = ' ABCDEFGHIJKLMNÑOPQRSTUVWXYZ';
export const MAX_PHRASE = 40;
export const DEFAULT_PHRASE = 'LA EVOLUCION NO TIENE PRISA';
export const SPRITE_SIZE = 16;

export const SEQUENCE_MODES: Record<SequenceMode, string> = {
  phrase: 'Frase',
  pixels: 'Pixel art',
  digits: 'Dígitos (versión Java)',
};

// Mayúsculas sin tildes (la Ñ se conserva); se descarta lo que no está en el alfabeto.
export function normalizePhrase(text: string): string {
  return text.toUpperCase().replace(/Ñ/g, '\u0000').normalize('NFD').replace(/[̀-ͯ]/g, '')
    .replace(/\u0000/g, 'Ñ').split('').filter(char => ALPHABET.includes(char)).join('')
    .replace(/\s+/g, ' ').trim().slice(0, MAX_PHRASE);
}

export const isValidPhrase = (phrase: unknown) =>
  typeof phrase === 'string' && phrase.length > 0 && normalizePhrase(phrase) === phrase;

export const PALETTE = ['#eef1f4', '#23272f', '#d35a62', '#ffffff', '#168153', '#f2c14e', '#3973d6', '#e9b48a'];
const CODES = '.krwgybo';

// Cada carácter es un color de la paleta: . fondo, k tinta, r rojo, w blanco, g verde, y amarillo, b azul, o piel.
const ART: Record<Sprite, string[]> = {
  heart: [
    '................',
    '...kkk....kkk...',
    '..krrrk..krrrk..',
    '.krwwrrkkrrrrrk.',
    '.krwrrrrrrrrrrk.',
    '.krrrrrrrrrrrrk.',
    '.krrrrrrrrrrrrk.',
    '..krrrrrrrrrrk..',
    '...krrrrrrrrk...',
    '....krrrrrrk....',
    '.....krrrrk.....',
    '......krrk......',
    '.......kk.......',
    '................',
    '................',
    '................',
  ],
  invader: [
    '................',
    '................',
    '................',
    '....g......g....',
    '.....g....g.....',
    '....gggggggg....',
    '...gg.gggg.gg...',
    '..gggggggggggg..',
    '..g.gggggggg.g..',
    '..g.g......g.g..',
    '.....gg..gg.....',
    '................',
    '................',
    '................',
    '................',
    '................',
  ],
  mushroom: [
    '.....kkkkkk.....',
    '...kkrrwwrrkk...',
    '..krrrwwwwrrrk..',
    '.kwrrrwwwwrrrwk.',
    '.kwwrrrwwrrrwwk.',
    'kwwwrrrrrrrrwwwk',
    'kwwrrrrrrrrrrwwk',
    'krrrwwwrrwwwrrrk',
    'krrwwwwwwwwwwrrk',
    '.kkkkkkkkkkkkkk.',
    '....kookkook....',
    '....kooooook....',
    '....kooooook....',
    '....kkkkkkkk....',
    '................',
    '................',
  ],
};

export const SPRITES: Record<Sprite, string> = { heart: 'Corazón', invader: 'Marciano', mushroom: 'Seta' };

export function spriteGenes(sprite: Sprite): number[] {
  return ART[sprite].join('').split('').map(code => CODES.indexOf(code));
}

export const phraseGenes = (phrase: string) => phrase.split('').map(char => ALPHABET.indexOf(char));
