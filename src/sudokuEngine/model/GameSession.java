package sudokuEngine.model;

import java.util.*;

/**
 * Gestisce l'intero stato di una partita di Sudoku:
 * griglie (iniziale, corrente, soluzione), storico mosse (undo/redo), errori e tempo.
 */
public class GameSession {
    public enum Status {
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    private final String sessionId;
    private String playerName;
    private final Difficulty difficulty;
    private final Grid initialGrid;
    private final Grid currentGrid;
    private final Grid solutionGrid;
    private final Deque<Move> undoStack;
    private final Deque<Move> redoStack;
    private long elapsedSeconds;
    private int mistakes;
    private final int maxMistakes;
    private Status status;

    public GameSession(String playerName, Difficulty difficulty, Grid initialGrid, Grid solutionGrid) {
        this.sessionId = UUID.randomUUID().toString();
        this.playerName = (playerName == null || playerName.isBlank()) ? "Giocatore" : playerName.trim();
        this.difficulty = difficulty;
        this.initialGrid = initialGrid.copy();
        this.currentGrid = initialGrid.copy();
        this.solutionGrid = solutionGrid.copy();
        this.undoStack = new ArrayDeque<>();
        this.redoStack = new ArrayDeque<>();
        this.elapsedSeconds = 0;
        this.mistakes = 0;
        this.maxMistakes = 3;
        this.status = Status.IN_PROGRESS;
    }

    public GameSession(String sessionId, String playerName, Difficulty difficulty,
                       Grid initialGrid, Grid currentGrid, Grid solutionGrid,
                       long elapsedSeconds, int mistakes, Status status) {
        this.sessionId = sessionId;
        this.playerName = playerName;
        this.difficulty = difficulty;
        this.initialGrid = initialGrid;
        this.currentGrid = currentGrid;
        this.solutionGrid = solutionGrid;
        this.undoStack = new ArrayDeque<>();
        this.redoStack = new ArrayDeque<>();
        this.elapsedSeconds = elapsedSeconds;
        this.mistakes = mistakes;
        this.maxMistakes = 3;
        this.status = status;
    }

    public boolean applyMove(int row, int col, int value) {
        if (status != Status.IN_PROGRESS) {
            return false;
        }
        if (initialGrid.isInitialClue(row, col)) {
            return false; // Cella fissa iniziale
        }

        int oldValue = currentGrid.get(row, col);
        if (oldValue == value) {
            return true;
        }

        // Se il valore inserito è diverso dalla soluzione attesa
        if (value != 0 && value != solutionGrid.get(row, col)) {
            mistakes++;
            if (mistakes >= maxMistakes) {
                status = Status.FAILED;
            }
        }

        currentGrid.set(row, col, value);
        Move move = new Move(row, col, oldValue, value);
        undoStack.push(move);
        redoStack.clear(); // Nuova azione resetta il redo

        if (currentGrid.isCompletelyValid() && currentGrid.toCompactString().equals(solutionGrid.toCompactString())) {
            status = Status.COMPLETED;
        }

        return true;
    }

    public boolean undo() {
        if (status != Status.IN_PROGRESS || undoStack.isEmpty()) {
            return false;
        }
        Move lastMove = undoStack.pop();
        currentGrid.set(lastMove.getRow(), lastMove.getCol(), lastMove.getOldValue());
        redoStack.push(lastMove);
        return true;
    }

    public boolean redo() {
        if (status != Status.IN_PROGRESS || redoStack.isEmpty()) {
            return false;
        }
        Move nextMove = redoStack.pop();
        currentGrid.set(nextMove.getRow(), nextMove.getCol(), nextMove.getNewValue());
        undoStack.push(nextMove);
        return true;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Grid getInitialGrid() {
        return initialGrid;
    }

    public Grid getCurrentGrid() {
        return currentGrid;
    }

    public Grid getSolutionGrid() {
        return solutionGrid;
    }

    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void addElapsedSeconds(long seconds) {
        this.elapsedSeconds += seconds;
    }

    public int getMistakes() {
        return mistakes;
    }

    public int getMaxMistakes() {
        return maxMistakes;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Move> getUndoHistory() {
        return new ArrayList<>(undoStack);
    }
}
