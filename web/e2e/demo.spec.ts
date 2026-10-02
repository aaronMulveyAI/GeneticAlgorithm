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

test('los siete problemas dibujan soluciones reales y caben en pantalla', async ({ page }, testInfo) => {
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Mostrar parámetros' }).click();
  for (const problem of ['queens', 'tsp', 'circular', 'knapsack', 'sequence', 'function', 'rockets']) {
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

test('población, descarga y configuración reproducible', async ({ page, browserName }, testInfo) => {
  if (testInfo.project.name === 'mobile') await page.getByRole('button', { name: 'Mostrar parámetros' }).click();
  await page.getByRole('combobox', { name: 'Problema', exact: true }).selectOption('sequence');
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
