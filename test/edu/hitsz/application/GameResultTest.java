package edu.hitsz.application;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.audio.AudioManager;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BloodProp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameResultTest {

    @BeforeEach
    void setUp() throws Exception {
        resetHeroAircraft();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetHeroAircraft();
    }

    @Test
    void shouldFinishGameWhenEnemyBulletKillsHero() throws Exception {
        AtomicInteger gameOverRecords = new AtomicInteger();
        Game game = new Game(Difficulty.NORMAL, new NoOpAudioManager(), gameOverRecords::incrementAndGet);
        HeroAircraft heroAircraft = HeroAircraft.getInstance();

        addEnemyBullet(game, new EnemyBullet(
                heroAircraft.getLocationX(),
                heroAircraft.getLocationY(),
                0,
                0,
                heroAircraft.getHp()
        ));
        invokeCrashCheckAction(game);

        assertEquals(0, heroAircraft.getHp());
        assertEquals(1, gameOverRecords.get());
    }

    @Test
    void shouldFinishGameWhenResultCheckFindsHeroDead() throws Exception {
        AtomicInteger gameOverRecords = new AtomicInteger();
        Game game = new Game(Difficulty.NORMAL, new NoOpAudioManager(), gameOverRecords::incrementAndGet);
        HeroAircraft heroAircraft = HeroAircraft.getInstance();
        heroAircraft.decreaseHp(Integer.MAX_VALUE);

        invokeCheckResultAction(game);

        assertEquals(1, gameOverRecords.get());
    }

    @Test
    void shouldUseSwingTimerForGameLoop() throws Exception {
        Game game = new Game(Difficulty.NORMAL, new NoOpAudioManager(), () -> {
        });

        Field timerField = Game.class.getDeclaredField("timer");
        timerField.setAccessible(true);

        assertTrue(timerField.get(game) instanceof javax.swing.Timer);
    }

    @Test
    void shouldPlaySupplySoundOnlyWhenHeroCollectsProp() throws Exception {
        NoOpAudioManager audioManager = new NoOpAudioManager();
        Game game = new Game(Difficulty.NORMAL, audioManager, () -> {
        });
        HeroAircraft heroAircraft = HeroAircraft.getInstance();

        addProp(game, new BloodProp(0, 0, 0, 0));
        invokeCrashCheckAction(game);
        assertEquals(0, audioManager.supplySoundCount);

        addProp(game, new BloodProp(
                heroAircraft.getLocationX(),
                heroAircraft.getLocationY(),
                0,
                0
        ));
        invokeCrashCheckAction(game);

        assertEquals(1, audioManager.supplySoundCount);
    }

    @Test
    void shouldPlaySupplySoundForEveryCollectedProp() throws Exception {
        NoOpAudioManager audioManager = new NoOpAudioManager();
        Game game = new Game(Difficulty.NORMAL, audioManager, () -> {
        });
        HeroAircraft heroAircraft = HeroAircraft.getInstance();

        addProp(game, new BloodProp(
                heroAircraft.getLocationX(),
                heroAircraft.getLocationY(),
                0,
                0
        ));
        addProp(game, new BloodProp(
                heroAircraft.getLocationX(),
                heroAircraft.getLocationY(),
                0,
                0
        ));
        invokeCrashCheckAction(game);

        assertEquals(2, audioManager.supplySoundCount);
    }

    private static void resetHeroAircraft() throws Exception {
        Field instanceField = HeroAircraft.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @SuppressWarnings("unchecked")
    private static void addEnemyBullet(Game game, BaseBullet bullet) throws Exception {
        Field enemyBulletsField = Game.class.getDeclaredField("enemyBullets");
        enemyBulletsField.setAccessible(true);
        List<BaseBullet> enemyBullets = (List<BaseBullet>) enemyBulletsField.get(game);
        enemyBullets.add(bullet);
    }

    private static void invokeCrashCheckAction(Game game) throws Exception {
        Method crashCheckAction = Game.class.getDeclaredMethod("crashCheckAction");
        crashCheckAction.setAccessible(true);
        crashCheckAction.invoke(game);
    }

    private static void invokeCheckResultAction(Game game) throws Exception {
        Method checkResultAction = Game.class.getDeclaredMethod("checkResultAction");
        checkResultAction.setAccessible(true);
        checkResultAction.invoke(game);
    }

    @SuppressWarnings("unchecked")
    private static void addProp(Game game, AbstractProp prop) throws Exception {
        Field propsField = Game.class.getDeclaredField("props");
        propsField.setAccessible(true);
        List<AbstractProp> props = (List<AbstractProp>) propsField.get(game);
        props.add(prop);
    }

    private static class NoOpAudioManager extends AudioManager {
        private int supplySoundCount;

        @Override
        public void stopAll() {
        }

        @Override
        public void playGameOverSound() {
        }

        @Override
        public void playSupplySound() {
            supplySoundCount++;
        }
    }
}
