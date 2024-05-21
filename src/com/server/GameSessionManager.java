package com.server;

import jakarta.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.sql.*;

import com.common.User;
import com.jms.GameMessageSender;
import com.enums.GameSessionStatus;
import com.handler.GameDatabaseHandler;
import com.handler.UserDatabaseHandler;

public class GameSessionManager {

    GameDatabaseHandler gameDatabaseHandler;
    UserDatabaseHandler userDatabaseHandler;
    private Map<String, GameSession> sessions = new HashMap<>();
    private ConnectionFactory connectionFactory;
    private Topic gameTopic;

    public GameSessionManager(ConnectionFactory connectionFactory, Topic gameTopic) {
        this.connectionFactory = connectionFactory;
        this.gameTopic = gameTopic;
        try {
            this.gameDatabaseHandler = new GameDatabaseHandler();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
        try {
            this.userDatabaseHandler = new UserDatabaseHandler();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    public String createNewSession() throws Exception {
        // Create a new game in the database
        int gameId;
        try {
            gameId = gameDatabaseHandler.createEmptyGame();
        } catch (SQLException e) {
            throw new Exception("- Failed to create a new game entity in the database.");
        }
        // Create a new session ID
        String sessionId = java.util.UUID.randomUUID().toString();
        // Create a new message sender for the game
        GameMessageSender messageSender;
        try {
            messageSender = new GameMessageSender(connectionFactory, gameTopic);
        } catch (JMSException e) {
            throw new Exception("- Failed to create a new message sender for the game.");
        }
        // Create a new game session
        GameSession session = new GameSession(this, gameId, sessionId, messageSender);
        // Add the session to the session map
        sessions.put(sessionId, session);
        // Print out the session information
        System.out.println("- Number of existing sessions: " + sessions.size());
        String sessionList = "";
        for (String key : sessions.keySet()) {
            sessionList += key.substring(key.length() - 4) + ", ";
        }
        sessionList = sessionList.substring(0, sessionList.length() - 2);
        System.out.println("- Existing sessions: " + sessionList);
        return sessionId;
    }

    public GameSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        System.out.println("- Existing session removed: " + sessionId);
    }

    public GameDatabaseHandler getGameDatabaseHandler() {
        return gameDatabaseHandler;
    }

    public UserDatabaseHandler getUserDatabaseHandler() {
        return userDatabaseHandler;
    }

    public String addUserToAvailableSession(User user) {
        System.out.println("- Adding player " + user.getUsername() + " to an available session");
        // Find a session that has not started and has less than 4 players
        for (GameSession session : sessions.values()) {
            if (session.getStatus() == GameSessionStatus.WAITING_FOR_PLAYERS_TO_JOIN) {
                System.out.println("- Found an available session: " + session.getSessionId());
                session.addPlayer(user);
                return session.getSessionId();
            }
        }
        // If no available session is found, create a new session
        String sessionId = null;
        try {
            System.out.println("- No available session found, creating a new session");
            sessionId = this.createNewSession();
            GameSession session = getSession(sessionId);
            session.addPlayer(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessionId;
    }

    public void close() {
        for (GameSession session : sessions.values()) {
            session.close();
        }
    }
}