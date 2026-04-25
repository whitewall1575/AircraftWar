package edu.hitsz.leaderboard;

import edu.hitsz.leaderboard.dao.FileScoreRecordDao;
import edu.hitsz.leaderboard.service.LeaderboardService;
import edu.hitsz.leaderboard.view.LeaderboardPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JTable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeaderboardPanelTest {

    private Path tempFile;
    private LeaderboardService service;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = Files.createTempFile("leaderboard-panel", ".txt");
        Files.deleteIfExists(tempFile);
        service = new LeaderboardService(new FileScoreRecordDao(tempFile.toString()));
    }

    @Test
    void shouldDisplaySortedRecordsInTable() {
        service.addRecord("P1", 300, LocalDateTime.of(2026, 4, 16, 9, 0));
        service.addRecord("P2", 900, LocalDateTime.of(2026, 4, 16, 9, 1));

        LeaderboardPanel panel = new LeaderboardPanel(service);
        JTable table = panel.getScoreTable();

        assertEquals(2, table.getRowCount());
        assertEquals(1, table.getValueAt(0, 0));
        assertEquals("P2", table.getValueAt(0, 1));
        assertEquals(900, table.getValueAt(0, 2));
    }

    @Test
    void shouldDeleteSelectedRecordAndRefreshTable() {
        service.addRecord("P1", 300, LocalDateTime.of(2026, 4, 16, 9, 0));
        service.addRecord("P2", 900, LocalDateTime.of(2026, 4, 16, 9, 1));
        LeaderboardPanel panel = new LeaderboardPanel(service);

        panel.getScoreTable().setRowSelectionInterval(0, 0);
        panel.deleteSelectedRecord();

        JTable table = panel.getScoreTable();
        assertEquals(1, table.getRowCount());
        assertEquals("P1", table.getValueAt(0, 1));
        assertEquals(1, service.getSortedRecords().size());
    }
}
