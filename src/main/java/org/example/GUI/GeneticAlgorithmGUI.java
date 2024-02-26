package org.example.GUI;
import org.example.GA.Agents.Abilities.Crossover.DoublePointCrossover;
import org.example.GA.Agents.Abilities.Crossover.SinglePointCrossover;
import org.example.GA.Agents.Abilities.Crossover.UniformCrossover;
import org.example.GA.Agents.Abilities.Selection.RouletteSelection;
import org.example.GA.Agents.Abilities.Selection.TournamentSelection;
import org.example.GA.Agents.Abilities.iReproduction;
import org.example.GA.Agents.Abilities.iSelection;
import org.example.GA.Agents.Population;
import org.example.GA.GeneticAlgorithm;
import org.example.OptimizationProblems.AbstractProblem;
import org.example.OptimizationProblems.KnapsackProblem;
import org.example.OptimizationProblems.TravelingSalesmanProblem;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class GeneticAlgorithmGUI extends JFrame {

    private GeneticAlgorithm ga;
    private Population population;

    AbstractProblem[] problems = new AbstractProblem[] {
        new KnapsackProblem(),
        new TravelingSalesmanProblem()
    };

    iReproduction[] reproductionMethods = new iReproduction[] {
        new SinglePointCrossover(),
        new DoublePointCrossover(),
        new UniformCrossover()
    };

    iSelection[] selectionMethods = new iSelection[] {
        new RouletteSelection(),
        new TournamentSelection()
    };

    private JComboBox<String> problemComboBox;
    private JComboBox<String> crossoverTypeComboBox;
    private JComboBox<String> selectionTypeComboBox;
    private JTextField crossoverRateField;
    private JTextField mutationRateField;
    private JTextField tournamentSizeField;
    private JTextField initialPopulationField;
    private JLabel statusLabel;

    public GeneticAlgorithmGUI() {
        setTitle("Genetic Algorithm Simulator");
        setSize(1500, 1000); // Ajustamos el tamaño inicial de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); // Usamos BorderLayout como layout principal

        JPanel sidePanel = new JPanel(new GridLayout(0, 1, 5, 1)); // Modificamos para acomodar más elementos con menos espacio entre ellos
        add(sidePanel, BorderLayout.WEST);

        JButton runOneGenerationButton = new JButton("Run 1 Generation");
        sidePanel.add(runOneGenerationButton);

        JButton runNGenerationsButton = new JButton("Run N Generations");
        sidePanel.add(runNGenerationsButton);

        JButton runForTimeButton = new JButton("Run For Time");
        sidePanel.add(runForTimeButton);

        JButton restartAlgorithmButton = new JButton("Restart Algorithm");
        sidePanel.add(restartAlgorithmButton);

        problemComboBox = new JComboBox<>(new String[]{"Knapsack Problem", "Traveling Salesman Problem"});
        sidePanel.add(new JLabel("Problem Configuration:"));
        sidePanel.add(problemComboBox);

        crossoverTypeComboBox = new JComboBox<>(new String[]{"Single Point", "Double Point", "Uniform"});
        sidePanel.add(new JLabel("Crossover Type:"));
        sidePanel.add(crossoverTypeComboBox);

        selectionTypeComboBox = new JComboBox<>(new String[]{"Roulette", "Tournament"});
        sidePanel.add(new JLabel("Selection Type:"));
        sidePanel.add(selectionTypeComboBox);

        JPanel constantsPanel = new JPanel(new GridLayout(0, 2, 0, 0));
        sidePanel.add(new JLabel("Constants Configuration:"));
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

        JPanel displayPanel = new JPanel();
        displayPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(displayPanel, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        add(statusPanel, BorderLayout.SOUTH);

        statusLabel = new JLabel("Status: Ready");
        statusPanel.add(statusLabel);

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
        // Implementa la lógica para los otros botones según sea necesario
    }

    private void runOneGeneration(int generationNumber) {
        if (ga == null) {
            initGA();
        }
        for (int i = 0; i < generationNumber; i++) {
            population = ga.evolve(population);
        }
        // Actualiza el estado de la GUI aquí, por ejemplo:
        // statusLabel.setText("Generation " + generationNumber + " completed. Best fitness: " + population.getFittestIndividual().getFitness());
    }

    private void initGA() {
        // Esta función debería inicializar `ga` con la configuración seleccionada
        // Asegúrate de que `ga` se inicialice aquí antes de su uso
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GeneticAlgorithmGUI().setVisible(true));
    }
}
