package edu.hitsz.leaderboard;

import edu.hitsz.leaderboard.dao.FileScoreRecordDao;
import edu.hitsz.leaderboard.model.ScoreRecord;
import edu.hitsz.leaderboard.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeaderboardServiceTest {

    private Path tempFile;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = Files.createTempFile("leaderboard-service", ".txt");
        Files.deleteIfExists(tempFile);
    }

    @Test
    void shouldReturnRecordsSortedByScoreDescending() {
        FileScoreRecordDao dao = new FileScoreRecordDao(tempFile.toString());
        LeaderboardService service = new LeaderboardService(dao);

        dao.insert(new ScoreRecord("P1", 300, LocalDateTime.of(2026, 4, 16, 9, 0)));
        dao.insert(new ScoreRecord("P2", 1200, LocalDateTime.of(2026, 4, 16, 9, 1)));
        dao.insert(new ScoreRecord("P3", 800, LocalDateTime.of(2026, 4, 16, 9, 2)));

        List<ScoreRecord> sorted = service.getSortedRecords();

        assertEquals(3, sorted.size());
        assertEquals("P2", sorted.get(0).getPlayerName());
        assertEquals("P3", sorted.get(1).getPlayerName());
        assertEquals("P1", sorted.get(2).getPlayerName());
    }

    @Test
    void shouldPrintLeaderboardWithRankNameScoreAndTime() {
        FileScoreRecordDao dao = new FileScoreRecordDao(tempFile.toString());
        LeaderboardService service = new LeaderboardService(dao);
        service.addRecord("PLAYER", 600, LocalDateTime.of(2026, 4, 16, 10, 30));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(output));
        try {
            service.printLeaderboard();
        } finally {
            System.setOut(oldOut);
        }

        String printed = output.toString();
        assertTrue(printed.contains("=== Leaderboard (NORMAL) ==="));
        assertTrue(printed.contains("1"));
        assertTrue(printed.contains("PLAYER"));
        assertTrue(printed.contains("600"));
        assertTrue(printed.contains("2026-04-16 10:30:00"));
    }
}
