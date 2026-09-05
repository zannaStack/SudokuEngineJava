package sudokuEngine.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sudokuEngine.model.Grid;

/**
 * Risolutore di Sudoku basato sull'algoritmo di Backtracking con euristica MRV
 * (Minimum Remaining Values - Most Constrained Variable).
 * Seleziona per prima la cella vuota con il minor numero di candidati legali,
 * massimizzando la potatura dell'albero di ricerca.
 */
// Ottimizzazione euristica MRV
public class BacktrackingSolver implements SudokuSolver {

    private final boolean randomized;

    public BacktrackingSolver() {
        this(false);
    }

    public BacktrackingSolver(boolean randomized) {
        this.randomized = randomized;
    }

    @Override
    public boolean solve(Grid grid) {
        return solveRecursive(grid);
    }

    private boolean solveRecursive(Grid grid) {
        int bestRow = -1;
        int bestCol = -1;
        int minCandidates = 10;
        List<Integer> bestCandidates = null;

        // Trova la cella con il minor numero di candidati (MRV Heuristic)
        for (int r = 0; r < Grid.SIZE; r++) {
            for (int c = 0; c < Grid.SIZE; c++) {
                if (grid.get(r, c) == 0) {
                    List<Integer> candidates = getValidCandidates(grid, r, c);
                    if (candidates.isEmpty()) {
                        // Dead end: cella vuota senza candidati legali
                        return false;
                    }
                    if (candidates.size() < minCandidates) {
                        minCandidates = candidates.size();
                        bestRow = r;
                        bestCol = c;
                        bestCandidates = candidates;
                        if (minCandidates == 1) {
                            break; // Ottimo locale
                        }
                    }
                }
            }
            if (minCandidates == 1) break;
        }

        // Se non ci sono celle vuote, la griglia è risolta con successo
        if (bestRow == -1) {
            return true;
        }

        if (randomized && bestCandidates != null) {
            Collections.shuffle(bestCandidates);
        }

        for (int num : bestCandidates) {
            grid.set(bestRow, bestCol, num);
            if (solveRecursive(grid)) {
                return true;
            }
            grid.set(bestRow, bestCol, 0); // Backtrack
        }

        return false;
    }

    @Override
    public int countSolutions(Grid grid, int limit) {
        return countRecursive(grid, limit, 0);
    }

    private int countRecursive(Grid grid, int limit, int currentCount) {
        if (currentCount >= limit) {
            return currentCount;
        }

        int bestRow = -1;
        int bestCol = -1;
        int minCandidates = 10;
        List<Integer> bestCandidates = null;

        for (int r = 0; r < Grid.SIZE; r++) {
            for (int c = 0; c < Grid.SIZE; c++) {
                if (grid.get(r, c) == 0) {
                    List<Integer> candidates = getValidCandidates(grid, r, c);
                    if (candidates.isEmpty()) {
                        return currentCount;
                    }
                    if (candidates.size() < minCandidates) {
                        minCandidates = candidates.size();
                        bestRow = r;
                        bestCol = c;
                        bestCandidates = candidates;
                        if (minCandidates == 1) break;
                    }
                }
            }
            if (minCandidates == 1) break;
        }

        if (bestRow == -1) {
            return currentCount + 1; // Trovata soluzione valida
        }

        for (int num : bestCandidates) {
            grid.set(bestRow, bestCol, num);
            currentCount = countRecursive(grid, limit, currentCount);
            grid.set(bestRow, bestCol, 0); // Backtrack
            if (currentCount >= limit) {
                break;
            }
        }

        return currentCount;
    }

    private List<Integer> getValidCandidates(Grid grid, int r, int c) {
        List<Integer> list = new ArrayList<>(9);
        for (int num = 1; num <= 9; num++) {
            if (grid.isValidPlacement(r, c, num)) {
                list.add(num);
            }
        }
        return list;
    }
}
