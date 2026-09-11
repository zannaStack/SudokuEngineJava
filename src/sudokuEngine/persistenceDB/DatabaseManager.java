package sudokuEngine.persistenceDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestore centralizzato delle connessioni al database relazionale SQLite.
 * Esegue le migrazioni DDL per la creazione delle tabelle all'avvio.
 */
public class DatabaseManager {

    private final String dbUrl;

    public DatabaseManager() {
        this("jdbc:sqlite:sudoku.db");
    }

    public DatabaseManager(String dbUrl) {
        this.dbUrl = dbUrl;
        initDatabase();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[AVVISO SQL] Driver SQLite JDBC non trovato nel classpath. Il database locale funzionerà in modalità disattivata o fallback.");
            return;
        }

        String createPuzzlesTable = """
            CREATE TABLE IF NOT EXISTS puzzles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                puzzle_data TEXT NOT NULL,
                solution_data TEXT NOT NULL,
                difficulty TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String createSessionsTable = """
            CREATE TABLE IF NOT EXISTS game_sessions (
                session_id TEXT PRIMARY KEY,
                player_name TEXT NOT NULL,
                difficulty TEXT NOT NULL,
                initial_grid TEXT NOT NULL,
                current_grid TEXT NOT NULL,
                solution_grid TEXT NOT NULL,
                elapsed_seconds INTEGER NOT NULL,
                mistakes INTEGER NOT NULL,
                status TEXT NOT NULL,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String createMovesTable = """
            CREATE TABLE IF NOT EXISTS moves (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id TEXT NOT NULL,
                move_order INTEGER NOT NULL,
                cell_row INTEGER NOT NULL,
                cell_col INTEGER NOT NULL,
                old_value INTEGER NOT NULL,
                new_value INTEGER NOT NULL,
                move_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (session_id) REFERENCES game_sessions(session_id)
            );
        """;

        String createLeaderboardTable = """
            CREATE TABLE IF NOT EXISTS leaderboard (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_name TEXT NOT NULL,
                difficulty TEXT NOT NULL,
                time_seconds INTEGER NOT NULL,
                mistakes INTEGER NOT NULL,
                completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createPuzzlesTable);
            stmt.execute(createSessionsTable);
            stmt.execute(createMovesTable);
            stmt.execute(createLeaderboardTable);
        } catch (SQLException e) {
            System.err.println("[ERRORE SQL] Impossibile inizializzare le tabelle del database: " + e.getMessage());
        }
    }
}
