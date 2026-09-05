package sudokuEngine.controller;

import sudokuEngine.model.Grid;

/**
 * Risolutore base di Sudoku con backtracking ricorsivo standard.
 * Prova a inserire i valori da 1 a 9 sulla prima cella vuota trovata.
 */
public class BacktrackingSolver implements SudokuSolver {

    @Override
    public boolean solve(Grid grid) {
        return solveRecursive(grid);
    }

    private boolean solveRecursive(Grid grid) {
        // Scansione riga per riga alla ricerca della prima cella vuota
        for (int r = 0; r < Grid.SIZE; r++) {
            for (int c = 0; c < Grid.SIZE; c++) {
                if (grid.get(r, c) == 0) {
                    for (int val = 1; val <= 9; val++) {
                        if (grid.isValidPlacement(r, c, val)) {
                            grid.set(r, c, val);
                            if (solveRecursive(grid)) {
                                return true;
                            }
                            grid.set(r, c, 0); // Backtrack
                        }
                    }
                    return false; // Nessun valore valido trovato
                }
            }
        }
        return true; // Nessuna cella vuota rimasta, griglia risolta
    }
}
