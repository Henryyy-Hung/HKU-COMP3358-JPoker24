package com.jpoker24.jms;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import jakarta.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

public class JMSUtility {

    private static Context initialContext;

    public static void setupJNDI() throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");
        env.put(Context.URL_PKG_PREFIXES, "com.sun.enterprise.naming");
        env.put(Context.STATE_FACTORIES, "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
        env.put("org.omg.CORBA.ORBInitialHost", "localhost");
        env.put("org.omg.CORBA.ORBInitialPort", "3700");

        initialContext = new InitialContext(env);
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