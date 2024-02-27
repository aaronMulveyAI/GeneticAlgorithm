package org.example.GUI;
import org.example.GA.Agents.Abilities.Crossover.DoublePointCrossover;
import org.example.GA.Agents.Abilities.Crossover.SinglePointCrossover;
import org.example.GA.Agents.Abilities.Crossover.UniformCrossover;
import org.example.GA.Agents.Abilities.Selection.RouletteSelection;
import org.example.GA.Agents.Abilities.Selection.TournamentSelection;
import org.example.GA.Agents.Abilities.iReproduction;
import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.Agents.Population;
import org.example.GA.Constants;
import org.example.GA.GeneticAlgorithm;
import org.example.OptimizationProblems.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;



public class GeneticAlgorithmGUI extends JFrame {


    private static GeneticAlgorithm ga;
    private static Population population;
    private PopulationPanel populationPanel;
    private XYSeries series;

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

    JPanel sidePanel = new JPanel(new GridLayout(0, 1, 0, 0)); // Modificamos para acomodar más elementos con menos espacio entre ellos

    JPanel statusPanel = new JPanel();
    public JLabel statusLabel = new JLabel("Status: Ready");
    int generatiosn = 0;

    AbstractProblem[] problems = new AbstractProblem[] {
            new TravelingSalesmanProblem(),
            new KnapsackProblem()

    };

    int indexProblems = 0;

    iReproduction[] reproductionMethods = new iReproduction[] {
        new SinglePointCrossover(),
        new DoublePointCrossover(),
        new UniformCrossover()
    };

    int indexCrossover = 0;

    iSelection[] selectionMethods = new iSelection[] {
        new RouletteSelection(),
        new TournamentSelection()
    };

    private JFreeChart histogramChart;
    private ChartPanel histogramPanel;
    private JFreeChart chart;

    private TravellingSalesmanVisualization TSPvisual;

    private KnapsackVisualization KPvisual;

    int indexSelection = 0;

    public GeneticAlgorithmGUI() {
        setTitle("Genetic Algorithm Simulator");
        setSize(1500, 1000); // Ajustamos el tamaño inicial de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); // Usamos BorderLayout como layout principal


        sidePanel();
        add(sidePanel, BorderLayout.WEST);

        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        add(statusPanel, BorderLayout.SOUTH);
        statusPanel.add(statusLabel);

        plots();

    }
    public void plots(){

        JPanel displayPanel = new JPanel(new GridLayout(2, 2)); // Organiza en 2 filas y 2 columnas
        displayPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(displayPanel, BorderLayout.CENTER);

        // Inicializa la serie de datos
        series = new XYSeries("Máximo Fitness");

        // Crea un contenedor de datos
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        // Crea el gráfico
        chart = ChartFactory.createXYLineChart(
                "Fitness Evolution", // Título del gráfico
                "Generation", // Etiqueta del eje X
                "Fitness", // Etiqueta del eje Y
                dataset, // Datos
                PlotOrientation.VERTICAL,
                true, // Incluir leyenda
                true,
                false
        );
        // Obtén el plot del gráfico
        XYPlot plot = chart.getXYPlot();

        // Ajusta el rango del eje X si es necesario
        plot.getDomainAxis().setAutoRange(true);

        // Ajusta el rango del eje Y si es necesario
        plot.getRangeAxis().setAutoRange(true);

        // Establece un margen alrededor de los rangos automáticos para evitar que los puntos más extremos toquen los bordes del gráfico
        plot.getDomainAxis().setUpperMargin(0.05); // 5% de margen en la parte superior del rango del eje X
        plot.getDomainAxis().setLowerMargin(0.05); // 5% de margen en la parte inferior del rango del eje X

        plot.getRangeAxis().setUpperMargin(0.05); // 5% de margen en la parte superior del rango del eje Y
        plot.getRangeAxis().setLowerMargin(0.05); // 5% de margen en la parte inferior del rango del eje Y


        // Crea un panel para el gráfico y lo añade al frame
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));

        // Añade el panel del gráfico al panel central dividido en 3

        displayPanel.add(chartPanel); // Añade el panel del gráfico como uno de los 3 paneles
        // Añadir otros 2 paneles aquí según sea necesario

        add(displayPanel, BorderLayout.CENTER);

        populationPanel = new PopulationPanel();
        displayPanel.add(populationPanel);

        initializeHistogram(); // Inicializa el histograma
        displayPanel.add(histogramPanel); // Añade el panel del histograma al panel central dividido en 3

        TSPvisual = new TravellingSalesmanVisualization();
        KPvisual = new KnapsackVisualization(KnapsackProblem.maxWeight);

        switch (indexProblems) {
            case 0:
                displayPanel.add(TSPvisual);
                break;
            case 1:
                displayPanel.add(KPvisual);
                break;
        }


    }
    public void sidePanel(){

        add(sidePanel, BorderLayout.WEST);


        sidePanel.add(runOneGenerationButton);

        sidePanel.add(runNGenerationsButton);

        sidePanel.add(runForTimeButton);

        sidePanel.add(restartAlgorithmButton);


        problemComboBox = new JComboBox<>(new String[]{"Traveling Salesman Problem", "Knapsack Problem"});
        sidePanel.add(problemComboBox);

        crossoverTypeComboBox = new JComboBox<>(new String[]{"Single Point", "Double Point", "Uniform"});
        // sidePanel.add(new JLabel("Crossover Type:"));
        sidePanel.add(crossoverTypeComboBox);

        selectionTypeComboBox = new JComboBox<>(new String[]{"Roulette", "Tournament"});
        // sidePanel.add(new JLabel("Selection Type:"));
        sidePanel.add(selectionTypeComboBox);

        JPanel constantsPanel = new JPanel(new GridLayout(0, 2, 0, 0));
        //sidePanel.add(new JLabel("Constants Configuration:"));
        sidePanel.add(constantsPanel);

        crossoverRateField = new JTextField("0.5", 5);
        constantsPanel.add(new JLabel("Crossover Rate:"));
        constantsPanel.add(crossoverRateField);

        mutationRateField = new JTextField("0.15", 5);
        constantsPanel.add(new JLabel("Mutation Rate:"));
        constantsPanel.add(mutationRateField);

        tournamentSizeField = new JTextField("5", 5);
        constantsPanel.add(new JLabel("Tournament Size:"));
        constantsPanel.add(tournamentSizeField);

        initialPopulationField = new JTextField("100", 5);
        constantsPanel.add(new JLabel("Initial Population:"));
        constantsPanel.add(initialPopulationField);

        // Manejadores de eventos
        runOneGenerationButton.addActionListener(e -> runOneGeneration(1));
        runNGenerationsButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter number of generations to run");
            if (input != null && !input.isEmpty()) {
                try {
                    int generations = Integer.parseInt(input);
                    runOneGeneration(generations);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        restartAlgorithmButton.addActionListener(e -> {
            generatiosn = 0;
            ga = null;
            population = null;
            statusLabel.setText("Algorithm restarted");
            series.clear();
            populationPanel.clear();
            TSPvisual.clear();
            plots();

            SwingUtilities.invokeLater(this::updateHistogram);
        });

        runForTimeButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter N ");
            if (input != null && !input.isEmpty()) {
                try {
                    int time = Integer.parseInt(input);

                    switch (indexProblems) {
                        case 0:
                            problems[0] = TravelingSalesmanProblem.generateRandom(time);
                            break;
                        case 1:
                            problems[1] = KnapsackProblem.generateRandom(time);
                            break;
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

    }
    private void runOneGeneration(int generationNumber) {

        if (ga == null) {
            initGA();
        }

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i < generationNumber; i++) {
                    population = ga.evolve(population);
                    int currentGeneration = i + 1;
                    double fitness = population.getFittestIndividual().getFitness();
                    SwingUtilities.invokeLater(() -> updateHistogram());
                    SwingUtilities.invokeLater(() -> populationPanel.setPopulation(population));
                    SwingUtilities.invokeLater(() -> TSPvisual.setPopulation(population));
                    SwingUtilities.invokeLater(() -> KPvisual.setPopulation(population));

                    // Publica los resultados intermedios para procesarlos en el EDT
                    publish(currentGeneration, (int) fitness);

                    // Simula un retraso si es necesario para ver la actualización en tiempo real
                    Thread.sleep(100); // Quitar o ajustar este retraso según sea necesario
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                // El último valor en chunks es el más reciente
                int currentGeneration = chunks.get(chunks.size() - 2);
                double fitness = chunks.get(chunks.size() - 1);

                // Asegúrate de que las actualizaciones a la GUI se hagan en el EDT
                series.addOrUpdate(currentGeneration, fitness);
                statusLabel.setText("Generation " + currentGeneration + " completed. Best fitness: " + fitness);
            }

            @Override
            protected void done() {
                // Esta función se llama cuando la tarea de fondo está completa
                // Puede usarse para hacer cualquier limpieza final o actualizaciones de estado
                try {
                    get(); // Llama a get para atrapar cualquier excepción que ocurra durante doInBackground
                    statusLabel.setText("All generations completed. Best fitness: " + population.getFittestIndividual().getFitness());
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Error in background task: no worries babe");
                }
            }
        };

        worker.execute(); // Ejecuta el SwingWorker; esto inicia el método doInBackground en un hilo de fondo
    }
    private void initializeHistogram() {
        HistogramDataset dataset = new HistogramDataset();
        histogramChart = ChartFactory.createHistogram(
                "Distribución de Fitness",
                "Fitness",
                "Frecuencia",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        histogramPanel = new ChartPanel(histogramChart);
        histogramPanel.setPreferredSize(new Dimension(400, 300));
    }
    private void updateHistogram() {
        if(population == null) return;
        double[] fitnessValues = new double[population.size()];
        for (int i = 0; i < population.size(); i++) {
            fitnessValues[i] = population.getIndividual(i).getFitness();
        }

        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Fitness", fitnessValues, 10); // Ajusta el '10' según el número de bins que desees

        histogramChart = ChartFactory.createHistogram(
                "Distribución de Fitness",
                "Fitness",
                "Frecuencia",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        histogramPanel.setChart(histogramChart);
    }
    private void initGA() {

        indexProblems = problemComboBox.getSelectedIndex();
        indexCrossover = crossoverTypeComboBox.getSelectedIndex();
        indexSelection = selectionTypeComboBox.getSelectedIndex();

        AbstractProblem problem = problems[indexProblems];
        iReproduction reproduction = reproductionMethods[indexCrossover];
        iSelection selection = selectionMethods[indexSelection];
        Constants.CROSSOVER_RATE = Double.parseDouble(crossoverRateField.getText());
        Constants.MUTATION_RATE = Double.parseDouble(mutationRateField.getText());
        Constants.TOURNAMENT_SELECTION_SIZE = Integer.parseInt(tournamentSizeField.getText());
        int initialPopulation = Integer.parseInt(initialPopulationField.getText());
        ga = new GeneticAlgorithm(problem, selection, reproduction);
        population = new Population(problem, initialPopulation);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GeneticAlgorithmGUI().setVisible(true));
    }
}
