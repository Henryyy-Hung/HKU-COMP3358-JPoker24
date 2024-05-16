package common;

import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    // User native attributes
    private String username;
    private String password;
    private boolean isOnline;

    // User statistics from other tables
    private int numOfGamesWon;
    private int numOfGamesPlayed;
    private double avgWinningTime;
    private int rank;

    // Constructor for Basic User (no info)
    public User(String username) {
        this.username = username;
    }

    // Constructor for Basic User
    public User(String username, String password, boolean isOnline) {
        this.username = username;
        this.password = password;
        this.isOnline = isOnline;
    }

    // Constructor for User with statistics
    public User(String username, String password, boolean isOnline, int numOfGamesWon, int numOfGamesPlayed, double avgWinningTime, int rank) {
        this.username = username;
        this.password = password;
        this.isOnline = isOnline;
        this.numOfGamesWon = numOfGamesWon;
        this.numOfGamesPlayed = numOfGamesPlayed;
        this.avgWinningTime = avgWinningTime;
        this.rank = rank;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public int getNumOfGamesWon() {
        return numOfGamesWon;
    }

    public int getNumOfGamesPlayed() {
        return numOfGamesPlayed;
    }

    public double getAvgWinningTime() {
        return avgWinningTime;
    }

    public int getRank() {
        return rank;
    }

    public void maskPassword() {
        this.password = "********";
    }
}