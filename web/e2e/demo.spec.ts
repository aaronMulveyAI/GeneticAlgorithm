import { expect, test } from '@playwright/test';

test.beforeEach(async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('button', { name: 'Iniciar', exact: true })).toBeEnabled();
});

test('avanza, ejecuta, pausa y reinicia desde el navegador', async ({ page }, testInfo) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  await expect(page.getByTestId('generation')).toHaveText('0');
  await page.getByRole('button', { name: 'Avanzar una generación', exact: true }).click();
  await expect(page.getByTestId('generation')).toHaveText('1');
  await page.getByRole('button', { name: 'Iniciar', exact: true }).click();
  await expect(page.getByRole('button', { name: 'Pausar', exact: true })).toBeVisible();
  await expect.poll(async () => Number(await page.getByTestId('generation').textContent())).toBeGreaterThan(1);
  await page.getByRole('button', { name: 'Pausar', exact: true }).click();
  await expect(page.getByRole('button', { name: 'Iniciar', exact: true })).toBeVisible();
  await page.getByRole('button', { name: 'Reiniciar simulación', exact: true }).click();
  await expect(page.getByTestId('generation')).toHaveText('0');
  await page.getByRole('button', { name: 'Avanzar 100 generaciones', exact: true }).click();
  await expect(page.getByTestId('generation')).toHaveText('100', { timeout: 10000 });
  await expect(page.getByRole('button', { name: 'Iniciar', exact: true })).toBeEnabled();
  await expect(page.getByRole('alert')).toHaveCount(0);
  expect(errors).toEqual([]);
  await page.screenshot({ path: `artifacts/${testInfo.project.name}-queens.png`, fullPage: true });
});

test('los ocho problemas dibujan soluciones reales y caben en pantalla', async ({ page }, testInfo) => {
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Mostrar parámetros' }).click();
  for (const problem of ['queens', 'tsp', 'circular', 'knapsack', 'sequence', 'function', 'rockets', 'walker']) {
    await page.getByRole('combobox', { name: 'Problema', exact: true }).selectOption(problem);
    await expect(page.getByRole('button', { name: 'Iniciar', exact: true })).toBeEnabled();
    await page.getByRole('button', { name: 'Avanzar una generación', exact: true }).click();
    await expect(page.getByTestId('generation')).toHaveText('1');
    const pixels = await page.getByTestId('solution-canvas').evaluate((element: HTMLCanvasElement) => {
      const data = element.getContext('2d')!.getImageData(0, 0, element.width, element.height).data;
      let colored = 0;
      for (let i = 0; i < data.length; i += 4) {
        if (Math.max(data[i], data[i + 1], data[i + 2]) - Math.min(data[i], data[i + 1], data[i + 2]) > 20) colored++;
      }
      return colored;
    });
    expect(pixels).toBeGreaterThan(100);
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
    await page.screenshot({ path: `artifacts/${testInfo.project.name}-${problem}.png`, fullPage: true });
  }
});

test('los cohetes vuelan animados y cambian de escenario', async ({ page }, testInfo) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Mostrar parámetros' }).click();
  await page.getByRole('combobox', { name: 'Problema', exact: true }).selectOption('rockets');
  await expect(page.getByRole('button', { name: 'Iniciar', exact: true })).toBeEnabled();
  await expect(page.getByRole('combobox', { name: 'Escenario', exact: true })).toHaveValue('wall');
  await expect(page.getByTestId('chromosome').locator('span')).toHaveCount(140);
  await expect(page.getByTestId('chromosome').locator('span').first()).toHaveText(/[↑↗→↘↓↙←↖]/);
  const frame = () => page.getByTestId('solution-canvas').evaluate((element: HTMLCanvasElement) => element.toDataURL());
  const first = await frame();
  await expect.poll(frame).not.toBe(first);
  await page.getByRole('combobox', { name: 'Escenario', exact: true }).selectOption('asteroids');
  await expect(page.getByRole('button', { name: 'Iniciar', exact: true })).toBeEnabled();
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Ocultar parámetros' }).click();
  await page.getByRole('button', { name: 'Avanzar 100 generaciones', exact: true }).click();
  await expect(page.getByTestId('generation')).toHaveText('100', { timeout: 15000 });
  await expect(page.getByTestId('solution-canvas')).toHaveAttribute('aria-label', /cohetes llegan a la diana/);
  await page.waitForTimeout(4500);
  expect(errors).toEqual([]);
  await page.screenshot({ path: `artifacts/${testInfo.project.name}-rockets-flight.png`, fullPage: true });
});

test('la criatura anda animada y cambia de cuerpo y terreno', async ({ page }, testInfo) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Mostrar parámetros' }).click();
  await page.getByRole('combobox', { name: 'Problema', exact: true }).selectOption('walker');
  await expect(page.getByRole('button', { name: 'Iniciar', exact: true })).toBeEnabled();
  await expect(page.getByRole('spinbutton', { name: 'Duración (s)', exact: true })).toHaveValue('10');
  await expect(page.getByTestId('chromosome').locator('span')).toHaveCount(9);
  await page.getByRole('combobox', { name: 'Criatura', exact: true }).selectOption('worm');
  await page.getByRole('combobox', { name: 'Terreno', exact: true }).selectOption('hills');
  await expect(page.getByRole('button', { name: 'Iniciar', exact: true })).toBeEnabled();
  await expect(page.getByTestId('chromosome').locator('span')).toHaveCount(13);
  const frame = () => page.getByTestId('solution-canvas').evaluate((element: HTMLCanvasElement) => element.toDataURL());
  const first = await frame();
  await expect.poll(frame).not.toBe(first);
  await page.getByRole('combobox', { name: 'Criatura', exact: true }).selectOption('quadruped');
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Ocultar parámetros' }).click();
  await page.getByRole('button', { name: 'Avanzar 100 generaciones', exact: true }).click();
  await expect(page.getByTestId('generation')).toHaveText('100', { timeout: 30000 });
  await expect(page.getByTestId('solution-canvas')).toHaveAttribute('aria-label', /El mejor recorre/);
  await page.waitForTimeout(6000);
  expect(errors).toEqual([]);
  await page.screenshot({ path: `artifacts/${testInfo.project.name}-walker-run.png`, fullPage: true });
});

test('la función 2D se ve en 3D, se gira y tiene mapa de calor', async ({ page }, testInfo) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Mostrar parámetros' }).click();
  await page.getByRole('combobox', { name: 'Problema', exact: true }).selectOption('function');
  await expect(page.getByRole('combobox', { name: 'Función', exact: true })).toHaveValue('rastrigin');
  await page.getByRole('combobox', { name: 'Función', exact: true }).selectOption('himmelblau');
  await expect(page.getByRole('button', { name: 'Iniciar', exact: true })).toBeEnabled();
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Ocultar parámetros' }).click();
  await expect(page.getByRole('tab', { name: 'Superficie 3D', exact: true })).toHaveAttribute('aria-selected', 'true');
  const canvas = page.getByTestId('solution-canvas');
  const frame = () => canvas.evaluate((element: HTMLCanvasElement) => element.toDataURL());
  const first = await frame();
  await expect.poll(frame).not.toBe(first);
  const box = (await canvas.boundingBox())!;
  await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
  await page.mouse.down();
  await page.mouse.move(box.x + box.width / 2 + 120, box.y + box.height / 2, { steps: 6 });
  await page.mouse.up();
  await page.getByRole('button', { name: 'Avanzar 100 generaciones', exact: true }).click();
  await expect(page.getByTestId('generation')).toHaveText('100', { timeout: 10000 });
  await expect(canvas).toHaveAttribute('aria-label', /Mejor solución de Optimización de funciones: f = /);
  await page.waitForTimeout(500);
  await page.screenshot({ path: `artifacts/${testInfo.project.name}-surface.png`, fullPage: true });
  await page.getByRole('tab', { name: 'Superficie 3D', exact: true }).press('ArrowRight');
  await expect(page.getByRole('tab', { name: 'Mapa de calor', exact: true })).toBeFocused();
  await expect(page.getByRole('tab', { name: 'Mapa de calor', exact: true })).toHaveAttribute('aria-selected', 'true');
  await expect(canvas).toHaveAttribute('aria-label', /Mapa de calor/);
  await page.waitForTimeout(500);
  expect(errors).toEqual([]);
  await page.screenshot({ path: `artifacts/${testInfo.project.name}-heatmap.png`, fullPage: true });
});

test('la secuencia se ve como panel de letras y como pixel art', async ({ page }, testInfo) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Mostrar parámetros' }).click();
  await page.getByRole('combobox', { name: 'Problema', exact: true }).selectOption('sequence');
  await expect(page.getByRole('combobox', { name: 'Modo', exact: true })).toHaveValue('phrase');
  await page.getByLabel('Frase objetivo').fill('¡Hola, pingüino del Ñandú!');
  await page.getByLabel('Frase objetivo').press('Enter');
  await expect(page.getByLabel('Frase objetivo')).toHaveValue('HOLA PINGUINO DEL ÑANDU');
  await expect(page.getByRole('button', { name: 'Iniciar', exact: true })).toBeEnabled();
  await expect(page.getByTestId('chromosome').locator('span')).toHaveCount(23);
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Ocultar parámetros' }).click();
  await page.getByRole('button', { name: 'Avanzar 100 generaciones', exact: true }).click();
  await expect(page.getByTestId('generation')).toHaveText('100', { timeout: 10000 });
  await expect(page.getByTestId('solution-canvas')).toHaveAttribute('aria-label', /de 23 letras correctas/);
  await page.getByRole('button', { name: 'Avanzar una generación', exact: true }).click();
  await page.waitForTimeout(150);
  await page.screenshot({ path: `artifacts/${testInfo.project.name}-phrase.png`, fullPage: true });
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Mostrar parámetros' }).click();
  await page.getByRole('combobox', { name: 'Modo', exact: true }).selectOption('pixels');
  await page.getByRole('combobox', { name: 'Dibujo', exact: true }).selectOption('mushroom');
  await expect(page.getByRole('button', { name: 'Iniciar', exact: true })).toBeEnabled();
  await expect(page.getByTestId('chromosome').locator('span')).toHaveCount(256);
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Ocultar parámetros' }).click();
  await page.getByRole('button', { name: 'Avanzar 100 generaciones', exact: true }).click();
  await expect(page.getByTestId('generation')).toHaveText('100', { timeout: 10000 });
  await page.getByRole('button', { name: 'Avanzar 100 generaciones', exact: true }).click();
  await expect(page.getByTestId('generation')).toHaveText('200', { timeout: 10000 });
  await expect(page.getByTestId('solution-canvas')).toHaveAttribute('aria-label', /de 256 píxeles correctos/);
  await page.waitForTimeout(400);
  expect(errors).toEqual([]);
  await page.screenshot({ path: `artifacts/${testInfo.project.name}-pixels.png`, fullPage: true });
});

test('población, descarga y configuración reproducible', async ({ page, browserName }, testInfo) => {
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Mostrar parámetros' }).click();
  await page.getByRole('combobox', { name: 'Problema', exact: true }).selectOption('sequence');
  await page.getByRole('combobox', { name: 'Modo', exact: true }).selectOption('digits');
  await page.getByRole('combobox', { name: 'Selección', exact: true }).selectOption('residual');
  await page.getByRole('combobox', { name: 'Cruce', exact: true }).selectOption('double');
  await page.getByLabel('Conservar el mejor individuo').check();
  await page.getByLabel('Semilla', { exact: true }).fill('1234');
  await page.getByLabel('Semilla', { exact: true }).blur();
  await expect(page.getByRole('button', { name: 'Iniciar', exact: true })).toBeEnabled();
  const initial = await page.getByTestId('chromosome').textContent();
  await page.getByRole('tab', { name: 'Población', exact: true }).click();
  await expect(page.getByRole('tab', { name: 'Población', exact: true })).toHaveAttribute('aria-selected', 'true');
  await page.getByRole('tab', { name: 'Población', exact: true }).press('ArrowLeft');
  await expect(page.getByRole('tab', { name: 'Mejor solución', exact: true })).toBeFocused();
  await expect(page.getByRole('tab', { name: 'Mejor solución', exact: true })).toHaveAttribute('aria-selected', 'true');
  const downloading = page.waitForEvent('download');
  await page.getByRole('button', { name: 'Descargar resultado', exact: true }).click();
  const download = await downloading;
  expect(download.suggestedFilename()).toBe('genetic-algorithm-sequence-1234.json');
  const stream = await download.createReadStream();
  let content = '';
  for await (const chunk of stream!) content += chunk.toString();
  const result = JSON.parse(content);
  expect(result.config.seed).toBe(1234);
  expect(result.config.selection).toBe('residual');
  expect(result.best.genes).toHaveLength(12);
  expect(result.history[0].generation).toBe(0);
  if (browserName === 'chromium') await page.context().grantPermissions(['clipboard-read', 'clipboard-write']);
  await page.getByRole('button', { name: 'Compartir configuración' }).click();
  await expect(page.getByRole('status').filter({ hasText: 'Enlace de la configuración copiado' })).toBeVisible();
  await page.reload();
  await expect(page.getByRole('button', { name: 'Iniciar', exact: true })).toBeEnabled();
  await expect(page.getByTestId('chromosome')).toHaveText(initial!);
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Mostrar parámetros' }).click();
  await expect(page.getByLabel('Semilla', { exact: true })).toHaveValue('1234');
  await expect(page.getByRole('combobox', { name: 'Selección', exact: true })).toHaveValue('residual');
  await expect(page.getByRole('combobox', { name: 'Cruce', exact: true })).toHaveValue('double');
});

test('pantallas estrechas y configuración máxima mantienen una visualización válida', async ({ page }, testInfo) => {
  await page.setViewportSize({ width: testInfo.project.name === 'mobile' ? 320 : 1920, height: 950 });
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Mostrar parámetros' }).click();
  await page.getByRole('combobox', { name: 'Problema', exact: true }).selectOption('knapsack');
  await page.getByRole('spinbutton', { name: 'Objetos', exact: true }).fill('48');
  await page.getByRole('spinbutton', { name: 'Objetos', exact: true }).blur();
  await expect(page.getByRole('button', { name: 'Iniciar', exact: true })).toBeEnabled();
  await expect(page.getByTestId('chromosome').locator('span')).toHaveCount(48);
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Ocultar parámetros' }).click();
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
  await page.screenshot({ path: `artifacts/${testInfo.project.name}-maximum.png`, fullPage: true });
});
