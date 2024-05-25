package com.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.security.SecurityManagerUtil;

public class ServerMain {
    
    public static void main(String[] args) {
        try {
            System.out.println("");

            // create RMI registry on default port 1099
        	int port = 1099;
            Registry registry = LocateRegistry.createRegistry(port);
            System.out.println("RMI registry created on port " + port + ".");

            // enforce all permissions
            SecurityManagerUtil.enforceAllPermissions();

            // create remote services and bind them to the registry
            AuthenticationServer auth = new AuthenticationServer(); 
            registry.rebind("AuthService", auth);
            System.out.println("Authentication Server is running...");

            // create profile service and bind it to the registry
            ProfileServer profile = new ProfileServer();
            registry.rebind("ProfileService", profile);
            System.out.println("Profile Server is running...");

            // create jms service
            GameServer gameServer = new GameServer();
            System.out.println("Game Server is running...");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    gameServer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}