package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;

import java.util.List;

/**
 * 抽象弹道策略
 */
public interface ShootStrategy {
    List<BaseBullet> shoot(AbstractAircraft aircraft);
}

