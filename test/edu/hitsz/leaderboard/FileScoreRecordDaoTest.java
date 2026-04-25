package edu.hitsz.leaderboard;

import edu.hitsz.leaderboard.dao.FileScoreRecordDao;
import edu.hitsz.leaderboard.model.ScoreRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileScoreRecordDaoTest {

    private Path tempFile;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = Files.createTempFile("leaderboard", ".txt");
        Files.deleteIfExists(tempFile);
    }

    @Test
    void shouldPersistAndLoadRecordsFromFile() {
        FileScoreRecordDao dao = new FileScoreRecordDao(tempFile.toString());
        ScoreRecord record = new ScoreRecord("PLAYER", 1200, LocalDateTime.of(2026, 4, 16, 9, 0));

        dao.insert(record);
        List<ScoreRecord> records = dao.getAll();

        assertEquals(1, records.size());
        assertEquals("PLAYER", records.get(0).getPlayerName());
        assertEquals(1200, records.get(0).getScore());
        assertEquals(LocalDateTime.of(2026, 4, 16, 9, 0), records.get(0).getRecordTime());
    }

    @Test
    void shouldDeleteMatchingRecordFromFile() {
        FileScoreRecordDao dao = new FileScoreRecordDao(tempFile.toString());
        ScoreRecord first = new ScoreRecord("P1", 300, LocalDateTime.of(2026, 4, 16, 9, 0));
        ScoreRecord second = new ScoreRecord("P2", 900, LocalDateTime.of(2026, 4, 16, 9, 1));

        dao.insert(first);
        dao.insert(second);
        dao.delete(first);

        List<ScoreRecord> records = dao.getAll();
        assertEquals(1, records.size());
        assertEquals("P2", records.get(0).getPlayerName());
    }
}
