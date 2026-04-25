package edu.hitsz.application;

public enum Difficulty {
    EASY("src/images/bg.jpg"),
    NORMAL("src/images/bg2.jpg"),
    HARD("src/images/bg3.jpg");

    private final String backgroundImagePath;

    Difficulty(String backgroundImagePath) {
        this.backgroundImagePath = backgroundImagePath;
    }

    public String getBackgroundImagePath() {
        return backgroundImagePath;
    }
}
