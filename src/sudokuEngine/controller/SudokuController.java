package sudokuEngine.controller;

import sudokuEngine.model.Difficulty;
import sudokuEngine.model.GameSession;
import sudokuEngine.model.Grid;
import sudokuEngine.persistenceDB.DatabaseManager;
import sudokuEngine.persistenceDB.GameRepository;

import java.util.List;

/**
 * Service Layer che incapsula la logica di business applicativa:
 * creazione partite, applicazione mosse, suggerimenti intelligenti (hints),
 * persistenza transazionale e query sulla leaderboard.
 */
public class SudokuController {

    private final SudokuGenerator generator;
    private final SudokuSolver solver;
    private final GameRepository repository;
    private int moveCounter = 0;

    public record Hint(int row, int col, int value, String message) {}

    public SudokuController(DatabaseManager dbManager) {
        this.generator = new SudokuGenerator();
        this.solver = new BacktrackingSolver(false);
        this.repository = new GameRepository(dbManager);
    }

    public GameSession createGame(String playerName, Difficulty difficulty) {
        SudokuGenerator.GeneratedPuzzle puzzle = generator.generate(difficulty);
        GameSession session = new GameSession(playerName, difficulty, puzzle.getPuzzle(), puzzle.getSolution());
        repository.saveSession(session);
        return session;
    }

    public boolean makeMove(GameSession session, int row, int col, int value) {
        int oldValue = session.getCurrentGrid().get(row, col);
        boolean applied = session.applyMove(row, col, value);
        if (applied) {
            moveCounter++;
            repository.saveSession(session);

            if (session.getStatus() == GameSession.Status.COMPLETED) {
                repository.addLeaderboardRecord(
                        session.getPlayerName(),
                        session.getDifficulty(),
                        (int) session.getElapsedSeconds(),
                        session.getMistakes()
                );
            }
        }
        return applied;
    }

    public boolean undo(GameSession session) {
        boolean undone = session.undo();
        if (undone) {
            repository.saveSession(session);
        }
        return undone;
    }

    public boolean redo(GameSession session) {
        boolean redone = session.redo();
        if (redone) {
            repository.saveSession(session);
        }
        return redone;
    }

    public Hint getHint(GameSession session) {
        Grid current = session.getCurrentGrid();
        Grid solution = session.getSolutionGrid();

        // Cerca la prima cella vuota o errata
        for (int r = 0; r < Grid.SIZE; r++) {
            for (int c = 0; c < Grid.SIZE; c++) {
                if (current.get(r, c) == 0 || current.get(r, c) != solution.get(r, c)) {
                    int correctVal = solution.get(r, c);
                    return new Hint(r, c, correctVal,
                            String.format("Suggerimento: Cella (%d, %d) -> valore corretto è %d", r + 1, c + 1, correctVal));
                }
            }
        }
        return new Hint(-1, -1, 0, "Nessun suggerimento: la griglia è già completata!");
    }

    public boolean solveCustom(Grid grid) {
        return solver.solve(grid);
    }

    public GameSession loadGame(String sessionId) {
        return repository.loadSession(sessionId);
    }

    public List<GameRepository.SessionSummary> getRecentGames(int limit) {
        return repository.listRecentSessions(limit);
    }

    public List<GameRepository.LeaderboardEntry> getLeaderboard(Difficulty difficulty, int limit) {
        return repository.getTopScores(difficulty, limit);
    }
}
