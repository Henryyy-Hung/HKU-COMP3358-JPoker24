package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import common.Authenticator;
import common.ProfileManager;

public class ClientMain {
    public static void main(String[] args) {
	    if (args.length < 1) {
	        System.err.println("Usage: java MessageBox <RMI registry host>");
	        System.exit(1);
	    }
	    String host = args[0];
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