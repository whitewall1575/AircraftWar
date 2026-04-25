package edu.hitsz.application;

import javax.swing.*;
import java.awt.GridLayout;
import java.util.function.Consumer;

public class StartMenu {
    private JButton normalButton;
    private JButton hardButton;
    private JButton easyButton;
    private JPanel mainPanel;

    @SuppressWarnings("unused")
    public StartMenu() {
        this(difficulty -> {
        });
    }

    public StartMenu(Consumer<Difficulty> difficultySelectedListener) {
        createFallbackComponentsIfNeeded();
        easyButton.addActionListener(e -> difficultySelectedListener.accept(Difficulty.EASY));
        normalButton.addActionListener(e -> difficultySelectedListener.accept(Difficulty.NORMAL));
        hardButton.addActionListener(e -> difficultySelectedListener.accept(Difficulty.HARD));
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    @SuppressWarnings("BoundFieldAssignment")
    private void createFallbackComponentsIfNeeded() {
        if (mainPanel != null) {
            return;
        }
        mainPanel = new JPanel(new GridLayout(4, 1, 0, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(180, 120, 180, 120));
        mainPanel.add(new JLabel("Aircraft War", SwingConstants.CENTER));

        easyButton = new JButton("简单模式");
        normalButton = new JButton("普通模式");
        hardButton = new JButton("困难模式");
        mainPanel.add(easyButton);
        mainPanel.add(normalButton);
        mainPanel.add(hardButton);
    }
}
