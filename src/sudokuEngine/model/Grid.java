package sudokuEngine.model;

import java.util.Arrays;

/**
 * Rappresenta una griglia Sudoku 9x9 con matrice di interi e celle iniziali bloccate.
 */
public class Grid {
    public static final int SIZE = 9;
    public static final int SUBGRID_SIZE = 3;

    private final int[][] cells;
    private final boolean[][] initialClues;

    public Grid() {
        this.cells = new int[SIZE][SIZE];
        this.initialClues = new boolean[SIZE][SIZE];
    }

    public Grid(int[][] source) {
        this();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                this.cells[r][c] = source[r][c];
                if (source[r][c] != 0) {
                    this.initialClues[r][c] = true;
                }
            }
        }
    }

    public int get(int row, int col) {
        checkBounds(row, col);
        return cells[row][col];
    }

    public void set(int row, int col, int value) {
        checkBounds(row, col);
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Il valore deve essere compreso tra 0 e 9 (0 = vuoto)");
        }
        cells[row][col] = value;
    }

    public boolean isInitialClue(int row, int col) {
        checkBounds(row, col);
        return initialClues[row][col];
    }

    public void setInitialClue(int row, int col, boolean isClue) {
        checkBounds(row, col);
        this.initialClues[row][col] = isClue;
    }

    public void markCurrentAsClues() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                this.initialClues[r][c] = (this.cells[r][c] != 0);
            }
        }
    }

    public void clear() {
        for (int r = 0; r < SIZE; r++) {
            Arrays.fill(cells[r], 0);
            Arrays.fill(initialClues[r], false);
        }
    }

    public Grid copy() {
        Grid clone = new Grid();
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(this.cells[r], 0, clone.cells[r], 0, SIZE);
            System.arraycopy(this.initialClues[r], 0, clone.initialClues[r], 0, SIZE);
        }
        return clone;
    }

    private void checkBounds(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            throw new IndexOutOfBoundsException("Coordinate fuori griglia: (" + row + ", " + col + ")");
        }
    }
}
