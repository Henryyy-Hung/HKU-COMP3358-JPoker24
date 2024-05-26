package com.jpoker24.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;

import com.jpoker24.common.User;
import com.jpoker24.common.Authenticator;
import com.jpoker24.enums.LoginStatus;
import com.jpoker24.enums.RegistrationStatus;
import com.jpoker24.handler.UserDatabaseHandler;

public class AuthenticationServer extends UnicastRemoteObject implements Authenticator {
    private static final long serialVersionUID = 1L;
	private UserDatabaseHandler dbHandler;

    public AuthenticationServer() throws RemoteException {
        super();
        try {
            this.dbHandler = new UserDatabaseHandler();
            this.dbHandler.setAllUsersOffline();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    @Override
    public LoginStatus login(String username, String password) {
        try {
            User user = dbHandler.getUserByName(username);
            if (user == null) {
                return LoginStatus.USER_NOT_FOUND;
            }
            if (!user.getPassword().equals(password)) {
                return LoginStatus.WRONG_PASSWORD;
            }
            if (user.isOnline()) {
                return LoginStatus.ALREADY_LOGGED_IN;
            }
            dbHandler.updateUserOnlineStatusByName(username, true);
            return LoginStatus.SUCCESS;
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            return LoginStatus.ERROR;
        }
    }

    @Override
    public boolean logout(String username) {
        try {
            dbHandler.updateUserOnlineStatusByName(username, false);
            return true;
        } catch (SQLException e) {
            System.err.println("Database error during logout: " + e.getMessage());
            return false;
        }
    }

    @Override
    public RegistrationStatus register(String username, String password) {
        try {
            if (dbHandler.getUserByName(username) != null) {
                return RegistrationStatus.USERNAME_EXISTS;
            }
            dbHandler.insertUser(username, password, true);      
            return RegistrationStatus.SUCCESS;
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            return RegistrationStatus.ERROR;
        }
    }
}