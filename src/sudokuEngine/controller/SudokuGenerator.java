package sudokuEngine.controller;

import sudokuEngine.model.Difficulty;
import sudokuEngine.model.Grid;

import java.util.*;

/**
 * Generatore algoritmico di schemi Sudoku garantiti con soluzione unica.
 * Implementa la tecnica di scavo a ritroso con verifica di unicità (Unique Solution Check)
 * e simmetria rotazionale opzionale.
 */
public class SudokuGenerator {

    private final Random random;
    private final SudokuSolver solver;

    public static class GeneratedPuzzle {
        private final Grid puzzle;
        private final Grid solution;
        private final Difficulty difficulty;

        public GeneratedPuzzle(Grid puzzle, Grid solution, Difficulty difficulty) {
            this.puzzle = puzzle;
            this.solution = solution;
            this.difficulty = difficulty;
        }

        public Grid getPuzzle() {
            return puzzle;
        }

        public Grid getSolution() {
            return solution;
        }

        public Difficulty getDifficulty() {
            return difficulty;
        }
    }

    public SudokuGenerator() {
        this(new Random());
    }

    public SudokuGenerator(Random random) {
        this.random = random;
        this.solver = new BacktrackingSolver(true);
    }

    /**
     * Genera una nuova coppia puzzle/soluzione per la difficoltà specificata.
     */
    public GeneratedPuzzle generate(Difficulty difficulty) {
        // 1. Genera una griglia completa valida casuale
        Grid fullGrid = generateCompleteGrid();
        Grid solution = fullGrid.copy();

        // 2. Determina il target di indizi da lasciare
        int targetClues = difficulty.getMinClues() + random.nextInt(difficulty.getMaxClues() - difficulty.getMinClues() + 1);

        // 3. Prepara la lista delle 81 celle in ordine casuale
        List<int[]> positions = new ArrayList<>(81);
        for (int r = 0; r < Grid.SIZE; r++) {
            for (int c = 0; c < Grid.SIZE; c++) {
                positions.add(new int[]{r, c});
            }
        }
        Collections.shuffle(positions, random);

        Grid puzzle = fullGrid.copy();
        int currentClues = 81;

        // 4. Rimuovi celle assicurando che la soluzione rimanga UNICA
        for (int[] pos : positions) {
            if (currentClues <= targetClues) {
                break;
            }

            int r = pos[0];
            int c = pos[1];
            int backup = puzzle.get(r, c);

            puzzle.set(r, c, 0);

            // Verifica unicità: se esistono 2 o più soluzioni, la mossa non è valida
            Grid testGrid = puzzle.copy();
            int solutionCount = solver.countSolutions(testGrid, 2);

            if (solutionCount != 1) {
                // Ripristina il valore per preservare l'unicità
                puzzle.set(r, c, backup);
            } else {
                currentClues--;
            }
        }

        puzzle.markCurrentAsClues();
        return new GeneratedPuzzle(puzzle, solution, difficulty);
    }

    private Grid generateCompleteGrid() {
        Grid grid = new Grid();

        // Riempi le 3 sottomatrici diagonali indipendenti (0,0), (3,3), (6,6)
        for (int i = 0; i < Grid.SIZE; i += Grid.SUBGRID_SIZE) {
            fillSubgridRandomly(grid, i, i);
        }

        // Risolvi il resto tramite backtracking casuale
        BacktrackingSolver randomizedSolver = new BacktrackingSolver(true);
        boolean solved = randomizedSolver.solve(grid);
        if (!solved) {
            // Fallback fail-safe
            return generateCompleteGrid();
        }

        return grid;
    }

    private void fillSubgridRandomly(Grid grid, int startRow, int startCol) {
        List<Integer> digits = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
        Collections.shuffle(digits, random);

        int idx = 0;
        for (int r = 0; r < Grid.SUBGRID_SIZE; r++) {
            for (int c = 0; c < Grid.SUBGRID_SIZE; c++) {
                grid.set(startRow + r, startCol + c, digits.get(idx++));
            }
        }
    }
}
