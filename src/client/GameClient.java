package client;

import javax.swing.*;
import jakarta.jms.*;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.ArrayList;

import common.Authenticator;
import common.ProfileManager;
import common.User;
import jms.*;
import ui.LoginUI;
import ui.RegisterUI;
import ui.GameUI;

public class GameClient {

    private Authenticator auth;
    private ProfileManager profile;

    private LoginUI loginUI;
    private RegisterUI registerUI;
    private GameUI gameUI;

    private User user;
    private List<User> topUsers;

    private GameManager gameManager;

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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        // login ui
        SwingUtilities.invokeLater(() -> {
            this.getLoginUI().setVisible(true);
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

    public List<User> getTopUsers() {
        return topUsers;
    }

    public void updateTopUsers() {
        try {
            this.topUsers = this.profile.getTopUsers();
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
            try {
                this.gameManager.initQueueMessageReceiver(this.user.getUsername());
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}