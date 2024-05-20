package com.client;

import jakarta.jms.*;
import javax.swing.*;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.ArrayList;

import com.common.*;
import com.jms.*;
import com.ui.*;

public class GameClient {

    // Function with RMI Remote Services
    private Authenticator auth;
    private ProfileManager profile;

    // Game Manager with JMS Messaging Services
    private GameManager gameManager;

    // User Information & Top Users
    private User user;
    private List<User> topUsers;

    // UIs of the Game
    private LoginUI loginUI;
    private RegisterUI registerUI;
    private GameUI gameUI;

    public GameClient(Authenticator auth, ProfileManager profile) throws Exception {
        // authenticator
        this.auth = auth;
        // profile manager
        this.profile = profile;
        // top users
        this.topUsers = new ArrayList<>();
        // game manager
        new Thread(() -> {
            try {
                this.gameManager = new GameManager(this);
                System.out.println("Game Manager is running...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        // login ui
        SwingUtilities.invokeLater(() -> {
            this.getLoginUI().setVisible(true);
            System.out.println("Game UI is running...");
        });
        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                gameManager.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    public Authenticator getAuthenticator() {
        return auth;
    }

    public ProfileManager getProfileManager() {
        return profile;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    // initialize user with username and password
    public void initUser(String username, String password) {
        try {
            this.user = this.getProfileManager().getUser(username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // get user
    public User getUser() {
        return user;
    }

    // update user base on the user's username and password stored in the user object
    public void updateUser() {
        // only update user if user is not null (i.e. user has logged in)
        String username = this.user.getUsername();
        String password = this.user.getPassword();
        if (username == null || password == null) {
            return;
        }
        try {
            User user = this.getProfileManager().getUser(username, password);
            if (user != null) {
                this.user = user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // for debug only
    public void printUserInfo() {
        if (this.user == null) {
            System.out.println("User is null.");
            return;
        }
        System.out.println("");
        System.out.println("> User Information:");
        System.out.println("- Username:            " + this.user.getUsername());
        System.out.println("- Password:            " + this.user.getPassword());
        System.out.println("- Is Online:           " + this.user.isOnline());
        System.out.println("- Num of games won:    " + this.user.getNumOfGamesWon());
        System.out.println("- Num of games played: " + this.user.getNumOfGamesPlayed());
        System.out.println("- Avg win time:        " + this.user.getAvgWinningTime());
        System.out.println("- Rank:                " + this.user.getRank());
    }

    // set top users
    public void setTopUsers(List<User> topUsers) {
        this.topUsers = topUsers;
    }

    // get top users
    public List<User> getTopUsers() {
        return topUsers;
    }

    // update top users from the profile manager (server side)
    public void updateTopUsers() {
        try {
            List<User> topUsers = this.profile.getTopUsers();
            if (topUsers != null && topUsers.size() > 0) {
                this.topUsers = topUsers;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LoginUI getLoginUI() {
        if (this.loginUI == null) {
            this.loginUI = new LoginUI(this);
        }
        return loginUI;
    }

    public RegisterUI getRegisterUI() {
        if (this.registerUI == null) {
            this.registerUI = new RegisterUI(this);
        }
        return registerUI;
    }

    public GameUI getGameUI() {
        if (this.gameUI == null && this.user != null) {
            this.gameUI = new GameUI(this);
            new Thread(() -> {
                try {
                    this.gameManager.initQueueMessageReceiver(this.user.getUsername());
                } catch (Exception e) {
                    System.out.println("Failed to initialize queue message receiver.");
                    e.printStackTrace();
                }
                try {
                    this.gameManager.initBroadcastMessageReceiver();
                } catch (Exception e) {
                    System.out.println("Failed to initialize broadcast message receiver.");
                    e.printStackTrace();
                }
            }).start();
        }
        return gameUI;
    }

    public void disposeLoginUI() {
        if (this.loginUI != null) {
            this.loginUI.setVisible(false);
            this.loginUI.dispose();
            this.loginUI = null;
        }
    }

    public void disposeRegisterUI() {
        if (this.registerUI != null) {
            this.registerUI.setVisible(false);
            this.registerUI.dispose();
            this.registerUI = null;
        }
    }

    public void disposeGameUI() {
        if (this.gameUI != null) {
            this.gameUI.setVisible(false);
            this.gameUI.dispose();
            this.gameUI = null;
        }
    }
}