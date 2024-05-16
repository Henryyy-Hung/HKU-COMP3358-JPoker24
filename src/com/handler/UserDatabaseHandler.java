package com.handler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.common.User;

public class UserDatabaseHandler {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/GameDB";
    private static final String DB_USER = "gameUser";
    private static final String DB_PASS = "gamePassword";

    private Connection conn;

    public UserDatabaseHandler() throws SQLException {
        conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public void insertUser(String name, String password, boolean isOnline) throws SQLException {
        String sql = "INSERT INTO Users (name, password, is_online) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, password);
            stmt.setBoolean(3, isOnline);
            stmt.executeUpdate();
        }
    }

    public User getUserByName(String name) throws SQLException {
        String sql = "SELECT name, password, is_online FROM Users WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getBoolean("is_online")
                );
            }
            return null;
        }
    }

    public void updateUserOnlineStatusByName(String name, boolean isOnline) throws SQLException {
        String sql = "UPDATE Users SET is_online = ? WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isOnline);
            stmt.setString(2, name);
            stmt.executeUpdate();
        }
    }

    public void deleteUserByName(String name) throws SQLException {
        String sql = "DELETE FROM Users WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        }
    }
    
    public void setAllUsersOffline() throws SQLException {
        String sql = "UPDATE Users SET is_online = FALSE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rowsUpdated = stmt.executeUpdate();
        }
    }

    public User getUserByNameWithGameInfo(String username) {
        String sql = "SELECT u.name, u.password, u.is_online, " +
                    "COALESCE(t.gamesPlayed, 0) AS gamesPlayed, " +
                    "COALESCE(t.gamesWon, 0) AS gamesWon, " +
                    "COALESCE(t.avgWinTime, 0) AS avgWinTime, " +
                    "COALESCE(t.rank, -1) AS `rank` " +
                    "FROM Users u " +
                    "LEFT JOIN ( " +
                    "    SELECT p.user_name, " +
                    "    COUNT(p.game_id) AS gamesPlayed, " +
                    "    SUM(p.is_winner) AS gamesWon, " +
                    "    AVG(CASE WHEN p.is_winner = TRUE THEN g.completion_time ELSE NULL END) AS avgWinTime, " +
                    "    DENSE_RANK() OVER (ORDER BY SUM(p.is_winner) DESC) AS `rank` " +
                    "    FROM Participations p " +
                    "    LEFT JOIN Games g ON p.game_id = g.id " +
                    "    GROUP BY p.user_name " +
                    ") t ON u.name = t.user_name " +
                    "WHERE u.name = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int gamesPlayed = rs.getInt("gamesPlayed");
                int gamesWon = rs.getInt("gamesWon");
                double avgWinTime = rs.getDouble("avgWinTime");
                int rank = rs.getInt("rank");

                // handle the case if the user didn't have any participation records
                if (rank == -1) {
                    String rankSql = "SELECT COALESCE(MAX(`rank`), 0) AS maxRank " +
                                    "FROM ( " +
                                    "    SELECT DENSE_RANK() OVER (ORDER BY SUM(is_winner) DESC) AS `rank` " +
                                    "    FROM Participations GROUP BY user_name " + 
                                    ") sub";
                    try (PreparedStatement rankStmt = conn.prepareStatement(rankSql)) {
                        ResultSet rankRs = rankStmt.executeQuery();
                        if (rankRs.next()) {
                            rank = rankRs.getInt("maxRank") + 1;
                        }
                    }
                }

                User user = new User(
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getBoolean("is_online"),
                    gamesWon,
                    gamesPlayed,
                    avgWinTime,
                    rank
                );
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user with full info: " + e.getMessage());
        }
        return null;
    }

    public List<User> getTopNUsersWithGameInfo(int n) {
        String sql = "SELECT u.name, u.password, u.is_online, " +
                     "COALESCE(COUNT(p.game_id), 0) AS gamesPlayed, " +
                     "COALESCE(SUM(p.is_winner), 0) AS gamesWon, " +
                     "COALESCE(AVG(CASE WHEN p.is_winner = TRUE THEN g.completion_time ELSE NULL END), 0) AS avgWinTime " +
                     "FROM Users u " +
                     "LEFT JOIN Participations p ON u.name = p.user_name " +
                     "LEFT JOIN Games g ON p.game_id = g.id " +
                     "GROUP BY u.name " +
                     "ORDER BY SUM(p.is_winner) DESC, COALESCE(AVG(CASE WHEN p.is_winner = TRUE THEN g.completion_time ELSE NULL END), 0) ASC " +
                     "LIMIT ?";

        List<User> topUsers = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, n); // Set the limit to n users
            ResultSet rs = stmt.executeQuery();
            int rank = 1;  // Start ranking from 1
            while (rs.next()) {
                int gamesPlayed = rs.getInt("gamesPlayed");
                int gamesWon = rs.getInt("gamesWon");
                double avgWinTime = rs.getDouble("avgWinTime");

                // Increment rank if the number of games won is less than the previous user
                if (topUsers.size() > 0 && gamesWon < topUsers.get(topUsers.size() - 1).getNumOfGamesWon()) {
                    rank++;
                }
                User user = new User(
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getBoolean("is_online"),
                    gamesWon,
                    gamesPlayed,
                    avgWinTime,
                    rank
                );
                topUsers.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching top " + n + " users with full info: " + e.getMessage());
        }
        return topUsers;
    }
}