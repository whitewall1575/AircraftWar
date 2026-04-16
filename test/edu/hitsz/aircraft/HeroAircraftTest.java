package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeroAircraftTest {

    @Test
    void shouldReturnSameInstance() {
        HeroAircraft first = HeroAircraft.getInstance();
        HeroAircraft second = HeroAircraft.getInstance();
        assertSame(first, second);
    }

    @Test
    void shouldDecreaseHpAndNotBelowZero() {
        HeroAircraft hero = HeroAircraft.getInstance();
        hero.decreaseHp(Integer.MAX_VALUE);
        assertEquals(0, hero.getHp());
    }

    @Test
    void shouldShootBulletWithDefaultStrategy() {
        HeroAircraft hero = HeroAircraft.getInstance();
        List<BaseBullet> bullets = hero.shoot();
        assertFalse(bullets.isEmpty());
        assertEquals(1, bullets.size());
    }
}
