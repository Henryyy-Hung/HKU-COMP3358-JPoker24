package com.jms;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import jakarta.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JMSUtility {

    private static Context initialContext;

    public static void setupJNDI() throws NamingException {
		System.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
		System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
		// System.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
        initialContext = new InitialContext();
    }

    public static ConnectionFactory getConnectionFactory(String factoryName) throws NamingException {
        return (ConnectionFactory) initialContext.lookup(factoryName);
    }

    public static Queue getQueue(String queueName) throws NamingException {
        return (Queue) initialContext.lookup(queueName);
    }

    public static Topic getTopic(String topicName) throws NamingException {
        return (Topic) initialContext.lookup(topicName);
    }
}