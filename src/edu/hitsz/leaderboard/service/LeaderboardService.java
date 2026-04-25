package edu.hitsz.leaderboard.service;

import edu.hitsz.leaderboard.dao.ScoreRecordDao;
import edu.hitsz.leaderboard.model.ScoreRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeaderboardService {
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ScoreRecordDao scoreRecordDao;

    public LeaderboardService(ScoreRecordDao scoreRecordDao) {
        this.scoreRecordDao = scoreRecordDao;
    }

    public void addRecord(String playerName, int score, LocalDateTime time) {
        scoreRecordDao.insert(new ScoreRecord(playerName, score, time));
    }

    public void deleteRecord(ScoreRecord record) {
        scoreRecordDao.delete(record);
    }

    public List<ScoreRecord> getSortedRecords() {
        List<ScoreRecord> records = new ArrayList<>(scoreRecordDao.getAll());
        records.sort(Comparator.comparingInt(ScoreRecord::getScore).reversed()
                .thenComparing(ScoreRecord::getRecordTime));
        return records;
    }

    @SuppressWarnings("unused")
    public void printLeaderboard() {
        List<ScoreRecord> records = getSortedRecords();
        System.out.println("=== Leaderboard (NORMAL) ===");
        if (records.isEmpty()) {
            System.out.println("No records yet.");
            return;
        }

        System.out.printf("%-6s %-10s %-8s %-20s%n", "Rank", "Player", "Score", "Time");
        for (int i = 0; i < records.size(); i++) {
            ScoreRecord record = records.get(i);
            System.out.printf("%-6d %-10s %-8d %-20s%n",
                    i + 1,
                    record.getPlayerName(),
                    record.getScore(),
                    record.getRecordTime().format(DISPLAY_FORMATTER));
        }
    }
}
