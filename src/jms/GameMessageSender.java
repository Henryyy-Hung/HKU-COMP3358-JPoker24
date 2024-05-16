package jms;

import jakarta.jms.*;

import common.GameMessage;

public class GameMessageSender {
    private MessageProducer producer;
    private Session session;
    private Connection connection;

    public GameMessageSender(ConnectionFactory factory, Destination destination) throws JMSException {
        connection = factory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        producer = session.createProducer(destination);
        connection.start();
    }

    public void sendMessage(GameMessage message) throws JMSException {
        ObjectMessage objectMessage = session.createObjectMessage();
        objectMessage.setObject(message);
        if (message.getReceiverId() != null) {
            objectMessage.setStringProperty("ReceiverID", message.getReceiverId());
        }
        if (message.getSessionId() != null) {
            objectMessage.setStringProperty("SessionID", message.getSessionId());
        }
        producer.send(objectMessage);
    }

    public void close() throws JMSException {
        if (producer != null) {
            producer.close();
        }
        if (session != null) {
            session.close();
        }
        if (connection != null) {
            connection.close();
        }
    }
}