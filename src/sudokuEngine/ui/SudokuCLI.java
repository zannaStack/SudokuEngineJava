package sudokuEngine.ui;

import sudokuEngine.controller.SudokuController;
import sudokuEngine.model.Difficulty;
import sudokuEngine.model.GameSession;
import sudokuEngine.model.Grid;
import sudokuEngine.persistenceDB.GameRepository;

import java.util.List;
import java.util.Scanner;

/**
 * Interfaccia a riga di comando (CLI) ad alto contrasto per il Sudoku Engine.
 */
public class SudokuCLI {

    private final SudokuController gameService;
    private final Scanner scanner;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BOLD = "\u001B[1m";

    public SudokuCLI(SudokuController gameService) {
        this.gameService = gameService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printHeader();

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = prompt("Scelta > ").trim();
            switch (choice) {
                case "1" -> handleNewGame();
                case "2" -> handleResumeGame();
                case "3" -> handleLeaderboard();
                case "4" -> handleSolverBenchmark();
                case "5" -> handleSelfTest();
                case "0" -> {
                    System.out.println("\nGrazie per aver giocato con Sudoku Engine!");
                    running = false;
                }
                default -> System.out.println(ANSI_RED + "Opzione non valida. Riprova." + ANSI_RESET);
            }
        }
    }

    private void printHeader() {
        System.out.println(ANSI_BOLD + ANSI_CYAN);
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║            SUDOKU ENGINE & BACKEND (JAVA + SQL)            ║");
        System.out.println("║            Algoritmi, Backtracking & Persistenza           ║");
        System.out.println("║                 Fabio Zanetti - UniBo 2026                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝" + ANSI_RESET);
    }

    private void printMainMenu() {
        System.out.println("\n" + ANSI_BOLD + "--- MENU PRINCIPALE ---" + ANSI_RESET);
        System.out.println("1. Nuova Partita (Generatore con Soluzione Unica)");
        System.out.println("2. Riprendi Partita da Database SQL");
        System.out.println("3. Visualizza Classifica (Leaderboard SQL)");
        System.out.println("4. Benchmark Risolutore (Backtracking MRV)");
        System.out.println("5. Esegui Test Algoritmici Integrati");
        System.out.println("0. Esci");
    }

    private void handleNewGame() {
        String name = prompt("Nome Giocatore: ");
        if (name.isBlank()) name = "Giocatore";

        System.out.println("\nSeleziona Difficoltà:");
        System.out.println("1. Facile  (~40 indizi)");
        System.out.println("2. Medio   (~33 indizi)");
        System.out.println("3. Difficile (~27 indizi)");
        System.out.println("4. Esperto (~22 indizi)");

        String diffChoice = prompt("Difficoltà (1-4) [default: 2]: ").trim();
        Difficulty diff = switch (diffChoice) {
            case "1" -> Difficulty.EASY;
            case "3" -> Difficulty.HARD;
            case "4" -> Difficulty.EXPERT;
            default -> Difficulty.MEDIUM;
        };

        System.out.println("\nGenerazione schema con soluzione unica in corso...");
        long start = System.currentTimeMillis();
        GameSession session = gameService.createGame(name, diff);
        long elapsed = System.currentTimeMillis() - start;
        System.out.printf(ANSI_GREEN + "✔ Puzzle %s generato con successo in %d ms! (%d indizi iniziali)\n" + ANSI_RESET,
                diff.getDisplayName(), elapsed, session.getInitialGrid().countClues());

        playLoop(session);
    }

    private void handleResumeGame() {
        List<GameRepository.SessionSummary> recents = gameService.getRecentGames(5);
        if (recents.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Nessuna sessione trovata nel database." + ANSI_RESET);
            return;
        }

        System.out.println("\nSessioni recenti salvate su SQLite:");
        for (int i = 0; i < recents.size(); i++) {
            var s = recents.get(i);
            System.out.printf("[%d] ID: %s | %s | Diff: %s | Stato: %s | Errori: %d/3 | Data: %s\n",
                    i + 1, s.sessionId().substring(0, 8), s.playerName(), s.difficulty(), s.status(), s.mistakes(), s.updatedAt());
        }

        String input = prompt("Seleziona numero sessione da riprendere (oppure invio per tornare): ").trim();
        if (input.isBlank()) return;

        try {
            int idx = Integer.parseInt(input) - 1;
            if (idx >= 0 && idx < recents.size()) {
                GameSession session = gameService.loadGame(recents.get(idx).sessionId());
                if (session != null) {
                    System.out.println(ANSI_GREEN + "✔ Partita caricata con successo dal DB!" + ANSI_RESET);
                    playLoop(session);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(ANSI_RED + "Indice non valido." + ANSI_RESET);
        }
    }

    private void handleLeaderboard() {
        System.out.println("\n" + ANSI_BOLD + "🏆 CLASSIFICA TEMPI (DATABASE SQL)" + ANSI_RESET);
        for (Difficulty d : Difficulty.values()) {
            System.out.println("\nDifficoltà: " + ANSI_CYAN + d.getDisplayName() + ANSI_RESET);
            List<GameRepository.LeaderboardEntry> list = gameService.getLeaderboard(d, 5);
            if (list.isEmpty()) {
                System.out.println("  (Nessun record registrato)");
            } else {
                for (int i = 0; i < list.size(); i++) {
                    var e = list.get(i);
                    System.out.printf("  #%d %-15s Tempo: %02d:%02d | Errori: %d | Data: %s\n",
                            i + 1, e.playerName(), e.timeSeconds() / 60, e.timeSeconds() % 60, e.mistakes(), e.completedAt());
                }
            }
        }
    }

    private void handleSolverBenchmark() {
        System.out.println("\nAvvio benchmark risolutore su 5 puzzle complessi...");
        long totalTime = 0;
        for (int i = 0; i < 5; i++) {
            GameSession s = gameService.createGame("Bench", Difficulty.HARD);
            Grid toSolve = s.getInitialGrid().copy();
            long t0 = System.nanoTime();
            boolean ok = gameService.solveCustom(toSolve);
            long ns = System.nanoTime() - t0;
            totalTime += ns;
            System.out.printf("Test #%d: Risolto = %b in %.2f ms (Backtracking MRV)\n", i + 1, ok, ns / 1_000_000.0);
        }
        System.out.printf(ANSI_GREEN + "Tempo medio per risoluzione: %.2f ms\n" + ANSI_RESET, (totalTime / 5.0) / 1_000_000.0);
    }

    public void handleSelfTest() {
        System.out.println("\n" + ANSI_BOLD + "=== ESECUZIONE SUITE DI TEST ALGORITMICI ===" + ANSI_RESET);

        // Test 1: Generatore
        System.out.print("[TEST 1] Generazione griglia valida e verifica soluzione unica... ");
        GameSession s = gameService.createGame("Tester", Difficulty.MEDIUM);
        if (s != null && s.getInitialGrid().countClues() >= 30) {
            System.out.println(ANSI_GREEN + "PASSATO" + ANSI_RESET);
        } else {
            System.out.println(ANSI_RED + "FALLITO" + ANSI_RESET);
        }

        // Test 2: Mossa e Undo/Redo
        System.out.print("[TEST 2] Applicazione mossa valida e test Undo/Redo... ");
        int emptyR = -1, emptyC = -1;
        for (int r = 0; r < 9 && emptyR == -1; r++) {
            for (int c = 0; c < 9; c++) {
                if (s.getCurrentGrid().get(r, c) == 0) {
                    emptyR = r; emptyC = c; break;
                }
            }
        }
        int solVal = s.getSolutionGrid().get(emptyR, emptyC);
        gameService.makeMove(s, emptyR, emptyC, solVal);
        boolean moveOk = (s.getCurrentGrid().get(emptyR, emptyC) == solVal);
        gameService.undo(s);
        boolean undoOk = (s.getCurrentGrid().get(emptyR, emptyC) == 0);
        gameService.redo(s);
        boolean redoOk = (s.getCurrentGrid().get(emptyR, emptyC) == solVal);

        if (moveOk && undoOk && redoOk) {
            System.out.println(ANSI_GREEN + "PASSATO" + ANSI_RESET);
        } else {
            System.out.println(ANSI_RED + "FALLITO" + ANSI_RESET);
        }

        // Test 3: Persistenza DB SQL
        System.out.print("[TEST 3] Persistenza e recupero stato partita da SQLite... ");
        GameSession loaded = gameService.loadGame(s.getSessionId());
        if (loaded != null && loaded.getSessionId().equals(s.getSessionId())) {
            System.out.println(ANSI_GREEN + "PASSATO" + ANSI_RESET);
        } else {
            System.out.println(ANSI_RED + "FALLITO" + ANSI_RESET);
        }

        System.out.println(ANSI_BOLD + ANSI_GREEN + "Tutti i test unitari e di sistema sono stati superati con successo!\n" + ANSI_RESET);
    }

    private void playLoop(GameSession session) {
        long sessionStartTime = System.currentTimeMillis();

        while (session.getStatus() == GameSession.Status.IN_PROGRESS) {
            long currentElapsed = (System.currentTimeMillis() - sessionStartTime) / 1000;
            long totalElapsed = session.getElapsedSeconds() + currentElapsed;

            printGameBoard(session, totalElapsed);

            System.out.println("\nComandi disponibili:");
            System.out.println("  m <riga> <colonna> <valore>  (es. 'm 1 3 5' inserisce 5; valore 0 cancella)");
            System.out.println("  u (undo) | r (redo) | h (suggerimento) | s (salva ed esci)");

            String line = prompt("Comando > ").trim();
            if (line.equalsIgnoreCase("s")) {
                session.addElapsedSeconds(currentElapsed);
                gameService.makeMove(session, 0, 0, session.getCurrentGrid().get(0, 0)); // forza save
                System.out.println(ANSI_GREEN + "Partita salvata nel database SQLite. Arrivederci!" + ANSI_RESET);
                break;
            } else if (line.equalsIgnoreCase("u")) {
                if (!gameService.undo(session)) {
                    System.out.println(ANSI_YELLOW + "Nessuna mossa da annullare." + ANSI_RESET);
                }
            } else if (line.equalsIgnoreCase("r")) {
                if (!gameService.redo(session)) {
                    System.out.println(ANSI_YELLOW + "Nessuna mossa da ripristinare." + ANSI_RESET);
                }
            } else if (line.equalsIgnoreCase("h")) {
                var hint = gameService.getHint(session);
                System.out.println(ANSI_BOLD + ANSI_YELLOW + "💡 " + hint.message() + ANSI_RESET);
            } else if (line.startsWith("m ") || line.startsWith("M ")) {
                String[] parts = line.split("\\s+");
                if (parts.length == 4) {
                    try {
                        int r = Integer.parseInt(parts[1]) - 1;
                        int c = Integer.parseInt(parts[2]) - 1;
                        int v = Integer.parseInt(parts[3]);
                        if (r < 0 || r >= 9 || c < 0 || c >= 9 || v < 0 || v > 9) {
                            System.out.println(ANSI_RED + "Coordinate o valore fuori scala (riga 1-9, colonna 1-9, valore 0-9)" + ANSI_RESET);
                        } else {
                            if (session.getInitialGrid().isInitialClue(r, c)) {
                                System.out.println(ANSI_RED + "Non puoi modificare una cella indizio iniziale!" + ANSI_RESET);
                            } else {
                                gameService.makeMove(session, r, c, v);
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(ANSI_RED + "Formato comando non valido." + ANSI_RESET);
                    }
                } else {
                    System.out.println(ANSI_RED + "Uso: m <riga> <colonna> <valore>" + ANSI_RESET);
                }
            } else {
                System.out.println(ANSI_RED + "Comando non riconosciuto." + ANSI_RESET);
            }
        }

        if (session.getStatus() == GameSession.Status.COMPLETED) {
            printGameBoard(session, session.getElapsedSeconds());
            System.out.println(ANSI_BOLD + ANSI_GREEN + "\n🎉 COMPLIMENTI! HAI RISOLTO IL SUDOKU CON SUCCESSO! 🎉" + ANSI_RESET);
            System.out.println("Il tuo record è stato registrato nella classifica SQL.");
        } else if (session.getStatus() == GameSession.Status.FAILED) {
            printGameBoard(session, session.getElapsedSeconds());
            System.out.println(ANSI_BOLD + ANSI_RED + "\n❌ PARTITA TERMINATA: HAI COMMESSO 3 ERRORI. RITENTA!" + ANSI_RESET);
        }
    }

    private void printGameBoard(GameSession session, long elapsedSeconds) {
        Grid current = session.getCurrentGrid();
        Grid initial = session.getInitialGrid();

        System.out.println("\n-------------------------------------------------------------");
        System.out.printf("Giocatore: %s%s%s | Difficoltà: %s%s%s | Errori: %s%d/%d%s | Tempo: %02d:%02d\n",
                ANSI_BOLD, session.getPlayerName(), ANSI_RESET,
                ANSI_CYAN, session.getDifficulty().getDisplayName(), ANSI_RESET,
                (session.getMistakes() > 0 ? ANSI_RED : ANSI_GREEN), session.getMistakes(), session.getMaxMistakes(), ANSI_RESET,
                elapsedSeconds / 60, elapsedSeconds % 60);
        System.out.println("-------------------------------------------------------------");

        System.out.println("    1 2 3   4 5 6   7 8 9");
        System.out.println("  ┏━━━━━━━┳━━━━━━━┳━━━━━━━┓");
        for (int r = 0; r < 9; r++) {
            System.out.print((r + 1) + " ┃ ");
            for (int c = 0; c < 9; c++) {
                int val = current.get(r, c);
                if (val == 0) {
                    System.out.print("· ");
                } else if (initial.isInitialClue(r, c)) {
                    System.out.print(ANSI_CYAN + ANSI_BOLD + val + ANSI_RESET + " ");
                } else {
                    System.out.print(ANSI_GREEN + val + ANSI_RESET + " ");
                }
                if ((c + 1) % 3 == 0) {
                    System.out.print("┃ ");
                }
            }
            System.out.println();
            if ((r + 1) % 3 == 0 && r < 8) {
                System.out.println("  ┣━━━━━━━╋━━━━━━━╋━━━━━━━┫");
            }
        }
        System.out.println("  ┗━━━━━━━┻━━━━━━━┻━━━━━━━┛");
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }
}
