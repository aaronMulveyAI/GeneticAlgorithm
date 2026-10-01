package org.example.GA;

import org.example.Experiment.ParameterTuning;
import org.example.GA.Agents.*;
import org.example.GA.Agents.Abilities.*;
import org.example.GA.Agents.Abilities.Crossover.*;
import org.example.GA.Agents.Abilities.Selection.*;
import org.example.OptimizationProblems.Modelling.*;
import org.example.OptimizationProblems.OptimizationMethod;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class GeneticAlgorithmTest {
    @BeforeEach
    void seedRandom() { Constants.RANDOM.setSeed(12345); }

    static Stream<iReproduction> crossovers() {
        return Stream.of(new SinglePointCrossover(), new DoublePointCrossover(), new UniformCrossover());
    }

    static Stream<iSelection> selections() {
        return Stream.of(new TournamentSelection(5), new RouletteSelection(),
                new TruncationSelection(0.5), new BrindleSelection());
    }

    @ParameterizedTest
    @MethodSource("crossovers")
    void crossoversPreservePermutationsAndParents(iReproduction crossover) {
        for (int size : new int[]{1, 2, 9}) {
            AbstractProblem problem = new CircularTSProblem(size);
            Individual father = new Individual(problem);
            Individual mother = new Individual(problem);
            int[] fatherBefore = father.getGenes();
            int[] motherBefore = mother.getGenes();
            for (int iteration = 0; iteration < 200; iteration++) {
                Individual child = crossover.crossover(father, mother);
                assertDoesNotThrow(() -> problem.validateSolution(child.getGenes()));
                assertTrue(Double.isFinite(child.getFitness()));
                assertNotSame(father, child);
                assertNotSame(mother, child);
            }
            assertArrayEquals(fatherBefore, father.getGenes());
            assertArrayEquals(motherBefore, mother.getGenes());
        }
    }

    @ParameterizedTest
    @MethodSource("crossovers")
    void combinatorialCrossoversOnlyInheritParentGenes(iReproduction crossover) {
        for (int size : new int[]{1, 2, 9}) {
            GuessNumberProblem problem = new GuessNumberProblem(size);
            int[] zeroes = new int[size];
            int[] nines = new int[size];
            Arrays.fill(nines, 9);
            Individual father = new Individual(problem, zeroes);
            Individual mother = new Individual(problem, nines);
            for (int iteration = 0; iteration < 100; iteration++) {
                int[] child = crossover.crossover(father, mother).getGenes();
                assertTrue(Arrays.stream(child).allMatch(gene -> gene == 0 || gene == 9));
            }
        }
    }

    @Test
    void fullMutationFlipsBitsWithoutChangingParents() {
        KnapsackProblem problem = new KnapsackProblem(new int[]{1, 2, 3}, new int[]{2, 3, 4}, 10);
        Population population = population(problem, new int[]{0, 1, 0});
        GeneticAlgorithm algorithm = new GeneticAlgorithm(problem, p -> p.getIndividual(0),
                new SinglePointCrossover(), 0, 1);
        Population next = algorithm.evolve(population);
        assertArrayEquals(new int[]{1, 0, 1}, next.getIndividual(0).getGenes());
        assertArrayEquals(new int[]{0, 1, 0}, population.getIndividual(0).getGenes());
    }

    @Test
    void digitMutationUsesDigitsRegardlessOfSequenceLength() {
        for (int size : new int[]{1, 3, 50}) {
            GuessNumberProblem problem = new GuessNumberProblem(size);
            Population population = population(problem, new int[size]);
            GeneticAlgorithm algorithm = new GeneticAlgorithm(problem, p -> p.getIndividual(0),
                    new UniformCrossover(), 0, 1);
            int[] genes = algorithm.evolve(population).getIndividual(0).getGenes();
            assertTrue(Arrays.stream(genes).allMatch(gene -> gene > 0 && gene < 10));
        }
    }

    @Test
    void singletonPermutationMutationTerminates() {
        CircularTSProblem problem = new CircularTSProblem(1);
        GeneticAlgorithm algorithm = new GeneticAlgorithm(problem, new TournamentSelection(1),
                new UniformCrossover(), 1, 1);
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Population next = algorithm.evolve(new Population(problem, 1));
            assertArrayEquals(new int[]{0}, next.getIndividual(0).getGenes());
            assertEquals(0, next.getFittestIndividual().getFitness());
        });
    }

    @Test
    void ratesControlEveryCrossoverAndAreCapturedPerAlgorithm() {
        ScalarProblem problem = new ScalarProblem(false, 1, 3);
        Population source = population(problem, new int[]{0}, new int[]{1});
        int[] calls = {0};
        iReproduction operator = new SinglePointCrossover() {
            @Override
            public Individual crossover(Individual father, Individual mother) {
                calls[0]++;
                return father.copy();
            }
        };
        GeneticAlgorithm disabled = new GeneticAlgorithm(problem, new TournamentSelection(1), operator, 0, 0);
        GeneticAlgorithm enabled = new GeneticAlgorithm(problem, new TournamentSelection(1), operator, 1, 0);
        disabled.evolve(source);
        assertEquals(0, calls[0]);
        enabled.evolve(source);
        assertEquals(2, calls[0]);
        disabled.evolve(source);
        assertEquals(2, calls[0]);
    }

    @Test
    void optimizationDirectionIsIndependentOfEncodingAndOtherAlgorithms() {
        ScalarProblem minimizing = new ScalarProblem(true, 1, 9);
        Population population = population(minimizing, new int[]{0}, new int[]{1});
        new GeneticAlgorithm(new GuessNumberProblem(), new RouletteSelection(), new UniformCrossover());
        assertEquals(1, population.getFittestIndividual().getFitness());
        assertEquals(9, population.getLeastFitIndividual().getFitness());
        assertEquals(0, new TruncationSelection(0.5).selectIndividual(population).getGene(0));
    }

    @ParameterizedTest
    @MethodSource("selections")
    void selectorsHandleSingletonsAndEqualZeroFitness(iSelection selection) {
        ScalarProblem problem = new ScalarProblem(false, 0, 0);
        Population singleton = population(problem, new int[]{0});
        Population zeroes = population(problem, new int[]{0}, new int[]{1});
        for (int iteration = 0; iteration < 100; iteration++) {
            assertSame(singleton.getIndividual(0), selection.selectIndividual(singleton));
            assertTrue(Arrays.asList(zeroes.getIndividuals()).contains(selection.selectIndividual(zeroes)));
        }
    }

    @Test
    void weightedSelectorsSampleTheWholePopulationWithCorrectDirection() {
        for (iSelection selection : new iSelection[]{new RouletteSelection(), new BrindleSelection()}) {
            assertSamplingRate(selection, new ScalarProblem(false, 1, 3), 0.75, 0.03);
            assertSamplingRate(selection, new ScalarProblem(false, 0, 0), 0.5, 0.03);
            assertSamplingRate(selection, new ScalarProblem(false, -5, -1), 1, 0.01);
            assertSamplingRate(selection, new ScalarProblem(true, 1, 3), 0, 0.01);
            assertSamplingRate(selection, new ScalarProblem(false, 1e300, 3e300), 0.75, 0.03);
        }
    }

    private void assertSamplingRate(iSelection selection, ScalarProblem problem, double expected, double tolerance) {
        Population population = population(problem, new int[]{0}, new int[]{1});
        int selected = 0;
        int draws = 10000;
        for (int i = 0; i < draws; i++) {
            if (selection.selectIndividual(population).getGene(0) == 1) selected++;
        }
        assertEquals(expected, selected / (double) draws, tolerance,
                selection.getClass().getSimpleName());
    }

    @Test
    void fitnessIsCachedAndChromosomeArraysAreDefensiveCopies() {
        ScalarProblem problem = new ScalarProblem(false, 2, 5);
        int[] input = {0};
        Individual individual = new Individual(problem, input);
        input[0] = 1;
        assertEquals(2, individual.getFitness());
        assertEquals(2, individual.calculateFitness());
        individual.getGenes()[0] = 1;
        assertEquals(2, individual.getFitness());
        assertEquals(1, problem.evaluations);
        individual.setGene(0, 1);
        assertEquals(5, individual.getFitness());
        assertEquals(2, problem.evaluations);
    }

    @Test
    void tournamentAndEvolutionDoNotGenerateDiscardedRandomIndividuals() {
        ScalarProblem problem = new ScalarProblem(false, 2, 5);
        Population population = new Population(problem, 8);
        int samples = problem.samples;
        new TournamentSelection(20).selectIndividual(population);
        GeneticAlgorithm algorithm = new GeneticAlgorithm(problem, new TournamentSelection(5),
                new UniformCrossover(), 1, 0);
        algorithm.evolve(population);
        assertEquals(samples, problem.samples);
    }

    @Test
    void problemInstancesKeepTheirOwnData() {
        GuessNumberProblem originalGuess = new GuessNumberProblem(5);
        int[] target = originalGuess.getTargetSequence();
        new GuessNumberProblem(1);
        assertEquals(5, originalGuess.solve(target));
        target[0] = (target[0] + 1) % 10;
        assertEquals(5, originalGuess.solve(originalGuess.getTargetSequence()));
        NQueensProblem queens = new NQueensProblem(4);
        new NQueensProblem(1);
        assertEquals(6, queens.solve(new int[]{1, 3, 0, 2}));
        CircularTSProblem circular = new CircularTSProblem(4);
        double distance = circular.solve(new int[]{0, 1, 2, 3});
        new CircularTSProblem(1);
        assertEquals(distance, circular.solve(new int[]{0, 1, 2, 3}));
        int[] weights = {2, 3};
        KnapsackProblem knapsack = new KnapsackProblem(weights, new int[]{4, 6}, 5);
        weights[0] = 100;
        new KnapsackProblem(new int[]{9}, new int[]{9}, 0);
        assertEquals(10, knapsack.solve(new int[]{1, 1}));
        double[][] distances = {{0, 2}, {2, 0}};
        TravelingSalesmanProblem tsp = new TravelingSalesmanProblem(distances);
        distances[0][1] = 100;
        tsp.getDistances()[1][0] = 100;
        new TravelingSalesmanProblem(new double[][]{{0}});
        assertEquals(4, tsp.solve(new int[]{0, 1}));
    }

    @Test
    void realEncodingHasCorrectEndpointsAndFiniteValueAtZero() {
        RealValueOptimizationProblem problem = new RealValueOptimizationProblem();
        int[] ones = new int[32];
        Arrays.fill(ones, 1);
        assertEquals(-100, problem.binaryToReal(new int[32]));
        assertEquals(100, problem.binaryToReal(ones));
        assertEquals(7.5, problem.evaluate(0));
    }

    @Test
    void invalidModelsParametersAndChromosomesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new NQueensProblem(0));
        assertThrows(IllegalArgumentException.class, () -> new Population(new GuessNumberProblem(), 0));
        assertThrows(IllegalArgumentException.class, () -> new TournamentSelection(0));
        for (double threshold : new double[]{0, -1, 2, Double.NaN}) {
            assertThrows(IllegalArgumentException.class, () -> new TruncationSelection(threshold));
        }
        KnapsackProblem knapsack = new KnapsackProblem();
        assertThrows(IllegalArgumentException.class, () -> new Individual(knapsack, new int[]{0}));
        int[] invalidBits = new int[knapsack.getModelSize()];
        invalidBits[0] = 2;
        assertThrows(IllegalArgumentException.class, () -> knapsack.solve(invalidBits));
        TravelingSalesmanProblem tsp = new TravelingSalesmanProblem(new double[][]{{0, 1}, {1, 0}});
        assertThrows(IllegalArgumentException.class, () -> tsp.solve(new int[]{0, 0}));
        assertThrows(IllegalArgumentException.class, () -> tsp.solve(new int[]{-1, 1}));
        assertThrows(IllegalArgumentException.class, () -> new TravelingSalesmanProblem(new double[][]{{0, -1}, {1, 0}}));
        for (double rate : new double[]{-0.1, 1.1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> new GeneticAlgorithm(knapsack,
                    new TournamentSelection(1), new UniformCrossover(), rate, 0));
            assertThrows(IllegalArgumentException.class, () -> new GeneticAlgorithm(knapsack,
                    new TournamentSelection(1), new UniformCrossover(), 0, rate));
        }
        ScalarProblem nonfinite = new ScalarProblem(false, Double.NaN, 0);
        assertThrows(IllegalStateException.class, () -> new Individual(nonfinite, new int[]{0}).getFitness());
    }

    @Test
    void differentProblemParentsAndPopulationsCannotBeMixed() {
        GuessNumberProblem first = new GuessNumberProblem(2);
        GuessNumberProblem second = new GuessNumberProblem(2);
        GeneticAlgorithm algorithm = new GeneticAlgorithm(first, new TournamentSelection(1), new UniformCrossover());
        assertThrows(IllegalArgumentException.class, () -> algorithm.evolve(new Population(second, 2)));
        assertThrows(IllegalArgumentException.class, () -> new UniformCrossover().crossover(
                new Individual(first), new Individual(second)));
        assertThrows(IllegalArgumentException.class, () -> new Population(first, 2).saveIndividual(0, new Individual(second)));
    }

    @TestFactory
    Stream<DynamicTest> everyProblemWorksWithEveryOperatorCombination() {
        AbstractProblem[] problems = {
                new TravelingSalesmanProblem(TravelingSalesmanProblem.generateDistances(8)),
                new CircularTSProblem(8), new KnapsackProblem(), new NQueensProblem(8),
                new GuessNumberProblem(17), new RealValueOptimizationProblem()
        };
        return Arrays.stream(problems).flatMap(problem -> selections().flatMap(selection ->
                crossovers().map(crossover -> DynamicTest.dynamicTest(problem.getName() + " / "
                        + selection.getClass().getSimpleName() + " / " + crossover.getClass().getSimpleName(), () -> {
                    Constants.RANDOM.setSeed(100);
                    Population population = new Population(problem, 17);
                    GeneticAlgorithm algorithm = new GeneticAlgorithm(problem, selection, crossover, 0.8, 0.2);
                    for (int generation = 0; generation < 15; generation++) {
                        population = algorithm.evolve(population);
                        assertEquals(17, population.size());
                        for (Individual individual : population.getIndividuals()) {
                            problem.validateSolution(individual.getGenes());
                            assertTrue(Double.isFinite(individual.getFitness()));
                        }
                    }
                }))));
    }

    @Test
    void benchmarksIncludeInitialFitnessAndReportFitnessSeparatelyFromGeneration() {
        ScalarProblem problem = new ScalarProblem(false, 2.5, 5.5);
        GeneticAlgorithm algorithm = new GeneticAlgorithm(problem, p -> p.getIndividual(0),
                new SinglePointCrossover(), 0, 0);
        ParameterTuning.BenchmarkResult result = ParameterTuning.benchmark(problem, algorithm, 3, 5, 1);
        assertEquals(2.5, result.averageBestFitness());
        assertEquals(0, result.averageBestGeneration());
        assertEquals(0, result.generationStandardDeviation());
        assertEquals(0, ParameterTuning.benchmark(problem, algorithm, 1, 0, 1).generationStandardDeviation());
    }

    private Population population(AbstractProblem problem, int[]... genes) {
        Population population = new Population(problem, genes.length, false);
        for (int i = 0; i < genes.length; i++) population.saveIndividual(i, new Individual(problem, genes[i]));
        return population;
    }

    private static class ScalarProblem extends AbstractProblem {
        private final boolean minimizing;
        private final double[] scores;
        private int evaluations;
        private int samples;

        ScalarProblem(boolean minimizing, double... scores) {
            super(null, "Scalar", OptimizationMethod.COMBINATORIAL, 1);
            this.minimizing = minimizing;
            this.scores = scores;
        }
        @Override public int getGeneValueCount() { return scores.length; }
        @Override public int[] sampleSolution() { samples++; return new int[]{0}; }
        @Override public double solve(int[] solution) { evaluations++; return scores[solution[0]]; }
        @Override public AbstractProblem generateRandom(int n) { return this; }
        @Override public OPTIMIZATION_TYPE getOptimizationType() {
            return minimizing ? OPTIMIZATION_TYPE.MINIMIZE : OPTIMIZATION_TYPE.MAXIMIZE;
        }
    }
}
