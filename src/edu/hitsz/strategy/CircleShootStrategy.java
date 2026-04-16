package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 环射弹道策略
 */
public class CircleShootStrategy implements ShootStrategy {
    private final int shootNum;
    private final int power;
    private final int bulletSpeed;

    public CircleShootStrategy(int shootNum, int power, int bulletSpeed) {
        this.shootNum = shootNum;
        this.power = power;
        this.bulletSpeed = bulletSpeed;
    }

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY();

        for (int i = 0; i < shootNum; i++) {
            double angle = 2 * Math.PI * i / shootNum;
            int speedX = (int) Math.round(bulletSpeed * Math.cos(angle));
            int speedY = (int) Math.round(bulletSpeed * Math.sin(angle));
            BaseBullet bullet;
            if (aircraft instanceof HeroAircraft) {
                bullet = new HeroBullet(x, y, speedX, speedY, power);
            } else {
                bullet = new EnemyBullet(x, y, speedX, speedY, power);
            }
            res.add(bullet);
        }
        return res;
    }
}

