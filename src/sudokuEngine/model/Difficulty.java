package sudokuEngine.model;

/**
 * Livelli di difficoltà supportati dall'engine Sudoku.
 * Definiscono il numero di indizi iniziali (clues) visibili sulla griglia.
 */
public enum Difficulty {
    EASY("Facile", 38, 45),
    MEDIUM("Medio", 30, 36),
    HARD("Difficile", 25, 29),
    EXPERT("Esperto", 21, 24);

    private final String displayName;
    private final int minClues;
    private final int maxClues;

    Difficulty(String displayName, int minClues, int maxClues) {
        this.displayName = displayName;
        this.minClues = minClues;
        this.maxClues = maxClues;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinClues() {
        return minClues;
    }

    public int getMaxClues() {
        return maxClues;
    }

    public static Difficulty fromString(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Difficulty text null or blank");
        }

        for (Difficulty d : values()) {
            if (d.name().equalsIgnoreCase(text) || d.displayName.equalsIgnoreCase(text)) {
                return d;
            }
        }

        throw new IllegalArgumentException("Difficulty level not exists: '" + text + "'");
    }
}
