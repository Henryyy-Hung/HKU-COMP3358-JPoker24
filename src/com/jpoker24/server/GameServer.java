package com.jpoker24.server;

import com.jpoker24.common.GameMessage;
import com.jpoker24.jms.GameMessageReceiver;
import com.jpoker24.jms.GameMessageSender;
import com.jpoker24.jms.JMSUtility;
import com.jpoker24.jms.MessageProcessor;
import com.jpoker24.enums.GameMessageType;

import jakarta.jms.*;
import java.io.Serializable;

public class GameServer implements MessageProcessor {

    private GameSessionManager sessionManager;
    private ConnectionFactory connectionFactory;
    private Queue gameQueue;
    private Topic gameTopic;
    private GameMessageSender queueMessageSender;
    private GameMessageSender topicMessageSender;
    private GameMessageReceiver queueMessageReceiver;

    public GameServer() throws Exception {
        System.out.println("Initializing JMS Services...");
        // Setup JNDI and get connection factory, queue, and topic
        JMSUtility.setupJNDI();
        this.connectionFactory = JMSUtility.getConnectionFactory("jms/JPoker24GameConnectionFactory");
        this.gameQueue = JMSUtility.getQueue("jms/JPoker24GameQueue");
        this.gameTopic = JMSUtility.getTopic("jms/JPoker24GameTopic");
        System.out.println("JMS Services initialized.");
        // Create a session manager
        this.sessionManager = new GameSessionManager(this.connectionFactory, this.gameTopic);
        // Create Broadcast and Queue senders
        this.topicMessageSender = new GameMessageSender(this.connectionFactory, this.gameTopic);
        this.queueMessageSender = new GameMessageSender(this.connectionFactory, this.gameQueue);
        // Create a receiver selector
        String selector = "ReceiverID = 'server'";
        // Create a receiver
        this.queueMessageReceiver = new GameMessageReceiver(this.connectionFactory, this.gameQueue, selector, this);
        // Start listening for messages in a new thread
        new Thread(queueMessageReceiver).start();
    }

    @Override
    public void processMessage(Serializable message) {
        if (message instanceof GameMessage) {
            // cast the message to GameMessage
            GameMessage gameMessage = (GameMessage) message;
            System.out.println("\n> Message received from " + gameMessage.getSenderId());
            // handle different message types using switch case
            switch (gameMessage.getType()) {
                case JOIN_GAME:
                    System.out.println("- Player " + gameMessage.getSenderId() + " request to join the game");
                    this.handleJoinGame(gameMessage);
                    break;
                case REDAY_FOR_GAME:
                    System.out.println("- Player " + gameMessage.getSenderId() + " is ready for the game");
                    this.handleReadyForGame(gameMessage);
                    break;
                case SUBMIT_ANSWER:
                    System.out.println("- Player " + gameMessage.getSenderId() + " submitted an answer");
                    this.handleSubmitAnswer(gameMessage);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleJoinGame(GameMessage gameMessage) {
        // Validate the message
        if (gameMessage.getSenderId() == null) {
            System.out.println("- Invalid message: sender id is missing");
            return;
        }
        if (gameMessage.getPlayer() == null) {
            System.out.println("- Invalid message: player is missing");
            return;
        }
        // add the user to an available session and get the session id
        String sessionId = sessionManager.addUserToAvailableSession(gameMessage.getPlayer());
        System.out.println("- Game session assigned to player " + gameMessage.getSenderId() + ": " + sessionId);
        // prepare a response message
        GameMessage response = new GameMessage();
        response.setType(GameMessageType.SESSION_READY);
        response.setSenderId("server");
        response.setReceiverId(gameMessage.getSenderId());
        response.setSessionId(sessionId);
        response.setMessage("Session is ready");
        // send the response message to the client
        try {
            queueMessageSender.sendMessage(response);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void handleReadyForGame(GameMessage gameMessage) {
        // Validate the message
        if (gameMessage.getSessionId() == null) {
            System.out.println("- Invalid message: session id is missing");
            return;
        }
        if (gameMessage.getPlayer() == null) {
            System.out.println("- Invalid message: player is missing");
            return;
        }
        // Get the session and set the player as ready
        GameSession session = this.sessionManager.getSession(gameMessage.getSessionId());
        if (session == null) {
            System.out.println("- Session not found");
            return;
        }
        session.setReady(gameMessage.getPlayer());
    }

    private void handleSubmitAnswer(GameMessage gameMessage) {
        // Validate the message
        if (gameMessage.getSessionId() == null) {
            System.out.println("- Invalid message: session id is missing");
            return;
        }
        if (gameMessage.getPlayer() == null) {
            System.out.println("- Invalid message: player is missing");
            return;
        }
        if (gameMessage.getExpression() == null) {
            System.out.println("- Invalid message: expression is missing");
            return;
        }
        // Get the session and submit the answer
        GameSession session = this.sessionManager.getSession(gameMessage.getSessionId());
        if (session == null) {
            System.out.println("- Session not found");
            return;
        }
        session.submitGameAnswer(gameMessage.getPlayer(), gameMessage.getExpression(), System.currentTimeMillis());
    }

    public void close() throws JMSException {
        if (queueMessageSender != null) {
            queueMessageSender.close();
        }
        if (topicMessageSender != null) {
            topicMessageSender.close();
        }
        if (queueMessageReceiver != null) {
            queueMessageReceiver.close();
        }
        this.sessionManager.close();
    }
}