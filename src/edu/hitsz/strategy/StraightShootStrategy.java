package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 直射弹道策略
 */
public class StraightShootStrategy implements ShootStrategy {
    private final int shootNum;
    private final int power;
    private final int direction;

    public StraightShootStrategy(int shootNum, int power, int direction) {
        this.shootNum = shootNum;
        this.power = power;
        this.direction = direction;
    }

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + direction * 2;
        int speedX = 0;
        int speedY = aircraft.getSpeedY() + direction * 5;

        for (int i = 0; i < shootNum; i++) {
            int offsetX = (i * 2 - shootNum + 1) * 10;
            BaseBullet bullet;
            if (aircraft instanceof HeroAircraft) {
                bullet = new HeroBullet(x + offsetX, y, speedX, speedY, power);
            } else {
                bullet = new EnemyBullet(x + offsetX, y, speedX, speedY, power);
            }
            res.add(bullet);
        }
        return res;
    }
}

