# Demo web de Genetic Algorithm

Aplicación en castellano para explorar los seis problemas del proyecto desde el navegador. La aplicación Java y sus pruebas siguen en sus carpetas originales.

## Desarrollo local

Necesitas Node.js 22.12 o superior (se recomienda Node.js 24).

```sh
cd web
npm ci
npm run dev
```

La terminal muestra la dirección local. Para generar y revisar la versión de producción:

```sh
npm run build
npm run preview
```

## Pruebas

```sh
npm test
npx playwright install chromium webkit
npm run build
npm run test:e2e
```

Las pruebas de navegador utilizan la versión compilada, verifican los Web Workers, las visualizaciones, los controles, las descargas y los enlaces compartidos en Chromium de escritorio y WebKit con viewport de iPhone. Las capturas se guardan en `artifacts/` y se excluyen de Git.

## Conectar a Vercel más adelante

Importa `aaronMulveyAI/GeneticAlgorithm` desde tu cuenta de Vercel y configura:

| Ajuste | Valor |
| --- | --- |
| Root Directory | `web` |
| Framework Preset | `Vite` |
| Install Command | `npm ci` |
| Build Command | `npm run build` |
| Output Directory | `dist` |
| Node.js | `24.x` |

No se necesitan variables de entorno, backend, base de datos ni servicios externos. `vercel.json` configura la compilación y cabeceras básicas. No se ha enlazado ni desplegado ningún proyecto de Vercel.

El botón Compartir genera un enlace con los parámetros y la semilla. Al abrirlo se reproduce la población inicial; no reanuda una ejecución en curso. Descargar resultado exporta la configuración, el problema generado, el mejor cromosoma y el historial a JSON.

## Correspondencia con Java

`src/engine.ts` traslada los operadores y las reglas de evaluación del código Java revisado:

- Selección por torneo, ruleta, truncamiento y residual (Brindle).
- Cruce de un punto, dos puntos y uniforme, con conservación de permutaciones.
- Mutación por gen dentro de su dominio o intercambio de posiciones en permutaciones.
- Dirección de optimización independiente de la representación.
- Sustitución generacional; el elitismo es opcional y está desactivado inicialmente.

Las reinas, la mochila, la secuencia y la función usan las mismas reglas de evaluación. El viajante web usa ciudades en un plano y distancias euclídeas para que el dibujo y el fitness correspondan; el viajante Java original permite matrices de distancias arbitrarias. La versión circular web usa radio 40.

Cada simulación tiene un generador pseudoaleatorio `seedrandom` independiente. La misma semilla y configuración producen el mismo resultado en la versión web, aunque la secuencia aleatoria no coincide con `java.util.Random`.

## Límites de la demo

El cálculo se ejecuta en un Web Worker del navegador del visitante. Los controles limitan la población a 500 individuos y la ejecución a 5.000 generaciones. El historial conserva la generación inicial y las últimas 1.000; el mejor resultado histórico y el cromosoma se conservan durante toda la ejecución.

El fitness histórico no garantiza un óptimo global. La velocidad depende del dispositivo del visitante. Las configuraciones compartidas solo contienen parámetros públicos; los resultados no se guardan en un servidor.
