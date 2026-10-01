package org.example.GUI;

import org.example.GA.Agents.Population;
import org.example.GA.GeneticAlgorithm;
import org.example.GA.Agents.Abilities.Selection.TournamentSelection;
import org.jfree.data.xy.XYSeries;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import javax.swing.*;
import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named = "java.awt.headless", matches = "false")
class GeneticAlgorithmGUITest {
    private GeneticAlgorithmGUI gui;

    @BeforeEach
    void createWindow() throws Exception {
        gui = onEdt(GeneticAlgorithmGUI::new);
    }

    @AfterEach
    void disposeWindow() throws Exception {
        onEdt(() -> { gui.dispose(); return null; });
    }

    @Test
    void consecutiveRunsKeepGenerationNumbersAndFractionalFitnessAndBlockParallelRuns() throws Exception {
        onEdt(() -> {
            gui.problemComboBox.setSelectedIndex(4);
            gui.initialPopulationField.setText("1");
            gui.mutationRateField.setText("0");
            gui.runOneGenerationButton.doClick(0);
            assertFalse(gui.runOneGenerationButton.isEnabled());
            assertFalse(gui.restartAlgorithmButton.isEnabled());
            gui.runOneGenerationButton.doClick(0);
            return null;
        });
        awaitWorker();
        onEdt(() -> {
            XYSeries series = field(gui, "series", XYSeries.class);
            assertEquals(1, series.getItemCount());
            double fitness = field(gui, "population", Population.class).getFittestIndividual().getFitness();
            assertEquals(fitness, series.getY(0).doubleValue());
            assertNotEquals(Math.rint(fitness), fitness);
            gui.runOneGenerationButton.doClick(0);
            return null;
        });
        awaitWorker();
        onEdt(() -> {
            XYSeries series = field(gui, "series", XYSeries.class);
            assertEquals(2, series.getItemCount());
            assertEquals(1, series.getX(0).intValue());
            assertEquals(2, series.getX(1).intValue());
            return null;
        });
    }

    @Test
    void restartOnlyResetsOnceAndUsesUpdatedTournamentConfiguration() throws Exception {
        onEdt(() -> {
            gui.problemComboBox.setSelectedIndex(2);
            gui.initialPopulationField.setText("3");
            gui.tournamentSizeField.setText("9");
            gui.restartAlgorithmButton.doClick(0);
            assertEquals(0, field(gui, "generations", Integer.class));
            assertEquals(0, field(gui, "series", XYSeries.class).getItemCount());
            assertEquals(3, gui.getContentPane().getComponentCount());
            GeneticAlgorithm algorithm = field(gui, "ga", GeneticAlgorithm.class);
            assertEquals(9, field((TournamentSelection) algorithm.selectionMethod, "tournamentSize", Integer.class));
            gui.runOneGenerationButton.doClick(0);
            return null;
        });
        awaitWorker();
        onEdt(() -> {
            gui.restartAlgorithmButton.doClick(0);
            assertEquals(0, field(gui, "generations", Integer.class));
            assertEquals(0, field(gui, "series", XYSeries.class).getItemCount());
            assertEquals(3, field(gui, "population", Population.class).size());
            assertEquals(3, gui.getContentPane().getComponentCount());
            assertTrue(gui.runOneGenerationButton.isEnabled());
            return null;
        });
    }

    private void awaitWorker() throws Exception {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
        while (onEdt(() -> field(gui, "worker", SwingWorker.class)) != null) {
            if (System.nanoTime() > deadline) fail("SwingWorker did not complete");
            Thread.sleep(20);
        }
        assertFalse(onEdt(() -> gui.statusLabel.getText().startsWith("Error:")));
    }

    private static <T> T field(Object object, String name, Class<T> type) throws ReflectiveOperationException {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(object));
    }

    private static <T> T onEdt(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        SwingUtilities.invokeAndWait(task);
        return task.get();
    }
}
