package edu.hitsz.leaderboard.view;

import edu.hitsz.leaderboard.service.LeaderboardService;

import javax.swing.JDialog;
import java.awt.Frame;

public class LeaderboardDialog extends JDialog {

    public LeaderboardDialog(Frame owner, LeaderboardService leaderboardService) {
        super(owner, "Leaderboard", true);
        setContentPane(new LeaderboardPanel(leaderboardService));
        setSize(420, 360);
        setLocationRelativeTo(owner);
    }
}
