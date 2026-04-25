package edu.hitsz.application.audio;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AudioManagerTest {

    @Test
    void shouldLoopAndStopBackgroundMusic() {
        List<FakeSound> sounds = new ArrayList<>();
        AudioManager audioManager = new AudioManager((filename, loop) -> {
            FakeSound sound = new FakeSound(filename, loop);
            sounds.add(sound);
            return sound;
        });

        audioManager.startBackgroundMusic();
        audioManager.stopBackgroundMusic();

        assertEquals(AudioManager.BACKGROUND_MUSIC, sounds.get(0).filename);
        assertTrue(sounds.get(0).loop);
        assertTrue(sounds.get(0).started);
        assertTrue(sounds.get(0).stopped);
    }

    @Test
    void shouldStartAndStopBossMusicIndependently() {
        List<FakeSound> sounds = new ArrayList<>();
        AudioManager audioManager = new AudioManager((filename, loop) -> {
            FakeSound sound = new FakeSound(filename, loop);
            sounds.add(sound);
            return sound;
        });

        audioManager.startBossMusic();
        audioManager.stopBossMusic();

        assertEquals(AudioManager.BOSS_MUSIC, sounds.get(0).filename);
        assertTrue(sounds.get(0).loop);
        assertTrue(sounds.get(0).started);
        assertTrue(sounds.get(0).stopped);
    }

    @Test
    void shouldLetBossMusicReplaceAndThenResumeBackgroundMusic() {
        List<FakeSound> sounds = new ArrayList<>();
        AudioManager audioManager = new AudioManager((filename, loop) -> {
            FakeSound sound = new FakeSound(filename, loop);
            sounds.add(sound);
            return sound;
        });

        audioManager.startBackgroundMusic();
        audioManager.startBossMusic();
        audioManager.stopBossMusic();

        assertEquals(AudioManager.BACKGROUND_MUSIC, sounds.get(0).filename);
        assertTrue(sounds.get(0).stopped);
        assertEquals(AudioManager.BOSS_MUSIC, sounds.get(1).filename);
        assertTrue(sounds.get(1).stopped);
        assertEquals(AudioManager.BACKGROUND_MUSIC, sounds.get(2).filename);
        assertTrue(sounds.get(2).started);
    }

    @Test
    void shouldKeepBackgroundMusicUntilBossMusicActuallyStarts() {
        List<FakeSound> sounds = new ArrayList<>();
        AudioManager audioManager = new AudioManager((filename, loop, onStarted, onFinished) -> {
            FakeSound sound = new FakeSound(filename, loop, false, onStarted, onFinished);
            sounds.add(sound);
            return sound;
        });

        audioManager.startBackgroundMusic();
        audioManager.startBossMusic();

        assertFalse(sounds.get(0).stopped);

        sounds.get(1).markStarted();

        assertTrue(sounds.get(0).stopped);
    }

    @Test
    void shouldNotResumeBackgroundMusicAfterStopAll() {
        List<FakeSound> sounds = new ArrayList<>();
        AudioManager audioManager = new AudioManager((filename, loop) -> {
            FakeSound sound = new FakeSound(filename, loop);
            sounds.add(sound);
            return sound;
        });

        audioManager.startBackgroundMusic();
        audioManager.startBossMusic();
        audioManager.stopAll();

        assertEquals(2, sounds.size());
        assertTrue(sounds.get(0).stopped);
        assertTrue(sounds.get(1).stopped);
    }

    @Test
    void shouldPlayOneShotEffects() {
        List<FakeSound> sounds = new ArrayList<>();
        AudioManager audioManager = new AudioManager((filename, loop) -> {
            FakeSound sound = new FakeSound(filename, loop);
            sounds.add(sound);
            return sound;
        });

        audioManager.playBulletHitSound();
        audioManager.playSupplySound();
        audioManager.playBombExplosionSound();
        audioManager.playGameOverSound();

        assertEquals(AudioManager.BULLET_HIT_SOUND, sounds.get(0).filename);
        assertEquals(AudioManager.SUPPLY_SOUND, sounds.get(1).filename);
        assertEquals(AudioManager.BOMB_EXPLOSION_SOUND, sounds.get(2).filename);
        assertEquals(AudioManager.GAME_OVER_SOUND, sounds.get(3).filename);
        for (FakeSound sound : sounds) {
            assertTrue(sound.started);
        }
    }

    @Test
    void shouldThrottleRepeatedBulletHitsAndSupplyEffectsWithoutQueueing() {
        List<FakeSound> sounds = new ArrayList<>();
        AtomicLong clock = new AtomicLong(1000);
        AudioManager audioManager = new AudioManager(
                (filename, loop) -> {
                    FakeSound sound = new FakeSound(filename, loop);
                    sounds.add(sound);
                    return sound;
                },
                clock::get
        );

        audioManager.playBulletHitSound();
        audioManager.playBulletHitSound();
        audioManager.playSupplySound();
        audioManager.playSupplySound();
        clock.addAndGet(200);
        audioManager.playBulletHitSound();
        audioManager.playSupplySound();

        assertEquals(AudioManager.BULLET_HIT_SOUND, sounds.get(0).filename);
        assertEquals(AudioManager.SUPPLY_SOUND, sounds.get(1).filename);
        assertEquals(AudioManager.BULLET_HIT_SOUND, sounds.get(2).filename);
        assertEquals(AudioManager.SUPPLY_SOUND, sounds.get(3).filename);
        assertEquals(4, sounds.size());
    }

    @Test
    void shouldStopActiveEffectsBeforeStartingBossMusic() {
        List<FakeSound> sounds = new ArrayList<>();
        AudioManager audioManager = new AudioManager((filename, loop) -> {
            FakeSound sound = new FakeSound(filename, loop);
            sounds.add(sound);
            return sound;
        });

        audioManager.playSupplySound();
        audioManager.startBossMusic();

        assertTrue(sounds.get(0).stopped);
    }

    @Test
    void shouldScheduleBackgroundStopOutsideBossStartCall() {
        List<FakeSound> sounds = new ArrayList<>();
        List<Runnable> stopTasks = new ArrayList<>();
        AudioManager audioManager = new AudioManager(
                (filename, loop, onStarted, onFinished) -> {
                    FakeSound sound = new FakeSound(filename, loop, true, onStarted, onFinished);
                    sounds.add(sound);
                    return sound;
                },
                System::currentTimeMillis,
                stopTasks::add
        );

        audioManager.startBackgroundMusic();
        audioManager.startBossMusic();

        assertEquals(1, stopTasks.size());
        assertFalse(sounds.get(0).stopped);

        stopTasks.get(0).run();

        assertTrue(sounds.get(0).stopped);
    }

    @Test
    void shouldScheduleBossStopOutsideBossDeathCall() {
        List<FakeSound> sounds = new ArrayList<>();
        List<Runnable> stopTasks = new ArrayList<>();
        AudioManager audioManager = new AudioManager(
                (filename, loop, onStarted, onFinished) -> {
                    FakeSound sound = new FakeSound(filename, loop, true, onStarted, onFinished);
                    sounds.add(sound);
                    return sound;
                },
                System::currentTimeMillis,
                stopTasks::add
        );

        audioManager.startBackgroundMusic();
        audioManager.startBossMusic();
        stopTasks.clear();
        audioManager.stopBossMusic();

        assertEquals(1, stopTasks.size());
        assertFalse(sounds.get(1).stopped);
        assertEquals(AudioManager.BACKGROUND_MUSIC, sounds.get(2).filename);

        stopTasks.get(0).run();

        assertTrue(sounds.get(1).stopped);
    }

    private static class FakeSound implements ManagedSound {
        private final String filename;
        private final boolean loop;
        private final boolean autoStart;
        private final Runnable onStarted;
        private final Runnable onFinished;
        private boolean started;
        private boolean stopped;

        private FakeSound(String filename, boolean loop) {
            this(filename, loop, true);
        }

        private FakeSound(String filename, boolean loop, boolean autoStart) {
            this(filename, loop, autoStart, null, null);
        }

        private FakeSound(String filename, boolean loop, boolean autoStart, Runnable onStarted, Runnable onFinished) {
            this.filename = filename;
            this.loop = loop;
            this.autoStart = autoStart;
            this.onStarted = onStarted;
            this.onFinished = onFinished;
        }

        @Override
        public void start() {
            started = true;
            if (autoStart) {
                markStarted();
            }
        }

        private void markStarted() {
            if (onStarted != null) {
                onStarted.run();
            }
        }

        @Override
        public void stopSound() {
            stopped = true;
            if (onFinished != null) {
                onFinished.run();
            }
        }
    }
}
