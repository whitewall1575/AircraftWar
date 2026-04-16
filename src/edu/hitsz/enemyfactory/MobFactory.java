package edu.hitsz.enemyfactory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class MobFactory implements EnemyFactory {
    @Override
    public AbstractEnemy createEnemy() {
        // 随机 X 坐标，Y 坐标在屏幕最上方附近
        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth()));
        int locationY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
        int speedX = 0;
        int speedY = 10;
        int hp = 30;
        return new MobEnemy(locationX, locationY, speedX, speedY, hp);
    }
}