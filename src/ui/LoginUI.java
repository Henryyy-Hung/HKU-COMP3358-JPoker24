package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import javax.swing.SwingUtilities;

import common.Authenticator;
import enums.LoginStatus;
import client.GameClient;
import common.User;

public class LoginUI extends JFrame {
    private JTextField loginNameField;
    private JPasswordField passwordField;

    public LoginUI(GameClient gameClient) {
        setTitle("Login");
        setSize(320, 220);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main panel with padding
        JPanel mainPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mainPanel, BorderLayout.CENTER);

        mainPanel.add(new JLabel("Login Name:"));
        loginNameField = new JTextField();
        mainPanel.add(loginNameField);

        mainPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        mainPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener((ActionEvent e) -> {
            String username = loginNameField.getText();
            String password = new String(passwordField.getPassword());
            
            if (username.length() == 0) {
                JOptionPane.showMessageDialog(this, "Username is empty!");
                return;
            } else if (password.length() == 0) {
                JOptionPane.showMessageDialog(this, "Password is empty!");
                return;
            }
            new Thread(() -> {
                try {
                    LoginStatus status = gameClient.getAuthenticator().login(username, password);
                    switch (status) {
                        case SUCCESS:
                            JOptionPane.showMessageDialog(this, "Login successful!");
                            User user = gameClient.getProfileManager().getUser(username, password);
                            gameClient.setUser(user);
                            gameClient.updateTopUsers();
                            SwingUtilities.invokeLater(() -> {
                                gameClient.disposeLoginUI();
                                gameClient.getGameUI().setVisible(true);
                            });
                            break;
                        case WRONG_PASSWORD:
                            JOptionPane.showMessageDialog(this, "Wrong password!");
                            break;
                        case USER_NOT_FOUND:
                            JOptionPane.showMessageDialog(this, "User not found!");
                            break;
                        case ALREADY_LOGGED_IN:
                            JOptionPane.showMessageDialog(this, "User already logged in!");
                            break;
                        default:
                            JOptionPane.showMessageDialog(this, "Login failed!");
                            break;
                    }
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }).start();

        });
        mainPanel.add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener((ActionEvent e) -> {
            SwingUtilities.invokeLater(() -> {
                gameClient.disposeLoginUI();
                gameClient.getRegisterUI().setVisible(true);
            });
        });
        mainPanel.add(registerButton);

        setLocationRelativeTo(null);
    }
}