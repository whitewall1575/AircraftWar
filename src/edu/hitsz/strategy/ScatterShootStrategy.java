package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 散射弹道策略
 */
public class ScatterShootStrategy implements ShootStrategy {
    private final int shootNum;
    private final int power;
    private final int direction;

    public ScatterShootStrategy(int shootNum, int power, int direction) {
        this.shootNum = shootNum;
        this.power = power;
        this.direction = direction;
    }

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + direction * 2;
        int baseSpeedY = aircraft.getSpeedY() + direction * 4;

        for (int i = 0; i < shootNum; i++) {
            int center = (shootNum - 1) / 2;
            int bulletSpeedX = (i - center) * 3;
            int offsetX = (i * 2 - shootNum + 1) * 10;
            BaseBullet bullet;
            if (aircraft instanceof HeroAircraft) {
                bullet = new HeroBullet(x + offsetX, y, bulletSpeedX, baseSpeedY, power);
            } else {
                bullet = new EnemyBullet(x + offsetX, y, bulletSpeedX, baseSpeedY, power);
            }
            res.add(bullet);
        }
        return res;
    }
}

