package enums;

public enum GameMessageType {
    // From Client
    JOIN_GAME,
    REDAY_FOR_GAME,
    SUBMIT_ANSWER,
    // From Server
    SESSION_READY,
    GAME_START,
    GAME_END,
}
