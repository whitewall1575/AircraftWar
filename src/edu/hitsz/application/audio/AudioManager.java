package edu.hitsz.application.audio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.LongSupplier;

public class AudioManager {
    public static final String BACKGROUND_MUSIC = "src/videos/bgm.wav";
    public static final String BOSS_MUSIC = "src/videos/bgm_boss.wav";
    public static final String BULLET_HIT_SOUND = "src/videos/bullet_hit.wav";
    public static final String BOMB_EXPLOSION_SOUND = "src/videos/bomb_explosion.wav";
    public static final String SUPPLY_SOUND = "src/videos/get_supply.wav";
    public static final String GAME_OVER_SOUND = "src/videos/game_over.wav";
    private static final long BULLET_HIT_INTERVAL_MILLIS = 160;
    private static final long SUPPLY_INTERVAL_MILLIS = 180;
    private static final long BOMB_EXPLOSION_INTERVAL_MILLIS = 250;
    private static final int MAX_ACTIVE_EFFECTS = 4;

    private final SoundFactory soundFactory;
    private final LongSupplier clock;
    private final Executor stopExecutor;
    private final List<ManagedSound> activeEffects = new ArrayList<>();
    private final Map<String, Long> lastEffectTimes = new HashMap<>();
    private ManagedSound backgroundMusic;
    private ManagedSound bossMusic;
    private ManagedSound pendingBossMusic;
    private boolean backgroundRequested;

    public AudioManager() {
        this((filename, loop, onStarted, onFinished) ->
                        new MusicThread(filename, loop, onStarted, onFinished),
                System::currentTimeMillis,
                AudioManager::stopOnDaemonThread);
    }

    AudioManager(SimpleSoundFactory soundFactory) {
        this(adaptSimpleFactory(soundFactory), System::currentTimeMillis, Runnable::run);
    }

    AudioManager(SimpleSoundFactory soundFactory, LongSupplier clock) {
        this(adaptSimpleFactory(soundFactory), clock, Runnable::run);
    }

    AudioManager(SoundFactory soundFactory) {
        this(soundFactory, System::currentTimeMillis, Runnable::run);
    }

    AudioManager(SoundFactory soundFactory, LongSupplier clock) {
        this(soundFactory, clock, Runnable::run);
    }

    AudioManager(SoundFactory soundFactory, LongSupplier clock, Executor stopExecutor) {
        this.soundFactory = soundFactory;
        this.clock = clock;
        this.stopExecutor = stopExecutor;
    }

    public synchronized void startBackgroundMusic() {
        backgroundRequested = true;
        if (bossMusic != null || pendingBossMusic != null) {
            return;
        }
        if (backgroundMusic == null) {
            startBackgroundMusicNow();
        }
    }

    public synchronized void stopBackgroundMusic() {
        backgroundRequested = false;
        stopBackgroundMusicNow();
    }

    public synchronized void startBossMusic() {
        if (bossMusic != null || pendingBossMusic != null) {
            return;
        }
        stopActiveEffectsNow();
        ManagedSound[] soundHolder = new ManagedSound[1];
        ManagedSound sound = soundFactory.create(
                BOSS_MUSIC,
                true,
                () -> handleBossMusicStarted(soundHolder[0]),
                () -> handleBossMusicFinished(soundHolder[0])
        );
        soundHolder[0] = sound;
        pendingBossMusic = sound;
        sound.start();
    }

    public synchronized void stopBossMusic() {
        if (bossMusic == null && pendingBossMusic == null) {
            return;
        }
        stopBossMusicNow();
        if (backgroundRequested && backgroundMusic == null) {
            startBackgroundMusicNow();
        }
    }

    public void playBulletHitSound() {
        playBulletHitThrottled();
    }

    public void playBombExplosionSound() {
        playThrottledEffect(BOMB_EXPLOSION_SOUND, BOMB_EXPLOSION_INTERVAL_MILLIS);
    }

    public void playSupplySound() {
        playThrottledEffect(SUPPLY_SOUND, SUPPLY_INTERVAL_MILLIS);
    }

    public void playGameOverSound() {
        playEffect(GAME_OVER_SOUND);
    }

    public synchronized void stopAll() {
        backgroundRequested = false;
        stopActiveEffectsNow();
        stopBackgroundMusicNow();
        stopBossMusicNow();
    }

    private void playThrottledEffect(String filename, long intervalMillis) {
        ManagedSound sound;
        long now = clock.getAsLong();
        synchronized (this) {
            Long lastTime = lastEffectTimes.get(filename);
            if (lastTime != null && now - lastTime < intervalMillis) {
                return;
            }
            if (activeEffects.size() >= MAX_ACTIVE_EFFECTS) {
                return;
            }
            lastEffectTimes.put(filename, now);
            ManagedSound[] soundHolder = new ManagedSound[1];
            sound = soundFactory.create(
                    filename,
                    false,
                    null,
                    () -> handleEffectFinished(soundHolder[0])
            );
            soundHolder[0] = sound;
            activeEffects.add(sound);
        }
        sound.start();
    }

    private void playBulletHitThrottled() {
        playThrottledEffect(BULLET_HIT_SOUND, BULLET_HIT_INTERVAL_MILLIS);
    }

    private void playEffect(String filename) {
        ManagedSound sound;
        synchronized (this) {
            ManagedSound[] soundHolder = new ManagedSound[1];
            sound = soundFactory.create(
                    filename,
                    false,
                    null,
                    () -> handleEffectFinished(soundHolder[0])
            );
            soundHolder[0] = sound;
            activeEffects.add(sound);
        }
        sound.start();
    }

    private void startBackgroundMusicNow() {
        ManagedSound[] soundHolder = new ManagedSound[1];
        ManagedSound sound = soundFactory.create(
                BACKGROUND_MUSIC,
                true,
                null,
                () -> handleBackgroundMusicFinished(soundHolder[0])
        );
        soundHolder[0] = sound;
        backgroundMusic = sound;
        sound.start();
    }

    private void stopBackgroundMusicNow() {
        ManagedSound sound = backgroundMusic;
        backgroundMusic = null;
        if (sound != null) {
            scheduleStop(sound);
        }
    }

    private void stopBossMusicNow() {
        ManagedSound pendingSound = pendingBossMusic;
        ManagedSound bossSound = bossMusic;
        pendingBossMusic = null;
        bossMusic = null;
        if (pendingSound != null) {
            scheduleStop(pendingSound);
        }
        if (bossSound != null && bossSound != pendingSound) {
            scheduleStop(bossSound);
        }
    }

    private void stopActiveEffectsNow() {
        List<ManagedSound> effects = new ArrayList<>(activeEffects);
        activeEffects.clear();
        for (ManagedSound effect : effects) {
            scheduleStop(effect);
        }
    }

    private void scheduleStop(ManagedSound sound) {
        stopExecutor.execute(sound::stopSound);
    }

    private synchronized void handleBossMusicStarted(ManagedSound sound) {
        if (sound == null || sound != pendingBossMusic) {
            return;
        }
        pendingBossMusic = null;
        bossMusic = sound;
        stopBackgroundMusicNow();
    }

    private synchronized void handleBossMusicFinished(ManagedSound sound) {
        if (sound == null) {
            return;
        }
        if (sound == pendingBossMusic) {
            pendingBossMusic = null;
            return;
        }
        if (sound != bossMusic) {
            return;
        }
        bossMusic = null;
        if (backgroundRequested && backgroundMusic == null) {
            startBackgroundMusicNow();
        }
    }

    private synchronized void handleBackgroundMusicFinished(ManagedSound sound) {
        if (sound != null && sound == backgroundMusic) {
            backgroundMusic = null;
        }
    }

    private synchronized void handleEffectFinished(ManagedSound sound) {
        if (sound != null) {
            activeEffects.remove(sound);
        }
    }

    private static SoundFactory adaptSimpleFactory(SimpleSoundFactory simpleSoundFactory) {
        return (filename, loop, onStarted, onFinished) -> {
            ManagedSound sound = simpleSoundFactory.create(filename, loop);
            return new ManagedSound() {
                private boolean finished;

                @Override
                public void start() {
                    sound.start();
                    if (onStarted != null) {
                        onStarted.run();
                    }
                }

                @Override
                public void stopSound() {
                    sound.stopSound();
                    notifyFinished();
                }

                private void notifyFinished() {
                    if (finished) {
                        return;
                    }
                    finished = true;
                    if (onFinished != null) {
                        onFinished.run();
                    }
                }
            };
        };
    }

    private static void stopOnDaemonThread(Runnable command) {
        Thread thread = new Thread(command, "audio-stop");
        thread.setDaemon(true);
        thread.start();
    }

    @FunctionalInterface
    interface SimpleSoundFactory {
        ManagedSound create(String filename, boolean loop);
    }

    @FunctionalInterface
    interface SoundFactory {
        ManagedSound create(String filename, boolean loop, Runnable onStarted, Runnable onFinished);
    }
}
