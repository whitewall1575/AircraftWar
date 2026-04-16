package edu.hitsz.enemyfactory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.EliteProEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class EliteProFactory implements EnemyFactory {
    @Override
    public AbstractEnemy createEnemy() {

        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_PRO_ENEMY_IMAGE.getWidth()));
        int locationY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);

        // 王牌敌机的横向速度更快
        int speedX = Math.random() < 0.5 ? 3 : -3;
        int speedY = 3;
        int hp = 120;
        return new EliteProEnemy(locationX, locationY, speedX, speedY, hp);
    }
}
