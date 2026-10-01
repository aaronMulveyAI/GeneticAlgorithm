package org.example.GUI;

import org.example.GA.Agents.Abilities.Crossover.*;
import org.example.GA.Agents.Abilities.Selection.*;
import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.Agents.Abilities.iReproduction;
import org.example.GA.Agents.Population;
import org.example.GA.GeneticAlgorithm;
import org.example.OptimizationProblems.Modelling.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GeneticAlgorithmGUI extends JFrame {
    private GeneticAlgorithm ga;
    private Population population;
    private PopulationPanel populationPanel;
    private XYSeries series;
    private ChartPanel histogramPanel;
    private JPanel displayPanel;
    private SwingWorker<Population, GenerationUpdate> worker;
    private int generations;

    public JComboBox<String> problemComboBox;
    public JComboBox<String> crossoverTypeComboBox;
    public JComboBox<String> selectionTypeComboBox;
    public JTextField crossoverRateField;
    public JTextField mutationRateField;
    public JTextField tournamentSizeField;
    public JTextField initialPopulationField;
    public JButton runOneGenerationButton = new JButton("Run 1 Generation");
    public JButton runNGenerationsButton = new JButton("Run N Generations");
    public JButton runForTimeButton = new JButton("Random Problem");
    public JButton restartAlgorithmButton = new JButton("Restart Algorithm");
    public JLabel statusLabel = new JLabel("Status: Ready");
    private final JPanel sidePanel = new JPanel(new GridLayout(0, 1));
    private final AbstractProblem[] problems = {
            new TravelingSalesmanProblem(), new KnapsackProblem(), new NQueensProblem(),
            new GuessNumberProblem(), new RealValueOptimizationProblem(), new CircularTSProblem()
    };
    private final iReproduction[] reproductionMethods = {
            new SinglePointCrossover(), new DoublePointCrossover(), new UniformCrossover()
    };

    private record GenerationUpdate(int generation, double fitness, Population population) {}

    public GeneticAlgorithmGUI() {
        setTitle("Genetic Algorithm Simulator");
        setSize(1500, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        sidePanel();
        add(sidePanel, BorderLayout.WEST);
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
        plots();
    }

    public void plots() {
        if (displayPanel != null) remove(displayPanel);
        displayPanel = new JPanel(new GridLayout(2, 2));
        displayPanel.setBackground(Color.WHITE);
        series = new XYSeries("Best Fitness");
        JFreeChart chart = ChartFactory.createXYLineChart("Fitness Evolution", "Generation", "Fitness",
                new XYSeriesCollection(series), PlotOrientation.VERTICAL, true, true, false);
        displayPanel.add(new ChartPanel(chart));
        populationPanel = new PopulationPanel();
        populationPanel.setBackground(Color.WHITE);
        displayPanel.add(populationPanel);
        histogramPanel = new ChartPanel(createHistogram(new HistogramDataset()));
        displayPanel.add(histogramPanel);
        JPanel visualization = problems[problemComboBox.getSelectedIndex()].getVisualization();
        visualization.setBackground(Color.WHITE);
        displayPanel.add(visualization);
        add(displayPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void sidePanel() {
        sidePanel.add(runOneGenerationButton);
        sidePanel.add(runNGenerationsButton);
        sidePanel.add(runForTimeButton);
        sidePanel.add(restartAlgorithmButton);
        problemComboBox = new JComboBox<>(new String[]{
                "Traveling Salesman Problem", "Knapsack Problem", "N-Queens Problem",
                "Guess Number Problem", "Real Value Optimization Problem", "Circular TSP Problem"
        });
        crossoverTypeComboBox = new JComboBox<>(new String[]{"Single Point", "Double Point", "Uniform"});
        selectionTypeComboBox = new JComboBox<>(new String[]{"Tournament", "Roulette", "Truncation", "Brindle Sampling"});
        sidePanel.add(problemComboBox);
        sidePanel.add(crossoverTypeComboBox);
        sidePanel.add(selectionTypeComboBox);
        JPanel parameters = new JPanel(new GridLayout(0, 2, 10, 0));
        crossoverRateField = new JTextField("0.5", 5);
        mutationRateField = new JTextField("0.15", 5);
        tournamentSizeField = new JTextField("5", 5);
        initialPopulationField = new JTextField("100", 5);
        parameters.add(new JLabel("Crossover Rate:"));
        parameters.add(crossoverRateField);
        parameters.add(new JLabel("Mutation Rate:"));
        parameters.add(mutationRateField);
        parameters.add(new JLabel("Tournament Size:"));
        parameters.add(tournamentSizeField);
        parameters.add(new JLabel("Initial Population:"));
        parameters.add(initialPopulationField);
        sidePanel.add(parameters);

        runOneGenerationButton.addActionListener(e -> runOneGeneration(1));
        runNGenerationsButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter number of generations to run");
            if (input == null) return;
            try {
                runOneGeneration(Integer.parseInt(input.trim()));
            } catch (IllegalArgumentException ex) {
                showError("Enter a positive number of generations");
            }
        });
        restartAlgorithmButton.addActionListener(e -> restartAlgorithm());
        problemComboBox.addActionListener(e -> resetDisplay());
        runForTimeButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter problem size");
            if (input == null) return;
            try {
                int size = Integer.parseInt(input.trim());
                if (size < 1) throw new IllegalArgumentException("Problem size must be positive");
                int index = problemComboBox.getSelectedIndex();
                problems[index] = problems[index].generateRandom(size);
                restartAlgorithm();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });
    }

    private void resetDisplay() {
        if (worker != null) return;
        if (ga != null) ga.problem.getVisualization().clear();
        ga = null;
        population = null;
        generations = 0;
        problems[problemComboBox.getSelectedIndex()].getVisualization().clear();
        plots();
        statusLabel.setText("Status: Ready");
    }

    private void restartAlgorithm() {
        if (worker != null) return;
        resetDisplay();
        try {
            initGA();
            displayPopulation();
            statusLabel.setText("Algorithm restarted");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void runOneGeneration(int count) {
        if (worker != null) return;
        if (count < 1) {
            showError("Number of generations must be positive");
            return;
        }
        try {
            initGA();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }
        final GeneticAlgorithm algorithm = ga;
        final Population initial = population;
        final int firstGeneration = generations;
        setControlsEnabled(sidePanel, false);
        statusLabel.setText("Running...");
        worker = new SwingWorker<>() {
            @Override
            protected Population doInBackground() throws InterruptedException {
                Population current = initial;
                for (int i = 1; i <= count; i++) {
                    current = algorithm.evolve(current);
                    publish(new GenerationUpdate(firstGeneration + i,
                            current.getFittestIndividual().getFitness(), current));
                    Thread.sleep(100);
                }
                return current;
            }

            @Override
            protected void process(List<GenerationUpdate> updates) {
                for (GenerationUpdate update : updates) {
                    series.addOrUpdate(update.generation(), update.fitness());
                }
                GenerationUpdate latest = updates.get(updates.size() - 1);
                generations = latest.generation();
                population = latest.population();
                displayPopulation();
                statusLabel.setText("Generation " + generations + " completed. Best fitness: " + latest.fitness());
            }

            @Override
            protected void done() {
                try {
                    population = get();
                    generations = firstGeneration + count;
                    displayPopulation();
                    statusLabel.setText("All generations completed. Best fitness: "
                            + population.getFittestIndividual().getFitness());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Execution interrupted");
                } catch (ExecutionException ex) {
                    showError("Execution failed: " + ex.getCause().getMessage());
                } finally {
                    worker = null;
                    setControlsEnabled(sidePanel, true);
                }
            }
        };
        worker.execute();
    }

    private void displayPopulation() {
        populationPanel.setPopulation(population);
        population.getProblem().getVisualization().setPopulation(population);
        updateHistogram();
    }

    private static void setControlsEnabled(Container container, boolean enabled) {
        for (Component component : container.getComponents()) {
            component.setEnabled(enabled);
            if (component instanceof Container child) setControlsEnabled(child, enabled);
        }
    }

    private static JFreeChart createHistogram(HistogramDataset dataset) {
        return ChartFactory.createHistogram("Fitness Distribution", "Fitness", "Frequency",
                dataset, PlotOrientation.VERTICAL, true, true, false);
    }

    private void updateHistogram() {
        if (population == null) return;
        double[] values = new double[population.size()];
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < values.length; i++) {
            values[i] = population.getIndividual(i).getFitness();
            minimum = Math.min(minimum, values[i]);
            maximum = Math.max(maximum, values[i]);
        }
        if (minimum == maximum) {
            double margin = Math.max(0.5, Math.abs(minimum) * 0.01);
            minimum -= margin;
            maximum += margin;
        }
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Fitness", values, 10, minimum, maximum);
        histogramPanel.setChart(createHistogram(dataset));
    }

    private void initGA() {
        AbstractProblem problem = problems[problemComboBox.getSelectedIndex()];
        double crossoverRate = Double.parseDouble(crossoverRateField.getText().trim());
        double mutationRate = Double.parseDouble(mutationRateField.getText().trim());
        int tournamentSize = Integer.parseInt(tournamentSizeField.getText().trim());
        int populationSize = Integer.parseInt(initialPopulationField.getText().trim());
        if (populationSize < 1 || tournamentSize < 1) {
            throw new IllegalArgumentException("Population and tournament sizes must be positive");
        }
        iSelection selection = switch (selectionTypeComboBox.getSelectedIndex()) {
            case 0 -> new TournamentSelection(tournamentSize);
            case 1 -> new RouletteSelection();
            case 2 -> new TruncationSelection(0.5);
            default -> new BrindleSelection();
        };
        GeneticAlgorithm algorithm = new GeneticAlgorithm(problem, selection,
                reproductionMethods[crossoverTypeComboBox.getSelectedIndex()], crossoverRate, mutationRate);
        if (population == null || population.getProblem() != problem || population.size() != populationSize) {
            population = new Population(problem, populationSize);
            generations = 0;
            series.clear();
        }
        ga = algorithm;
    }

    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GeneticAlgorithmGUI().setVisible(true));
    }
}
