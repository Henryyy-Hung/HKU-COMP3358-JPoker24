package com.server;

import jakarta.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.sql.*;

import com.common.User;
import com.jms.GameMessageSender;
import com.enums.GameSessionStatus;
import com.handler.GameDatabaseHandler;

public class GameSessionManager {

    GameDatabaseHandler gameDatabaseHandler;
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
    }

    public String createNewSession() throws Exception {
        // Create a new game in the database
        int gameId;
        try {
            gameId = gameDatabaseHandler.createEmptyGame();
        } catch (SQLException e) {
            throw new Exception("Failed to create a new game in the database.");
        }
        // Create a new session ID
        String sessionId = java.util.UUID.randomUUID().toString();
        // Create a new message sender for the game
        GameMessageSender messageSender;
        try {
            messageSender = new GameMessageSender(connectionFactory, gameTopic);
        } catch (JMSException e) {
            throw new Exception("Failed to create a new message sender for the game.");
        }
        // Create a new game session
        GameSession session = new GameSession(this, gameId, sessionId, messageSender);
        // Add the session to the session map
        sessions.put(sessionId, session);
        return sessionId;
    }

    public GameSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public GameDatabaseHandler getGameDatabaseHandler() {
        return gameDatabaseHandler;
    }

    public String addUserToAvailableSession(User user) {
        // Find a session that has not started and has less than 4 players
        for (GameSession session : sessions.values()) {
            if (session.getStatus() == GameSessionStatus.WAITING_FOR_PLAYERS_TO_JOIN) {
                session.addPlayer(user);
                return session.getSessionId();
            }
        }
        // If no available session is found, create a new session
        String sessionId = null;
        try {
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