# Demo web de Genetic Algorithm

Aplicación en castellano para explorar desde el navegador los seis problemas del proyecto y dos simulaciones animadas: cohetes inteligentes y una criatura que aprende a andar. La aplicación Java y sus pruebas siguen en sus carpetas originales.

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

Cada simulación tiene un generador pseudoaleatorio independiente (algoritmo Alea de `seedrandom`). Con el ARC4 por defecto, las semillas cuyos dígitos se repiten (1, 11, 111…) producían la misma ejecución. La misma semilla y configuración producen el mismo resultado en la versión web, aunque la secuencia aleatoria no coincide con `java.util.Random`.

## Optimización de funciones

El problema de funciones ofrece cuatro funciones de prueba clásicas de dos variables, que se minimizan, además de la función original de una variable de la versión Java, que se maximiza (`src/landscapes.ts`):

| Función | Dominio | Qué muestra |
| --- | --- | --- |
| Rastrigin | ±5,12 | Cientos de mínimos locales en cuadrícula |
| Ackley | ±5 | Paisaje casi plano con un embudo central |
| Himmelblau | ±5 | Cuatro mínimos globales igual de buenos |
| Schwefel | ±500 | El mejor valle está lejos del segundo mejor |

El cromosoma tiene 32 bits: 16 para x y 16 para y. La vista Superficie 3D dibuja el terreno en el canvas, sin librerías, con giro automático y giro manual arrastrando. El terreno se ordena por profundidad y la población se dibuja encima. La vista Mapa de calor muestra el terreno desde arriba con curvas de nivel. En ambas, cada generación nueva se desliza desde la anterior; la estrella marca el óptimo global y el punto rojo, el mejor histórico. Con movimiento reducido activado no hay giro ni transiciones.

## Cohetes inteligentes

Problema exclusivo de la versión web, sin equivalente en Java (`src/rockets.ts`). Cada cromosoma es una secuencia de impulsos, uno por paso de vuelo, en ocho direcciones. Los cohetes salen de la base con inercia y velocidad máxima limitada; se estrellan si tocan un obstáculo o salen del mapa.

- Fitness de 0 a 100 según el avance hacia la diana, medido como distancia de navegación que rodea los obstáculos (Dijkstra sobre una cuadrícula de 100 × 100). Un cohete estrellado conserva la mitad de su avance.
- Aterrizar puntúa de 100 a 200: cuanto antes llega, más puntos.
- Escenarios: muro central, zigzag y un campo de asteroides generado con la semilla que siempre deja un pasillo de al menos 10 unidades.
- La vista Mejor solución anima el vuelo de toda la población actual y resalta en verde el mejor cohete histórico. Mientras la simulación avanza, cada vuelta muestra la generación más reciente; en pausa, cada generación despega desde el principio. Con movimiento reducido activado se muestra el estado final del vuelo.

## Criatura que aprende a andar

Problema exclusivo de la versión web (`src/walker.ts`). La criatura es un conjunto de nodos unidos por huesos de longitud fija y músculos cuya longitud oscila. La física usa integración de Verlet con restricciones de distancia, gravedad, suelo y rozamiento, sin dependencias y de forma determinista.

- El cromosoma tiene 16 niveles por gen: una frecuencia común y, para cada músculo, amplitud y fase. Cuadrúpedo: 9 genes. Gusano: 13.
- El fitness son los metros que avanza el centro de la criatura durante la prueba (de 4 a 20 segundos; 10 por defecto).
- Si el tronco del cuadrúpedo o el lomo del gusano tocan el suelo, la criatura se cae y la prueba termina con la distancia recorrida hasta ese momento.
- Terrenos: llano o colinas suaves a partir de los 2 m.
- La animación reproduce el recorrido en tiempo real con una cámara que sigue al mejor histórico (en negro, con músculos en rojo). En azul aparecen hasta diez individuos distintos de la generación actual repartidos por el ranking, en rojo los que se han caído. La barra superior sitúa a todos en la carrera aunque queden fuera de cámara.
- Simular cada generación cuesta unos 40–70 ms con 96 individuos, por lo que la velocidad 16× avanza menos generaciones por segundo que en los demás problemas.

## Límites de la demo

El cálculo se ejecuta en un Web Worker del navegador del visitante. Los controles limitan la población a 500 individuos y la ejecución a 5.000 generaciones. El historial conserva la generación inicial y las últimas 1.000; el mejor resultado histórico y el cromosoma se conservan durante toda la ejecución.

El fitness histórico no garantiza un óptimo global. La velocidad depende del dispositivo del visitante. Las configuraciones compartidas solo contienen parámetros públicos; los resultados no se guardan en un servidor.
