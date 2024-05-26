package com.jpoker24.handler;

import java.sql.*;
import java.util.List;

import com.jpoker24.common.User;

public class GameDatabaseHandler {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/GameDB";
    private static final String DB_USER = "gameUser";
    private static final String DB_PASS = "gamePassword";

    private Connection conn;

    public GameDatabaseHandler() throws SQLException {
        conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public int createEmptyGame() throws SQLException {
        String sql = "INSERT INTO Games (completion_time) VALUES (null)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to create a new game record.");
    }

    public void appendUsersToGame(int gameId, List<User> users) throws SQLException {
        String sql = "INSERT INTO Participations (user_name, game_id, is_winner) VALUES (?, ?, FALSE)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);  // Start transaction
            for (User user : users) {
                stmt.setString(1, user.getUsername());
                stmt.setInt(2, gameId);
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);  // End transaction
        }
    }

    public void setWinnerAndCompletionTime(int gameId, User winner, double completionTime) throws SQLException {
        String sqlUpdateGame = "UPDATE Games SET completion_time = ? WHERE id = ?";
        String sqlUpdateParticipation = "UPDATE Participations SET is_winner = TRUE WHERE game_id = ? AND user_name = ?";

        try (PreparedStatement stmtGame = conn.prepareStatement(sqlUpdateGame);
             PreparedStatement stmtParticipation = conn.prepareStatement(sqlUpdateParticipation)) {

            conn.setAutoCommit(false);  // Start transaction
            
            // Update Games Table
            stmtGame.setDouble(1, completionTime);
            stmtGame.setInt(2, gameId);
            stmtGame.executeUpdate();

            // Update Participations Table
            stmtParticipation.setInt(1, gameId);
            stmtParticipation.setString(2, winner.getUsername());
            stmtParticipation.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);  // End transaction
        }
    }
}