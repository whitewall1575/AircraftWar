package edu.hitsz.application.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class MusicThread extends Thread implements ManagedSound {
    private final String filename;
    private final boolean loop;
    private final Runnable onStarted;
    private final Runnable onFinished;
    private volatile boolean running = true;
    private volatile Clip clip;

    public MusicThread(String filename, boolean loop) {
        this(filename, loop, null, null);
    }

    public MusicThread(String filename, boolean loop, Runnable onStarted, Runnable onFinished) {
        super("music-" + new File(filename).getName());
        this.filename = filename;
        this.loop = loop;
        this.onStarted = onStarted;
        this.onFinished = onFinished;
        setDaemon(true);
    }

    @Override
    public void run() {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filename))) {
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            if (!running) {
                return;
            }
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                notifyStarted();
                waitWhileLooping();
            } else {
                clip.start();
                notifyStarted();
                waitUntilOneShotFinishes();
            }
        } catch (Exception e) {
            if (shouldReportPlaybackFailure(e, running)) {
                System.err.println("Failed to play audio: " + filename);
                e.printStackTrace();
            }
        } finally {
            closeClip();
            notifyFinished();
        }
    }

    private void waitWhileLooping() throws InterruptedException {
        while (running && clip.isOpen()) {
            Thread.sleep(50);
        }
    }

    private void waitUntilOneShotFinishes() throws InterruptedException {
        long length = Math.max(clip.getMicrosecondLength(), 0);
        long timeoutNanos = System.nanoTime() + length * 1000L + 500_000_000L;
        while (running
                && clip.isOpen()
                && clip.getMicrosecondPosition() < length
                && System.nanoTime() < timeoutNanos) {
            Thread.sleep(10);
        }
    }

    @Override
    public void stopSound() {
        running = false;
        Clip currentClip = clip;
        if (currentClip != null) {
            currentClip.stop();
        }
        interrupt();
    }

    private void closeClip() {
        Clip currentClip = clip;
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            clip = null;
        }
    }

    private void notifyStarted() {
        if (onStarted != null) {
            onStarted.run();
        }
    }

    private void notifyFinished() {
        if (onFinished != null) {
            onFinished.run();
        }
    }

    static boolean shouldReportPlaybackFailure(Exception exception, boolean running) {
        return running || !(exception instanceof InterruptedException);
    }
}
