package sudokuEngine.model;

import java.time.Instant;

/**
 * Rappresenta una singola mossa eseguita durante la sessione di gioco.
 * Utilizzata dal Command Pattern per supportare operazioni di Undo/Redo e salvataggio su SQL.
 */
public class Move {
    private final int row;
    private final int col;
    private final int oldValue;
    private final int newValue;
    private final Instant timestamp;

    public Move(int row, int col, int oldValue, int newValue) {
        this.row = row;
        this.col = col;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = Instant.now();
    }

    public Move(int row, int col, int oldValue, int newValue, Instant timestamp) {
        this.row = row;
        this.col = col;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = timestamp;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getOldValue() {
        return oldValue;
    }

    public int getNewValue() {
        return newValue;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("Mossa: cella (%d, %d) cambiata da %d a %d [%s]",
                row + 1, col + 1, oldValue, newValue, timestamp);
    }
}
