package com.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.common.User;
import com.common.GameMessage;
import com.jms.GameMessageSender;
import com.enums.GameSessionStatus;
import com.enums.GameMessageType;
import com.handler.UserDatabaseHandler;
import com.utils.*;

public class GameSession implements Serializable {

    private GameSessionManager gameSessionManager;      // the game session manager
    private String sessionId;                           // the session id of the game session (for communication)
    private int gameId;                                 // the game id of the game session in the database (for record keeping)
    private GameSessionStatus status;                   // the status of the game session
    private int[] cards;                                // the cards for all players in the game
    private transient GameMessageSender messageSender;  // the broadcast message sender for the game
    private long creationTime;                          // the time when the game session was created
    private long gameStartTime;                         // the time when the game started
    private List<Player> players;                       // the players in the game

    // Inner class for player information
    public class Player {
        public User user;
        public boolean isReady;
        public boolean hasSubmitted;
        public String expression;
        public boolean isCorrect;
        public long submissionTime;

        public Player(User user) {
            this.user = user;
            this.isReady = false;
            this.hasSubmitted = false;
            this.expression = "";
            this.isCorrect = false;
            this.submissionTime = Long.MAX_VALUE;
        }
    }

    public GameSession(GameSessionManager gameSessionManager,int gameId, String sessionId, GameMessageSender messageSender) {
        // Initialize the game session
        this.gameSessionManager = gameSessionManager;
        this.gameId = gameId;
        this.sessionId = sessionId;
        this.status = GameSessionStatus.WAITING_FOR_PLAYERS_TO_JOIN;
        this.messageSender = messageSender;
        this.creationTime = System.currentTimeMillis();
        this.players = new ArrayList<>();

        // generate cards for the game
        Point24Generator generator = new Point24Generator();
        this.cards = generator.generateCards();
        Arrays.sort(this.cards);

        // Start the game after 10 seconds if there are more than 1 player
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (players.size() > 1 && status == GameSessionStatus.WAITING_FOR_PLAYERS_TO_JOIN) {
                    // Diable further player from joining
                    status = GameSessionStatus.WAITING_FOR_PLAYERS_TO_READY;
                    System.out.println("\n> 10 seconds elapsed for session: " + sessionId);
                    System.out.println("- Minimum number of players reached for session: " + sessionId);
                    System.out.println("- Session moved to the next stage: WAITING_FOR_PLAYERS_TO_READY");
                    // If all players are ready, start the game
                    if (allPlayersReady()) {
                        System.out.println("- All players are ready for session: " + sessionId);
                        startGame();
                    }
                    // If not all players are ready, wait for them to be ready, and trigger the game start later
                }
            }
        };
        timer.schedule(task, 10000);

        System.out.println("- New session created: " + sessionId);
        System.out.println("- Session initiate with status: WAITING_FOR_PLAYERS_TO_JOIN");
    }

    private boolean allPlayersReady() {
        for (Player player : players) {
            if (!player.isReady) {
                return false;
            }
        }
        return true;
    }

    private Player getPlayer(User user) {
        for (Player player : players) {
            if (player.user.getUsername().equals(user.getUsername())) {
                return player;
            }
        }
        return null;
    }

    public void addPlayer(User user) {
        if (getPlayer(user) != null) {
            return;
        }
        players.add(new Player(user));
        long secondsSinceCreation = (System.currentTimeMillis() - creationTime) / 1000;
        if (players.size() == 4 || (players.size() > 1 && secondsSinceCreation >= 10)) {
            this.status = GameSessionStatus.WAITING_FOR_PLAYERS_TO_READY;
            if (players.size() == 4) {
                System.out.println("- Maximum number of players reached for session: " + sessionId);
            }
            if (players.size() > 1 && secondsSinceCreation >= 10) {
                System.out.println("- 10 seconds elapsed for session: " + sessionId);
                System.out.println("- Minimum number of players reached for session: " + sessionId);
            }
            System.out.println("- Session moved to the next stage: WAITING_FOR_PLAYERS_TO_READY");
        }
    }

    public void setReady(User user) {
        Player player = getPlayer(user);
        if (player == null) {
            return;
        }
        player.isReady = true;
        if (this.status == GameSessionStatus.WAITING_FOR_PLAYERS_TO_READY && allPlayersReady()) {
            System.out.println("- All players are ready for session: " + sessionId);
            this.startGame();
        }
    }

    public void startGame() {
        System.out.println("- Initiating game start for session: " + sessionId);
        // Start the game if all players are ready
        if (this.status != GameSessionStatus.WAITING_FOR_PLAYERS_TO_READY) {
            System.out.println("- Invalid session status" + status);
            System.out.println("- Failed to start the game for session: " + sessionId);
            return;
        }
        if (!allPlayersReady()) {
            System.out.println("- Not all players are ready");
            System.out.println("- Failed to start the game for session: " + sessionId);
            return;
        }
        // add players to the database
        try {
            this.gameSessionManager.getGameDatabaseHandler().appendUsersToGame(gameId, this.getUsers());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        this.status = GameSessionStatus.GAME_STARTED;
        System.out.println("- Move to the next stage: GAME_STARTED");
        // prepare a response message
        GameMessage response = this.initGameMessageToPlayers();
        response.setType(GameMessageType.GAME_START);
        response.setMessage("Game started");
        // send the response message to all players
        try {
            messageSender.sendMessage(response);
            this.gameStartTime = System.currentTimeMillis();
            System.out.println("- Broadcasted game start message to players for session: " + sessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void submitGameAnswer(User user, String expression, long submissionTime) {

        // Update the player's status
        Player player = getPlayer(user);
        player.hasSubmitted = true;
        player.expression = expression;
        player.submissionTime = submissionTime;

        ExpressionValidator validator = new ExpressionValidator();
        boolean isValid = validator.allNumbersUsed(expression, this.cards);

        if (isValid) {
            try {
                ExpressionEvaluator evaluator = new ExpressionEvaluator();
                double result = evaluator.evaluate(expression);
                if (Math.abs(result - 24) < 1e-6) {
                    player.isCorrect = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (player.isCorrect) {
            System.out.println("- Player " + user.getUsername() + " submitted the correct answer: " + expression);
        } else {
            System.out.println("- Player " + user.getUsername() + " submitted an incorrect answer: " + expression);
        }

        Player winner = getWinner();
        if (winner != null) {
            endGame(winner);
        }
    }

    private Player getWinner() {
        // obtain sorted players base on submission time in ascending order
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((a, b) -> (int) (a.submissionTime - b.submissionTime));
        for (Player p : sortedPlayers) {
            if (p.hasSubmitted && p.isCorrect) {
                return p;
            }
        }
        return null;
    }

    private void endGame(Player winner) {

        // the time for winner to submit the correct answer (in seconds)
        double secondsUsed =  (winner.submissionTime - this.gameStartTime) / 1000.0;
        // record to the database
        try {
            this.gameSessionManager.getGameDatabaseHandler().setWinnerAndCompletionTime(gameId, winner.user, secondsUsed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("- Winner is " + winner.user.getUsername() + " with expression " + winner.expression);
        // acknowledge all players the game has ended
        GameMessage response = this.initGameMessageToPlayers();
        response.setType(GameMessageType.GAME_END);
        response.setMessage("Game ended");
        response.setWinner(winner.user);
        response.setSolution(winner.expression);
        try {
            messageSender.sendMessage(response);
            System.out.println("- Broadcasted game end message to players for session: " + sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // update the leaderboard of all online users
        broadcastUpdateLeaderboard();
        this.terminate();
    }

    private List<User> getUsers() {
        List<User> users = new ArrayList<>();
        for (Player player : this.players) {
            users.add(player.user);
        }
        return users;
    }

    private void broadcastUpdateLeaderboard() {

        List<User> topUsers = new ArrayList<>();
        try {
            // get top 10 users
            topUsers = this.gameSessionManager.getUserDatabaseHandler().getTopNUsersWithGameInfo(20);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("- Failed to broadcast update leaderboard message since failed to get top users");
            return;
        }
        GameMessage message = new GameMessage();
        message.setType(GameMessageType.UPDATE_LEADERBOARD);
        message.setSenderId("server");
        message.setReceiverId("all");
        message.setMessage("Update leaderboard");
        message.setTopUsers(topUsers);
        try {
            messageSender.sendMessage(message);
            System.out.println("- Broadcasted update leaderboard message to all online players");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private GameMessage initGameMessageToPlayers() {
        GameMessage message = new GameMessage();
        message.setSenderId("server");
        message.setSessionId(this.sessionId);
        List <User> users = getUsers();
        message.setPlayers(users);
        message.setCards(this.cards);
        return message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public GameSessionStatus getStatus() {
        return status;
    }

    public void terminate() {
        try {
            this.status = GameSessionStatus.GAME_ENDED;
            System.out.println("- Move to the next stage: GAME_ENDED");
            messageSender.close();
            this.messageSender = null;
            this.gameSessionManager.removeSession(this.sessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (messageSender != null) {
            try {
                messageSender.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}