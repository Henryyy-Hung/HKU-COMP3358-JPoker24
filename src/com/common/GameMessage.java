package com.common;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import com.enums.GameMessageType;

public class GameMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private GameMessageType type;   // the type of this message

    // Common informations
    private String senderId;        // the sender of this message
    private String receiverId;      // the receiver of this message (empty for broadcast)
    private String sessionId;       // the session id of current game (only for broadcast)
    private long creationTime;      // the time when this message was created

    // Debug informations
    private String message;         // a plain text message for debug (description of this message)

    // Stage Specific informations
    private User player;            // the player who wants to join the game
    private List<User> players;     // the players in the game
    private int[] cards;            // the cards for all players
    private String expression;      // the expression provided by a player
    private User winner;            // the winner of the game
    private String solution;        // the solution of the winner

    // Broadcast informations
    private List<User> topUsers;    // the top users in the game

    public GameMessage() {
        this.creationTime = System.currentTimeMillis();
    }

    public void setType(GameMessageType type) {
        this.type = type;
    }

    public GameMessageType getType() {
        return type;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setPlayer(User player) {
        this.player = player;
    }

    public User getPlayer() {
        return player;
    }

    public void setPlayers(List<User> players) {
        this.players = players;
    }

    public List<User> getPlayers() {
        return players;
    }

    public void setCards(int[] cards) {
        this.cards = cards;
    }

    public int[] getCards() {
        return cards;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    public User getWinner() {
        return winner;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getSolution() {
        return solution;
    }

    public void setTopUsers(List<User> topUsers) {
        this.topUsers = topUsers;
    }

    public List<User> getTopUsers() {
        return topUsers;
    }
}