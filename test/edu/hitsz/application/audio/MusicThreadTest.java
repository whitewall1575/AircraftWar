package edu.hitsz.application.audio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MusicThreadTest {

    @Test
    void shouldTreatInterruptedSleepAfterStopAsNormalShutdown() {
        assertFalse(MusicThread.shouldReportPlaybackFailure(
                new InterruptedException("sleep interrupted"),
                false
        ));
    }

    @Test
    void shouldReportUnexpectedPlaybackFailure() {
        assertTrue(MusicThread.shouldReportPlaybackFailure(
                new IllegalStateException("boom"),
                false
        ));
    }
}
