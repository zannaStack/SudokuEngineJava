package sudokuEngine.controller;

import sudokuEngine.model.Grid;

/**
 * Interfaccia comune per gli algoritmi di risoluzione del Sudoku (Strategy Pattern).
 */
public interface SudokuSolver {
    /**
     * Risolve la griglia passata in-place.
     * @param grid Griglia da risolvere
     * @return true se la griglia è risolvibile, false altrimenti
     */
    boolean solve(Grid grid);

    /**
     * Conta quante soluzioni valide esistono per la griglia data,
     * fermandosi una volta raggiunta la soglia 'limit' (utilissimo per verificare l'unicità della soluzione = 1).
     * @param grid Griglia da analizzare
     * @param limit Limite massimo di soluzioni da cercare (es. 2)
     * @return Numero di soluzioni trovate (compreso tra 0 e limit)
     */
    int countSolutions(Grid grid, int limit);
}
