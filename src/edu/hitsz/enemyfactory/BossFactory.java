package edu.hitsz.enemyfactory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class BossFactory implements EnemyFactory {
    @Override
    public AbstractEnemy createEnemy() {
// Boss 通常从屏幕正上方正中央出场
        int locationX = Main.WINDOW_WIDTH / 2;
        int locationY = ImageManager.BOSS_ENEMY_IMAGE.getHeight() / 2;
        // Boss 只有横向速度，纵向速度固定为 0
        int speedX = 4;
        int speedY = 0;
        int hp = 500;
        return new BossEnemy(locationX, locationY, speedX, speedY, hp);
    }
}
