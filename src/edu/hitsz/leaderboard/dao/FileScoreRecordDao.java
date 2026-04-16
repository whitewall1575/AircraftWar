package edu.hitsz.leaderboard.dao;

import edu.hitsz.leaderboard.model.ScoreRecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileScoreRecordDao implements ScoreRecordDao {

    private static final DateTimeFormatter STORAGE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final Path filePath;

    public FileScoreRecordDao() {
        this("data/leaderboard_normal.txt");
    }

    public FileScoreRecordDao(String filePath) {
        this.filePath = Paths.get(filePath);
    }

    @Override
    public List<ScoreRecord> getAll() {
        ensureFileExists();
        List<ScoreRecord> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                records.add(parseLine(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read leaderboard file: " + filePath, e);
        }

        return records;
    }

    @Override
    public void insert(ScoreRecord record) {
        ensureFileExists();
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND)) {
            writer.write(formatLine(record));
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write leaderboard file: " + filePath, e);
        }
    }

    private void ensureFileExists() {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize leaderboard file: " + filePath, e);
        }
    }

    private ScoreRecord parseLine(String line) {
        String[] parts = line.split(",", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid leaderboard line: " + line);
        }
        String playerName = parts[0];
        int score = Integer.parseInt(parts[1]);
        LocalDateTime time = LocalDateTime.parse(parts[2], STORAGE_FORMATTER);
        return new ScoreRecord(playerName, score, time);
    }

    private String formatLine(ScoreRecord record) {
        return record.getPlayerName() + "," + record.getScore() + "," + record.getRecordTime().format(STORAGE_FORMATTER);
    }
}
