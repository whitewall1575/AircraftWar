package edu.hitsz.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DifficultyTest {

    @Test
    void shouldExposeBackgroundImagePathForEachDifficulty() {
        assertEquals("src/images/bg.jpg", Difficulty.EASY.getBackgroundImagePath());
        assertEquals("src/images/bg2.jpg", Difficulty.NORMAL.getBackgroundImagePath());
        assertEquals("src/images/bg3.jpg", Difficulty.HARD.getBackgroundImagePath());
    }

    @Test
    void shouldLoadBackgroundImageForDifficulty() {
        assertNotNull(ImageManager.getBackgroundImage(Difficulty.EASY));
        assertNotNull(ImageManager.getBackgroundImage(Difficulty.NORMAL));
        assertNotNull(ImageManager.getBackgroundImage(Difficulty.HARD));
    }
}
