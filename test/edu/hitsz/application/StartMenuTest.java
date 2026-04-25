package edu.hitsz.application;

import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StartMenuTest {

    @Test
    void shouldCreateMainPanelWithoutDesignerRuntime() {
        StartMenu startMenu = new StartMenu(difficulty -> {
        });

        assertNotNull(startMenu.getMainPanel());
    }

    @Test
    void shouldNotifySelectedDifficultyWhenButtonClicked() {
        AtomicReference<Difficulty> selectedDifficulty = new AtomicReference<>();
        StartMenu startMenu = new StartMenu(selectedDifficulty::set);

        JButton easyButton = findButton(startMenu, "简单模式");
        easyButton.doClick();

        assertEquals(Difficulty.EASY, selectedDifficulty.get());
    }

    private JButton findButton(StartMenu startMenu, String text) {
        for (java.awt.Component component : startMenu.getMainPanel().getComponents()) {
            if (component instanceof JButton && text.equals(((JButton) component).getText())) {
                return (JButton) component;
            }
        }
        throw new AssertionError("Button not found: " + text);
    }
}
