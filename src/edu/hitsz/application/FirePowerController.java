package edu.hitsz.application;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.strategy.ShootStrategy;
import edu.hitsz.strategy.StraightShootStrategy;

import java.util.concurrent.atomic.AtomicInteger;

public class FirePowerController {
    public static final long DEFAULT_DURATION_MILLIS = 8000;

    private final AbstractAircraft aircraft;
    private final long durationMillis;
    private final ThreadLauncher threadLauncher;
    private final Sleeper sleeper;
    private final AtomicInteger activationVersion = new AtomicInteger(0);

    public FirePowerController(AbstractAircraft aircraft) {
        this(aircraft, DEFAULT_DURATION_MILLIS,
                task -> {
                    Thread thread = new Thread(task, "fire-power-recovery");
                    thread.setDaemon(true);
                    thread.start();
                },
                Thread::sleep);
    }

    FirePowerController(AbstractAircraft aircraft, long durationMillis, ThreadLauncher threadLauncher, Sleeper sleeper) {
        this.aircraft = aircraft;
        this.durationMillis = durationMillis;
        this.threadLauncher = threadLauncher;
        this.sleeper = sleeper;
    }

    public void activate(ShootStrategy enhancedStrategy) {
        int currentVersion = activationVersion.incrementAndGet();
        aircraft.setShootStrategy(enhancedStrategy);
        threadLauncher.start(() -> recoverIfLatest(currentVersion));
    }

    private void recoverIfLatest(int version) {
        try {
            sleeper.sleep(durationMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        if (activationVersion.get() == version) {
            aircraft.setShootStrategy(new StraightShootStrategy(1, 30, -1));
        }
    }

    @FunctionalInterface
    interface ThreadLauncher {
        void start(Runnable task);
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }
}
