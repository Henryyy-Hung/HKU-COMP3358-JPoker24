package com.jpoker24.enums;

public enum GameMessageType {
    // For Game Session Communication
    // From Client
    JOIN_GAME,
    REDAY_FOR_GAME,
    SUBMIT_ANSWER,
    // From Server
    SESSION_READY,
    GAME_START,
    GAME_END,
    // For Common Communication
    UPDATE_LEADERBOARD,
}
