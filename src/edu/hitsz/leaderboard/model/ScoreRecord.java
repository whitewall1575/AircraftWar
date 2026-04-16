package edu.hitsz.leaderboard.model;

import java.time.LocalDateTime;

public class ScoreRecord {
    private String playerName;
    private int score;
    private LocalDateTime recordTime;

    public ScoreRecord(String playerName, int score, LocalDateTime recordTime) {
        this.playerName = playerName;
        this.score = score;
        this.recordTime = recordTime;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public LocalDateTime getRecordTime() {
        return recordTime;
    }
}
