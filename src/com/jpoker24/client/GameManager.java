package com.jpoker24.client;

import com.jpoker24.common.GameMessage;
import com.jpoker24.common.User;
import com.jpoker24.enums.GameMessageType;
import com.jpoker24.jms.GameMessageReceiver;
import com.jpoker24.jms.GameMessageSender;
import com.jpoker24.jms.JMSUtility;
import com.jpoker24.jms.MessageProcessor;

import jakarta.jms.*;
import java.io.Serializable;
import java.util.List;
import javax.swing.SwingUtilities;

public class GameManager implements MessageProcessor {

    private GameClient gameClient;

    private ConnectionFactory connectionFactory;
    private Queue gameQueue;
    private Topic gameTopic;

    private GameMessageSender queueMessageSender;           // send messages to the server for p2p communication
    private GameMessageReceiver queueMessageReceiver;       // receive messages from the server for p2p communication
    private GameMessageReceiver topicMessageReceiver;       // receive messages from the server for game session communication
    private GameMessageReceiver broadcastMessageReceiver;   // receive messages from the server for broadcast communication

    private String sessionId;

    public GameManager(GameClient gameClient) throws Exception {

        this.gameClient = gameClient;

        JMSUtility.setupJNDI();
        this.connectionFactory = JMSUtility.getConnectionFactory("jms/JPoker24GameConnectionFactory");
        this.gameQueue = JMSUtility.getQueue("jms/JPoker24GameQueue");
        this.gameTopic = JMSUtility.getTopic("jms/JPoker24GameTopic");

        this.queueMessageSender = new GameMessageSender(this.connectionFactory, this.gameQueue);
    }

    public void initQueueMessageReceiver(String receiverId) throws Exception {
        if (queueMessageReceiver != null) {
            queueMessageReceiver.close();
        }
        String selector = "ReceiverID = '" + receiverId + "'";
        this.queueMessageReceiver = new GameMessageReceiver(this.connectionFactory, this.gameQueue, selector, this);
        new Thread(queueMessageReceiver).start();
    }

    public void initTopicMessageReceiver(String sessionId) throws Exception {
        if (topicMessageReceiver != null) {
            topicMessageReceiver.close();
        }
        String selector = "SessionID = '" + sessionId + "'";
        this.topicMessageReceiver = new GameMessageReceiver(this.connectionFactory, this.gameTopic, selector, this);
        new Thread(topicMessageReceiver).start();
    }

    public void initBroadcastMessageReceiver() throws Exception {
        if (broadcastMessageReceiver != null) {
            broadcastMessageReceiver.close();
        }
        String selector = "ReceiverID = 'all'";         // username must with length > 4, thus safe to use 'all' as receiver id
        this.broadcastMessageReceiver = new GameMessageReceiver(this.connectionFactory, this.gameTopic, selector, this);
        new Thread(broadcastMessageReceiver).start();
    }

    public void dropTopicMessageReceiver() throws JMSException {
        if (topicMessageReceiver != null) {
            topicMessageReceiver.close();
            this.topicMessageReceiver = null;
        }
    }

    @Override
    public void processMessage(Serializable message) {
        if (message instanceof GameMessage) {
            // cast the message to GameMessage
            GameMessage gameMessage = (GameMessage) message;
            // print the received message
            System.out.println("\n> Message received from " + gameMessage.getSenderId());
            // handle different message types using switch case
            switch (gameMessage.getType()) {
                case SESSION_READY: {
                    System.out.println("- The " + gameMessage.getSenderId() + " accepted the game join request");
                    this.handleSessionReady(gameMessage);
                    break;
                }
                case GAME_START: {
                    System.out.println("- The " + gameMessage.getSenderId() + " started the game since all players ready");
                    this.handleGameStart(gameMessage);
                    break;
                }
                case GAME_END: {
                    System.out.println("- The " + gameMessage.getSenderId() + " ended the game since a player won");
                    this.handleGameEnd(gameMessage);
                    break;
                }
                case UPDATE_LEADERBOARD: {
                    System.out.println("- The " + gameMessage.getSenderId() + " sent the latest leaderboard");
                    this.handleUpdateLeaderboard(gameMessage);
                    break;
                }
                default:
                    break;
            }
        }
    }

    private void handleSessionReady(GameMessage gameMessage) {
        // obtain the session id
        String sessionId = gameMessage.getSessionId();
        System.out.println("- Game session id obtained: " + sessionId);
        this.sessionId = sessionId;
        // initialize the topic message receiver for the session
        try {
            this.initTopicMessageReceiver(sessionId);
            System.out.println("- Topic message receiver initialized for session: " + sessionId);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // acknowledge the server the session is received, and client is ready
        this.readyForGameSession(sessionId);
    }

    private void handleGameStart(GameMessage gameMessage) {
        SwingUtilities.invokeLater(() -> {
            this.gameClient.getGameUI().proceedGamePanel_gamePlaying(gameMessage.getPlayers(), gameMessage.getCards());
            System.out.println("- Game UI updated");
        });
    }

    private void handleGameEnd(GameMessage gameMessage) {
        // update ui
        SwingUtilities.invokeLater(() -> {
            this.gameClient.getGameUI().proceedGamePanel_gameOver(gameMessage.getWinner().getUsername(), gameMessage.getSolution());
            System.out.println("- Game UI updated");
        });
        // update information
        this.gameClient.updateUser();
        SwingUtilities.invokeLater(() -> {
            this.gameClient.getGameUI().updateUserProfilePanel();
            System.out.println("- User profile updated");
        });
        // Drop the topic message receiver for the terminated game session
        try {
            this.dropTopicMessageReceiver();
            System.out.println("- Topic message receiver dropped for session: " + this.sessionId);
        }
        catch (JMSException e) {
            e.printStackTrace();
        }
        this.sessionId = null;
    }

    private void handleUpdateLeaderboard(GameMessage gameMessage) {
        List<User> topUsers = gameMessage.getTopUsers();
        if (topUsers == null) {
            System.out.println("- Failed to update the leaderboard due to missing top users");
            return;
        }
        this.gameClient.setTopUsers(topUsers);
        SwingUtilities.invokeLater(() -> {
            this.gameClient.getGameUI().updateUserLeaderBoardPanel();
            System.out.println("- Leaderboard updated");
        });
    }

    private GameMessage initGameMessageToServer() {
        GameMessage request = new GameMessage();
        request.setSenderId(this.gameClient.getUser().getUsername());
        request.setReceiverId("server");
        request.setPlayer(this.gameClient.getUser());
        return request;
    }

    public void joinGameSession() {
        GameMessage request = this.initGameMessageToServer();
        request.setType(GameMessageType.JOIN_GAME);
        request.setMessage("Requesting game session");
        try {
            queueMessageSender.sendMessage(request);
            System.out.println("\n> Message sent to server");
            System.out.println("- Requesting game session");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readyForGameSession(String sessionId) {
        GameMessage request = this.initGameMessageToServer();
        request.setType(GameMessageType.REDAY_FOR_GAME);
        request.setSessionId(sessionId);
        request.setMessage("Ready for game");
        try {
            queueMessageSender.sendMessage(request);
            System.out.println("\n> Message sent to server");
            System.out.println("- Ready for game");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void submitGameAnswer(String answer) {
        GameMessage request = this.initGameMessageToServer();
        request.setType(GameMessageType.SUBMIT_ANSWER);
        request.setSessionId(this.sessionId);
        request.setMessage("Submitting answer: " + answer);
        request.setExpression(answer);
        try {
            queueMessageSender.sendMessage(request);
            System.out.println("\n> Message sent to server");
            System.out.println("- Submitting answer: " + answer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() throws JMSException {
        if (queueMessageSender != null) {
            queueMessageSender.close();
        }
        if (topicMessageReceiver != null) {
            topicMessageReceiver.close();
        }
        if (queueMessageReceiver != null) {
            queueMessageReceiver.close();
        }
        if (broadcastMessageReceiver != null) {
            broadcastMessageReceiver.close();
        }
    }
}