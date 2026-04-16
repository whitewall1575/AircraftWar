package edu.hitsz.aircraft;

import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.strategy.CircleShootStrategy;

public class BossEnemy extends AbstractEnemy{
    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.score = 200;
        this.setShootStrategy(new CircleShootStrategy(20, 15, 5));
    }

    @Override
    public void forward() {
        // Boss 悬浮在上方，仅左右移动
        locationX += speedX;
        if (locationX <= ImageManager.BOSS_ENEMY_IMAGE.getWidth() / 2) {
            locationX = ImageManager.BOSS_ENEMY_IMAGE.getWidth() / 2;
            speedX = Math.abs(speedX);
        } else if (locationX >= Main.WINDOW_WIDTH - ImageManager.BOSS_ENEMY_IMAGE.getWidth() / 2) {
            locationX = Main.WINDOW_WIDTH - ImageManager.BOSS_ENEMY_IMAGE.getWidth() / 2;
            speedX = -Math.abs(speedX);
        }
    }
}

