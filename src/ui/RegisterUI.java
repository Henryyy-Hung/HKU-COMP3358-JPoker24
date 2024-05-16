package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import javax.swing.SwingUtilities;

import common.Authenticator;
import enums.RegistrationStatus;
import client.GameClient;
import common.User;

public class RegisterUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public RegisterUI(GameClient gameClient) {
        setTitle("Register");
        setSize(320, 280);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mainPanel, BorderLayout.CENTER);

        mainPanel.add(new JLabel("Login Name:"));
        usernameField = new JTextField();
        mainPanel.add(usernameField);

        mainPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        mainPanel.add(passwordField);

        mainPanel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        mainPanel.add(confirmPasswordField);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener((ActionEvent e) -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (username.length() == 0) {
                JOptionPane.showMessageDialog(this, "Invalid User Name!");
                return;
            } else if (password.length() < 4 || password.length() > 16) {
                JOptionPane.showMessageDialog(this, "Length of password should between 4 and 16!");
                return;
            } else if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!");
                return;
            }
            new Thread(() -> {
                try {
                    RegistrationStatus status = gameClient.getAuthenticator().register(username, password);
                    switch (status) {
                        case SUCCESS:
                            JOptionPane.showMessageDialog(this, "Registration successful!");
                            User user = gameClient.getProfileManager().getUser(username, password);
                            gameClient.setUser(user);
                            gameClient.updateTopUsers();
                            SwingUtilities.invokeLater(() -> {
                                gameClient.disposeRegisterUI();
                                gameClient.getGameUI().setVisible(true);
                            });
                            break;
                        case USERNAME_EXISTS:
                            JOptionPane.showMessageDialog(this, "Username already exists!");
                            break;
                        default:
                            JOptionPane.showMessageDialog(this, "Registration failed!");
                            break;
                    }
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }).start();

        });
        mainPanel.add(registerButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((ActionEvent e) -> {
            SwingUtilities.invokeLater(() -> {
                gameClient.disposeRegisterUI();
                gameClient.getLoginUI().setVisible(true);
            });
        });
        mainPanel.add(cancelButton);

        setLocationRelativeTo(null);
    }
}