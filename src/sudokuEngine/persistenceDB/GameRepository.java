package sudokuEngine.persistenceDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sudokuEngine.model.Difficulty;
import sudokuEngine.model.GameSession;
import sudokuEngine.model.Grid;
import sudokuEngine.model.Move;

/**
 * Data Access Object (DAO) per la persistenza delle entità Sudoku su database SQL.
 */
public class GameRepository {

    private final DatabaseManager dbManager;

    public record LeaderboardEntry(String playerName, String difficulty, int timeSeconds, int mistakes, String completedAt) {}
    public record SessionSummary(String sessionId, String playerName, String difficulty, String status, long elapsedSeconds, int mistakes, String updatedAt) {}

    public GameRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void saveSession(GameSession session) {
        String sql = """
            INSERT INTO game_sessions (
                session_id, player_name, difficulty, initial_grid, current_grid, solution_grid,
                elapsed_seconds, mistakes, status, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(session_id) DO UPDATE SET
                current_grid = excluded.current_grid,
                elapsed_seconds = excluded.elapsed_seconds,
                mistakes = excluded.mistakes,
                status = excluded.status,
                updated_at = CURRENT_TIMESTAMP;
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, session.getSessionId());
            ps.setString(2, session.getPlayerName());
            ps.setString(3, session.getDifficulty().name());
            ps.setString(4, session.getInitialGrid().toCompactString());
            ps.setString(5, session.getCurrentGrid().toCompactString());
            ps.setString(6, session.getSolutionGrid().toCompactString());
            ps.setLong(7, session.getElapsedSeconds());
            ps.setInt(8, session.getMistakes());
            ps.setString(9, session.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Impossibile salvare la sessione: " + e.getMessage());
        }
    }

    public GameSession loadSession(String sessionId) {
        String sql = "SELECT * FROM game_sessions WHERE session_id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String playerName = rs.getString("player_name");
                    Difficulty difficulty = Difficulty.valueOf(rs.getString("difficulty"));
                    Grid initial = Grid.fromCompactString(rs.getString("initial_grid"));
                    Grid current = Grid.fromCompactString(rs.getString("current_grid"));
                    Grid solution = Grid.fromCompactString(rs.getString("solution_grid"));
                    long elapsed = rs.getLong("elapsed_seconds");
                    int mistakes = rs.getInt("mistakes");
                    GameSession.Status status = GameSession.Status.valueOf(rs.getString("status"));

                    return new GameSession(sessionId, playerName, difficulty, initial, current, solution, elapsed, mistakes, status);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Impossibile caricare la sessione: " + e.getMessage());
        }
        return null;
    }

    public void logMove(String sessionId, int moveOrder, Move move) {
        String sql = """
            INSERT INTO moves (session_id, move_order, cell_row, cell_col, old_value, new_value)
            VALUES (?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ps.setInt(2, moveOrder);
            ps.setInt(3, move.getRow());
            ps.setInt(4, move.getCol());
            ps.setInt(5, move.getOldValue());
            ps.setInt(6, move.getNewValue());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Impossibile registrare la mossa: " + e.getMessage());
        }
    }

    public void addLeaderboardRecord(String playerName, Difficulty difficulty, int timeSeconds, int mistakes) {
        String sql = """
            INSERT INTO leaderboard (player_name, difficulty, time_seconds, mistakes)
            VALUES (?, ?, ?, ?);
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerName);
            ps.setString(2, difficulty.name());
            ps.setInt(3, timeSeconds);
            ps.setInt(4, mistakes);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Impossibile inserire nella classifica: " + e.getMessage());
        }
    }

    public List<LeaderboardEntry> getTopScores(Difficulty difficulty, int limit) {
        List<LeaderboardEntry> list = new ArrayList<>();
        String sql = """
            SELECT player_name, difficulty, time_seconds, mistakes, completed_at
            FROM leaderboard
            WHERE difficulty = ?
            ORDER BY time_seconds ASC, mistakes ASC
            LIMIT ?;
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, difficulty.name());
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new LeaderboardEntry(
                            rs.getString("player_name"),
                            rs.getString("difficulty"),
                            rs.getInt("time_seconds"),
                            rs.getInt("mistakes"),
                            rs.getString("completed_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Impossibile recuperare la classifica: " + e.getMessage());
        }
        return list;
    }

    public List<SessionSummary> listRecentSessions(int limit) {
        List<SessionSummary> list = new ArrayList<>();
        String sql = """
            SELECT session_id, player_name, difficulty, status, elapsed_seconds, mistakes, updated_at
            FROM game_sessions
            ORDER BY updated_at DESC
            LIMIT ?;
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new SessionSummary(
                            rs.getString("session_id"),
                            rs.getString("player_name"),
                            rs.getString("difficulty"),
                            rs.getString("status"),
                            rs.getLong("elapsed_seconds"),
                            rs.getInt("mistakes"),
                            rs.getString("updated_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Impossibile elencare le sessioni: " + e.getMessage());
        }
        return list;
    }
}
