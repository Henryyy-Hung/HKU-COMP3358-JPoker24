package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;

import common.User;
import common.ProfileManager;
import handler.UserDatabaseHandler;

public class ProfileServer extends UnicastRemoteObject implements ProfileManager {
    private static final long serialVersionUID = 1L;
	private UserDatabaseHandler dbHandler;

    public ProfileServer() throws RemoteException {
        super();
        try {
            this.dbHandler = new UserDatabaseHandler();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    @Override
    public User getUser(String username, String password) {
        User user = dbHandler.getUserByNameWithGameInfo(username);
        if (user == null) {
            return null;
        }
        if (!user.getPassword().equals(password)) {
            return null;
        }
        return user;
    }

    @Override
    public List<User> getTopUsers() {
        List<User> topUsers = dbHandler.getTopNUsersWithGameInfo(10);
        for (User user : topUsers) {
            user.maskPassword();
        }
        return topUsers;
    }

}