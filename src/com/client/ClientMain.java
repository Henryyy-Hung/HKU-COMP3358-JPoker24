package com.client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.common.Authenticator;
import com.common.ProfileManager;

public class ClientMain {
    public static void main(String[] args) {
	    if (args.length < 1) {
	        System.out.println("Usage: java MessageBox <RMI registry host>");
            System.out.println("Using default host: localhost");
	    }
	    String host = (args.length < 1) ? "localhost" : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host, 1099);
            Authenticator auth = (Authenticator) registry.lookup("AuthService");
            ProfileManager profile = (ProfileManager) registry.lookup("ProfileService");
            new GameClient(auth, profile);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}