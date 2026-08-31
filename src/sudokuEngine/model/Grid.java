package sudokuEngine.model;

import java.util.Arrays;

/**
 * Rappresenta una griglia Sudoku 9x9 con metodi di validazione delle regole classiche
 * (riga, colonna, sottomatrice 3x3), utilità di copia e serializzazione a stringa.
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

    /**
     * Verifica se inserire 'value' nella cella (row, col) rispetta le regole del Sudoku.
     */
    public boolean isValidPlacement(int row, int col, int value) {
        if (value == 0) return true;

        // Controllo riga
        for (int c = 0; c < SIZE; c++) {
            if (c != col && cells[row][c] == value) {
                return false;
            }
        }

        // Controllo colonna
        for (int r = 0; r < SIZE; r++) {
            if (r != row && cells[r][col] == value) {
                return false;
            }
        }

        // Controllo sottomatrice 3x3
        int startRow = (row / SUBGRID_SIZE) * SUBGRID_SIZE;
        int startCol = (col / SUBGRID_SIZE) * SUBGRID_SIZE;
        for (int r = startRow; r < startRow + SUBGRID_SIZE; r++) {
            for (int c = startCol; c < startCol + SUBGRID_SIZE; c++) {
                if ((r != row || c != col) && cells[r][c] == value) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isFull() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (cells[r][c] == 0) return false;
            }
        }
        return true;
    }

    public boolean isCompletelyValid() {
        if (!isFull()) return false;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int val = cells[r][c];
                cells[r][c] = 0;
                boolean valid = isValidPlacement(r, c, val);
                cells[r][c] = val;
                if (!valid) return false;
            }
        }
        return true;
    }

    public int countClues() {
        int count = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (cells[r][c] != 0) count++;
            }
        }
        return count;
    }

    public Grid copy() {
        Grid clone = new Grid();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                clone.cells[r][c] = this.cells[r][c];
                clone.initialClues[r][c] = this.initialClues[r][c];
            }
        }
        return clone;
    }

    /**
     * Serializza la griglia in una stringa di 81 caratteri (cifre 0-9).
     */
    public String toCompactString() {
        StringBuilder sb = new StringBuilder(81);
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                sb.append(cells[r][c]);
            }
        }
        return sb.toString();
    }

    public static Grid fromCompactString(String data) {
        if (data == null || data.length() != 81) {
            throw new IllegalArgumentException("La stringa compatta deve contenere esattamente 81 caratteri");
        }
        Grid grid = new Grid();
        for (int i = 0; i < 81; i++) {
            char ch = data.charAt(i);
            int val = Character.isDigit(ch) ? Character.getNumericValue(ch) : 0;
            int r = i / SIZE;
            int c = i % SIZE;
            grid.set(r, c, val);
            if (val != 0) {
                grid.setInitialClue(r, c, true);
            }
        }
        return grid;
    }

    private void checkBounds(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            throw new IndexOutOfBoundsException("Coordinate cella non valide: (" + row + ", " + col + ")");
        }
    }

    public int[][] toArray() {
        int[][] copy = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            copy[r] = Arrays.copyOf(cells[r], SIZE);
        }
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("    1 2 3   4 5 6   7 8 9\n");
        sb.append("  ┏━━━━━━━┳━━━━━━━┳━━━━━━━┓\n");
        for (int r = 0; r < SIZE; r++) {
            sb.append((r + 1)).append(" ┃ ");
            for (int c = 0; c < SIZE; c++) {
                int val = cells[r][c];
                if (val == 0) {
                    sb.append("· ");
                } else {
                    sb.append(val).append(" ");
                }
                if ((c + 1) % 3 == 0) {
                    sb.append("┃ ");
                }
            }
            sb.append("\n");
            if ((r + 1) % 3 == 0 && r < SIZE - 1) {
                sb.append("  ┣━━━━━━━╋━━━━━━━╋━━━━━━━┫\n");
            }
        }
        sb.append("  ┗━━━━━━━┻━━━━━━━┻━━━━━━━┛\n");
        return sb.toString();
    }
}
