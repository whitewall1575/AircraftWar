package edu.hitsz.leaderboard.view;

import edu.hitsz.leaderboard.model.ScoreRecord;
import edu.hitsz.leaderboard.service.LeaderboardService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardPanel extends JPanel {
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] COLUMN_NAMES = {"Rank", "Player", "Score", "Time"};

    private final LeaderboardService leaderboardService;
    private final DefaultTableModel tableModel;
    private final JTable scoreTable;
    private List<ScoreRecord> displayedRecords = new ArrayList<>();

    public LeaderboardPanel(LeaderboardService leaderboardService) {
        super(new BorderLayout(10, 10));
        this.leaderboardService = leaderboardService;
        this.tableModel = createTableModel();
        this.scoreTable = new JTable(tableModel);
        initializeView();
        refreshRecords();
    }

    public JTable getScoreTable() {
        return scoreTable;
    }

    public void deleteSelectedRecord() {
        int selectedRow = scoreTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        int modelRow = scoreTable.convertRowIndexToModel(selectedRow);
        ScoreRecord selectedRecord = displayedRecords.get(modelRow);
        leaderboardService.deleteRecord(selectedRecord);
        refreshRecords();
    }

    private void initializeView() {
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titleLabel = new JLabel("Leaderboard", SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        scoreTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scoreTable.setRowHeight(24);
        add(new JScrollPane(scoreTable), BorderLayout.CENTER);

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteSelectedRecord());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> closeWindow());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void refreshRecords() {
        displayedRecords = leaderboardService.getSortedRecords();
        tableModel.setRowCount(0);
        for (int i = 0; i < displayedRecords.size(); i++) {
            ScoreRecord record = displayedRecords.get(i);
            tableModel.addRow(new Object[]{
                    i + 1,
                    record.getPlayerName(),
                    record.getScore(),
                    record.getRecordTime().format(DISPLAY_FORMATTER)
            });
        }
    }

    private void closeWindow() {
        Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }
}
