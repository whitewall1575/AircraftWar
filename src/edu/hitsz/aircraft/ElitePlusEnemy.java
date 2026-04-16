package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.strategy.StraightShootStrategy;

public class ElitePlusEnemy extends AbstractEnemy{
      public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.score = 40;
        this.setShootStrategy(new StraightShootStrategy(2, 10, 1));
    }

    @Override
    public void forward() {
        super.forward(); // 继承父类的基础移动和 Y 轴越界判定

        if (locationX <= 0) {
            // 如果撞到左边界，强制让速度变成正数（向右飞）
            speedX = Math.abs(speedX);
        } else if (locationX >= Main.WINDOW_WIDTH) {
            // 如果撞到右边界，强制让速度变成负数（向左飞）
            speedX = -Math.abs(speedX);
        }
    }
}
