package com.jms;

import jakarta.jms.*;
import java.io.Serializable;

public class GameMessageReceiver implements Runnable {
    private MessageConsumer consumer;
    private Session session;
    private Connection connection;
    private MessageProcessor processor;
    private volatile boolean running = true; // Flag to control the loop

    public GameMessageReceiver(ConnectionFactory factory, Destination destination, String selector, MessageProcessor processor) throws JMSException {
        this.processor = processor;
        connection = factory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        if (selector != null) {
            consumer = session.createConsumer(destination, selector);
        } else {
            consumer = session.createConsumer(destination);
        }
        connection.start();
    }

    @Override
    public void run() {
        try {
            // while connection is not closed
            while (running) {
                Message message = consumer.receive();
                if (message instanceof ObjectMessage) {
                    Serializable object = ((ObjectMessage) message).getObject();
                    processor.processMessage(object);
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void close() throws JMSException {
        running = false; // Update the flag when closing the connection
        if (consumer != null) {
            consumer.close();
        }
        if (session != null) {
            session.close();
        }
        if (connection != null) {
            connection.close();
        }
    }
}