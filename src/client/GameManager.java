package client;

import jakarta.jms.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

import common.GameMessage;
import common.User;
import jms.JMSUtility;
import jms.GameMessageSender;
import jms.GameMessageReceiver;
import jms.MessageProcessor;
import enums.GameMessageType;

public class GameManager implements MessageProcessor {

    private GameClient gameClient;

    private ConnectionFactory connectionFactory;
    private Queue gameQueue;
    private Topic gameTopic;

    private GameMessageSender queueMessageSender;
    private GameMessageReceiver topicMessageReceiver;
    private GameMessageReceiver queueMessageReceiver;

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
            System.out.println("> Received message: " + gameMessage.getMessage() + " from " + gameMessage.getSenderId());
            // handle different message types using switch case
            switch (gameMessage.getType()) {
                case SESSION_READY: {
                    System.out.println("- This is a SESSION_READY message");
                    this.handleSessionReady(gameMessage);
                    break;
                }
                case GAME_START: {
                    System.out.println("- This is a GAME_START message");
                    this.handleGameStart(gameMessage);
                    break;
                }
                case GAME_END: {
                    System.out.println("- This is a GAME_END message");
                    this.handleGameEnd(gameMessage);
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
        this.sessionId = sessionId;
        // initialize the topic message receiver for the session
        try {
            this.initTopicMessageReceiver(sessionId);
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
        });
    }

    private void handleGameEnd(GameMessage gameMessage) {
        SwingUtilities.invokeLater(() -> {
            this.gameClient.getGameUI().proceedGamePanel_gameOver(gameMessage.getWinner().getUsername(), gameMessage.getSolution());
        });
        // Drop the topic message receiver for the terminated game session
        try {
            this.dropTopicMessageReceiver();
        }
        catch (JMSException e) {
            e.printStackTrace();
        }
        this.sessionId = null;
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
    }
}