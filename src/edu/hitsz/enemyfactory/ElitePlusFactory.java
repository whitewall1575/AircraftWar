package edu.hitsz.enemyfactory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class ElitePlusFactory implements EnemyFactory {
    @Override
    public AbstractEnemy createEnemy() {

        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_PLUS_ENEMY_IMAGE.getWidth()));
        int locationY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);

        // 核心改动：精锐敌机会左右移动，这里随机决定它是往左(-2)还是往右(2)飞
        int speedX = Math.random() < 0.5 ? 2 : -2;
        int speedY = 4;
        int hp = 90;
        return new ElitePlusEnemy(locationX, locationY, speedX, speedY, hp);
    }
}
