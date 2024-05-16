package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.rmi.RemoteException;
import java.io.InputStream;

import client.GameClient;
import common.User;
import utils.ExpressionEvaluator;
import utils.ExpressionValidator;

public class GameUI extends JFrame {

    private JButton logoutButton;
    private JTabbedPane tabbedPane;
    
    private GameClient gameClient;

    private final String IMAGE_PATH_TABLE = "/assets/images/background/table.jpg";
    private final String IMAGE_PATH_BOARD = "/assets/images/background/board.png";
    private final String IMAGE_PATH_BUTTON = "/assets/images/background/button.png";
    private final String IMAGE_PATH_BUTTON_HOVER = "/assets/images/background/button_hover.png";

    public GameUI(GameClient gameClient) {
        this.gameClient = gameClient;
        setTitle("JPoker 24-Game");
        setSize(600, 460);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        JPanel userProfilePanel = createUserProfilePanel();
        JPanel playGamePanel = createPlayGamePanel();
        JPanel leaderBoardPanel = createLeaderBoardPanel();

        Insets insets = new Insets(6, 4, 3, 4);
        Font font = new Font("Arial", Font.BOLD, 14);

        tabbedPane.addTab("User Profile", userProfilePanel);
        tabbedPane.setTabComponentAt(0, new JLabel("User Profile") {
            { 
                setFont(font);
                setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right)); 
            }
        });
        tabbedPane.addTab("Play Game", playGamePanel);
        tabbedPane.setTabComponentAt(1, new JLabel("Play Game") {
            { 
                setFont(font);
                setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right)); 
            }
        });
        tabbedPane.addTab("Leader Board", leaderBoardPanel);
        tabbedPane.setTabComponentAt(2, new JLabel("Leader Board") {
            { 
                setFont(font);
                setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right)); 
            }
        });

        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(this::logoutActionPerformed);
        add(logoutButton, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        ImageIO.setUseCache(false);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logoutAndClose();
            }
        });
    }

    public void proceedGamePanel_gamePlaying(List<User> players, int[] cards) {
        JPanel nextPanel = createGamePanel_gamePlaying(players, cards);
        tabbedPane.setComponentAt(tabbedPane.indexOfTab("Play Game"), nextPanel);
        nextPanel.revalidate();
        nextPanel.repaint();
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    public void proceedGamePanel_gameOver(String winner, String solution) {
        JPanel nextPanel = createGamePanel_gameOver(winner, solution);
        tabbedPane.setComponentAt(tabbedPane.indexOfTab("Play Game"), nextPanel);
        nextPanel.revalidate();
        nextPanel.repaint();
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    public void updateUserProfilePanel() {
        JPanel nextPanel = createUserProfilePanel();
        tabbedPane.setComponentAt(tabbedPane.indexOfTab("User Profile"), nextPanel);
        nextPanel.revalidate();
        nextPanel.repaint();
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    public void updateUserLeaderBoardPanel() {
        JPanel nextPanel = createLeaderBoardPanel();
        tabbedPane.setComponentAt(tabbedPane.indexOfTab("Leader Board"), nextPanel);
        nextPanel.revalidate();
        nextPanel.repaint();
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    private String getImagePathOfCard(String rank, String suit) {
        return "/assets/images/cards/" + rank + suit + ".gif";
    }

    // ensure the access of image after the jar is built
    private BufferedImage getImage(String path) {
        BufferedImage image = null;
        try {
            InputStream is = getClass().getResourceAsStream(path);
            image = ImageIO.read(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    private JPanel createUserProfilePanel() {
    	
    	JPanel rootPanel = new JPanel(new GridBagLayout()) {
            BufferedImage bgImage;
            {
                bgImage = getImage(IMAGE_PATH_TABLE);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this); // Stretch to fill entire panel
                }
            }
        };
        rootPanel.repaint();

        JPanel userInfoPanel =  new JPanel(new GridBagLayout()) {
            BufferedImage bgImage;
            {
                bgImage = getImage(IMAGE_PATH_BOARD);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this); // Stretch to fill entire panel
                }
            }
        };
        userInfoPanel.repaint();
        userInfoPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  
        gbc.anchor = GridBagConstraints.LINE_START;

        String username = "";
        String numOfGamesWon = "";
        String numOfGamesPlayed = "";
        String avgWinningTime = "";
        String rank = "";

        User player = gameClient.getUser();
        if (player != null) {
            username = player.getUsername() + "";
            numOfGamesWon = player.getNumOfGamesWon() + "";
            numOfGamesPlayed = player.getNumOfGamesPlayed() + "";
            avgWinningTime = String.format("%.2f", player.getAvgWinningTime()) + "s";
            rank = "#" + player.getRank();
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        userInfoPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        userInfoPanel.add(new JLabel(username), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        userInfoPanel.add(new JLabel("Number of wins:"), gbc);
        gbc.gridx = 1;
        userInfoPanel.add(new JLabel(numOfGamesWon + ""), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        userInfoPanel.add(new JLabel("Number of games:"), gbc);
        gbc.gridx = 1;
        userInfoPanel.add(new JLabel(numOfGamesPlayed + ""), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        userInfoPanel.add(new JLabel("Average time to win:"), gbc);
        gbc.gridx = 1;
        userInfoPanel.add(new JLabel(avgWinningTime), gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        userInfoPanel.add(new JLabel("Rank:"), gbc);
        gbc.gridx = 1;
        userInfoPanel.add(new JLabel(rank), gbc);

        for (Component comp : userInfoPanel.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(Color.WHITE);
            }
        }
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipadx = 50;
        gbc.ipady = 50;
        gbc.anchor = GridBagConstraints.CENTER;

        rootPanel.add(userInfoPanel, gbc);

        return rootPanel;
    }
    
    private JPanel createPlayGamePanel() {
        JPanel rootPanel = this.createGamePanel_initial();
        return rootPanel;
    }

    private JPanel createGamePanel_initial() {
        JPanel rootPanel = new JPanel(new GridBagLayout()) {
            BufferedImage bgImage;
            {
                bgImage = getImage(IMAGE_PATH_TABLE);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        rootPanel.repaint();
        rootPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("JPoker 24 Game");
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 48));
        welcomeLabel.setForeground(Color.WHITE);

        ImageIcon icon = null;
        ImageIcon hoverIcon = null;
     
        icon = new ImageIcon(getImage(IMAGE_PATH_BUTTON));
        hoverIcon = new ImageIcon(getImage(IMAGE_PATH_BUTTON_HOVER));

        JButton startGameButton =  new JButton("Start Game");
        startGameButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    SwingUtilities.invokeLater(() -> {
                        JPanel nextPanel = createGamePanel_gameJoining();
                        tabbedPane.setComponentAt(tabbedPane.indexOfTab("Play Game"), nextPanel);
                        tabbedPane.repaint();
                    });
                    this.gameClient.getGameManager().joinGameSession();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
        startGameButton.setIcon(icon);
        startGameButton.setRolloverIcon(hoverIcon);
        startGameButton.setBorderPainted(false);
        startGameButton.setFocusPainted(false);
        startGameButton.setContentAreaFilled(false);
        startGameButton.setHorizontalTextPosition(SwingConstants.CENTER);
        startGameButton.setVerticalTextPosition(SwingConstants.CENTER);
        startGameButton.setForeground(Color.WHITE);
        startGameButton.setFont(new Font("Arial", Font.BOLD, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.5; // Weight for welcome label area
        gbc.anchor = GridBagConstraints.CENTER;
        rootPanel.add(welcomeLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0.5; // Weight for button area
        gbc.anchor = GridBagConstraints.NORTH;
        rootPanel.add(startGameButton, gbc);

        return rootPanel;
    }

    private JPanel createGamePanel_gameJoining() {

        JPanel rootPanel = new JPanel(new GridBagLayout()) {
            BufferedImage bgImage;
            {
                bgImage = getImage(IMAGE_PATH_TABLE);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        rootPanel.repaint();
        rootPanel.setOpaque(false);

        JLabel noticeLabel = new JLabel("Joining Game...");
        noticeLabel.setHorizontalAlignment(JLabel.CENTER);
        noticeLabel.setFont(new Font("Arial", Font.BOLD, 32));
        noticeLabel.setForeground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        rootPanel.add(noticeLabel, gbc);

        return rootPanel;
    }

    private JPanel createGamePanel_gameOver(String winner, String solution) {
        JPanel rootPanel = new JPanel(new GridBagLayout()) {
            BufferedImage bgImage;
            {
                bgImage = getImage(IMAGE_PATH_TABLE);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        rootPanel.repaint();
        rootPanel.setOpaque(false);

        JLabel winnerLabel = new JLabel("Winner: " + winner);
        winnerLabel.setHorizontalAlignment(JLabel.CENTER);
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        winnerLabel.setForeground(Color.WHITE);

        JLabel solutionLabel = new JLabel(solution);
        solutionLabel.setHorizontalAlignment(JLabel.CENTER);
        solutionLabel.setFont(new Font("Arial", Font.BOLD, 24));
        solutionLabel.setForeground(Color.WHITE);

        ImageIcon icon = null;
        ImageIcon hoverIcon = null;
     
        icon = new ImageIcon(getImage(IMAGE_PATH_BUTTON));
        hoverIcon = new ImageIcon(getImage(IMAGE_PATH_BUTTON_HOVER));

        JButton nextGameButton =  new JButton("New Game");
        nextGameButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    SwingUtilities.invokeLater(() -> {
                        JPanel nextPanel = createGamePanel_gameJoining();
                        tabbedPane.setComponentAt(tabbedPane.indexOfTab("Play Game"), nextPanel);
                        tabbedPane.revalidate();
                        tabbedPane.repaint();
                    });
                    this.gameClient.getGameManager().joinGameSession();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
        nextGameButton.setIcon(icon);
        nextGameButton.setRolloverIcon(hoverIcon);
        nextGameButton.setBorderPainted(false);
        nextGameButton.setFocusPainted(false);
        nextGameButton.setContentAreaFilled(false);
        nextGameButton.setHorizontalTextPosition(SwingConstants.CENTER);
        nextGameButton.setVerticalTextPosition(SwingConstants.CENTER);
        nextGameButton.setForeground(Color.WHITE);
        nextGameButton.setFont(new Font("Arial", Font.BOLD, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipady = 10;
        gbc.weightx = 1;
        gbc.weighty = 0.25;

        gbc.anchor = GridBagConstraints.SOUTH;
        rootPanel.add(winnerLabel, gbc);
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        rootPanel.add(solutionLabel, gbc);
        gbc.gridy = 2;
        gbc.weighty = 0.5;
        rootPanel.add(nextGameButton, gbc);

        return rootPanel;
    }

    private JPanel createGamePanel_gamePlaying(List<User> players, int[] cards) {
    	
        JPanel rootPanel = new JPanel(new BorderLayout(20, 0)) {
            BufferedImage bgImage;
            {
                bgImage = getImage(IMAGE_PATH_TABLE);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this); // Stretch to fill entire panel
                }
            }
        };
        rootPanel.repaint();
        rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.setOpaque(false);

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setOpaque(false);

        GridBagConstraints cardPanelGbc = new GridBagConstraints();
        cardPanelGbc.gridx = 0;
        cardPanelGbc.gridy = 0;
        cardPanelGbc.insets = new Insets(10, 10, 10, 10);

        String[] suits = {"c", "d", "s", "h"};
        String[] ranks = {"a", "2", "3", "4", "5", "6", "7", "8", "9", "t", "j", "q", "k"};

        for (int i = 0; i < 4; i++) {
            int rank = (cards[i]-1) % 13; // suppose cards[i] is 1-indexed, from 1 to 13
            int suit = i;

            ImageIcon cardIcon = new ImageIcon(getImage(getImagePathOfCard(ranks[rank], suits[suit])));
            if (cardIcon == null) {
                cardIcon = ImageIcon.class.cast(null);
            }
            JLabel cardLabel = new JLabel(cardIcon);
            cardLabel.setOpaque(false);
            cardLabel.setBackground(Color.WHITE);
            cardLabel.revalidate();
            cardLabel.repaint();
            cardPanel.add(cardLabel, cardPanelGbc);
            cardPanelGbc.gridx++;
        }

        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setOpaque(false);
        
        infoPanel.setLayout(new GridBagLayout());
        GridBagConstraints infoPanelGbc = new GridBagConstraints();
        infoPanelGbc.gridx = 0;
        infoPanelGbc.gridy = 0;
        infoPanelGbc.ipadx = 40;
        infoPanelGbc.ipady = 25;
        infoPanelGbc.insets = new Insets(0, 0, 0, 0);
        infoPanelGbc.anchor = GridBagConstraints.NORTH;
        infoPanelGbc.fill = GridBagConstraints.BOTH;

        for (int i = 0; i < players.size(); i++) {
            JPanel playerPanel = new JPanel() {
                BufferedImage bgImage;
                {
                    bgImage = getImage(IMAGE_PATH_BOARD);
                }
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (bgImage != null) {
                        g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this); // Stretch to fill entire panel
                    }
                }
            };
            playerPanel.repaint();
            playerPanel.setOpaque(false);
            playerPanel.setLayout(new GridBagLayout());

            GridBagConstraints playerInfoGbc = new GridBagConstraints();
            playerInfoGbc.gridx = 0;
            playerInfoGbc.gridy = 0;
            playerInfoGbc.insets = new Insets(0, 0, 8, 0);

            User player = players.get(i);

            String name = player.getUsername();
            if (this.gameClient.getUser().getUsername().equals(name)) {
                name = "• " + name + " •";
            }

            String stat = "Win: " + player.getNumOfGamesWon() + "/" + player.getNumOfGamesPlayed() + " Avg: " + String.format("%.1f", player.getAvgWinningTime()) + "s";
            
            JLabel nameLabel = new JLabel(name);
            JLabel statsLabel = new JLabel(stat);

            playerPanel.add(nameLabel, playerInfoGbc);
            playerInfoGbc.gridy++;
            playerInfoGbc.insets = new Insets(0, 0, 0, 0);
            playerPanel.add(statsLabel, playerInfoGbc);
            
            for (Component comp : playerPanel.getComponents()) {
                if (comp instanceof JLabel) {
                    ((JLabel) comp).setForeground(Color.WHITE);
                }
            }
            
            infoPanel.add(playerPanel, infoPanelGbc);
            
            infoPanelGbc.gridy++;
            infoPanelGbc.insets = new Insets(20, 0, 0, 0);
        }
        
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.setBorder(BorderFactory.createLineBorder(new Color(229, 180, 74), 2));
        inputPanel.setBackground(new Color(57, 51, 45));
        inputPanel.setOpaque(true);

        JTextField inputField = new JTextField();
        inputField.setOpaque(false);
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        inputField.addActionListener((ActionEvent e) -> {
            new Thread(() -> {
                String inputText = inputField.getText();
                // inputField.setText(""); // Optionally clear the input field after processing
                ExpressionEvaluator evaluator = new ExpressionEvaluator();
                ExpressionValidator validator = new ExpressionValidator();
                if (! validator.allNumbersUsed(inputText, cards)) {
                    JOptionPane.showMessageDialog(GameUI.this, "Please use all numbers once");
                    return;
                }
                try {
                    Double result = evaluator.evaluate(inputText);
                    if (result != null && Math.abs(result - 24) < 1e-6) {
                        // JOptionPane.showMessageDialog(GameUI.this, "Correct!");
                        this.gameClient.getGameManager().submitGameAnswer(inputText);
                    } else {
                        JOptionPane.showMessageDialog(GameUI.this, "The result is " + (result == null ? "invalid" : result));
                    }
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(GameUI.this, ex.getMessage());
                }
            }).start();
        });

        inputPanel.add(inputField);
        
        gamePanel.add(cardPanel, BorderLayout.CENTER);
        gamePanel.add(inputPanel, BorderLayout.SOUTH);

        rootPanel.add(gamePanel, BorderLayout.CENTER);
        rootPanel.add(infoPanel, BorderLayout.EAST);

        return rootPanel;
    }
    
    private JPanel createLeaderBoardPanel() {

        String[] columnNames = {"Rank", "Player", "Games Won", "Games Played", "Avg Win Time"};
        List topUsers = gameClient.getTopUsers();
        Object[][] data = new Object[topUsers.size()][5];
        for (int i = 0; i < topUsers.size(); i++) {
            User user = (User) topUsers.get(i);
            data[i][0] = " " + user.getRank();
            data[i][1] = " " + user.getUsername();
            data[i][2] = " " + user.getNumOfGamesWon();
            data[i][3] = " " + user.getNumOfGamesPlayed();
            data[i][4] = " " + String.format("%.2f", user.getAvgWinningTime()) + "s";
        }

        JPanel rootPanel = new JPanel(new BorderLayout());

        JTable table = new JTable(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // This will make the table cells not editable
            }
        };
        Font font = new Font("Arial", Font.BOLD, 12);

        table.setRowHeight(26);
        table.setFont(font);
        JTableHeader header = table.getTableHeader();
        header.setFont(font);
        header.setPreferredSize(new Dimension(100, 26));
        table.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(table);
        rootPanel.add(scrollPane, BorderLayout.CENTER);
        return rootPanel;
    }
    
    private void logoutAndClose() {
        try {
            if (gameClient.getAuthenticator().logout(gameClient.getUser().getUsername())) {
                JOptionPane.showMessageDialog(this, "Logout successful.");
            } else {
                JOptionPane.showMessageDialog(this, "Logout failed.");
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error during logout: " + e.getMessage());
        }
        dispose();
        System.exit(0);
    }

    private void logoutActionPerformed(ActionEvent event) {
    	logoutAndClose();
    }
}