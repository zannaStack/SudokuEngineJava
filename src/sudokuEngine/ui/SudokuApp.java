package sudokuEngine.ui;

import javafx.application.Application;
import sudokuEngine.controller.SudokuController;
import sudokuEngine.persistenceDB.DatabaseManager;

/**
 * Entry Point principale unificato dell'applicazione Sudoku Engine & Solver.
 * Avvia l'interfaccia grafica JavaFX per impostazione predefinita,
 * oppure la CLI interattiva se invocato con '--cli' o la suite di test con '--test'.
 */
public class SudokuApp {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("--test")) {
            DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite:sudoku.db");
            SudokuController service = new SudokuController(dbManager);
            SudokuCLI cli = new SudokuCLI(service);
            cli.handleSelfTest();
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("--cli")) {
            DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite:sudoku.db");
            SudokuController service = new SudokuController(dbManager);
            SudokuCLI cli = new SudokuCLI(service);
            cli.start();
            return;
        }

        // Avvio interfaccia grafica JavaFX
        Application.launch(SudokuGUI.class, args);
    }
}
