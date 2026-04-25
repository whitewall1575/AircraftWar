package edu.hitsz.application;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.strategy.CircleShootStrategy;
import edu.hitsz.strategy.ScatterShootStrategy;
import edu.hitsz.strategy.ShootStrategy;
import edu.hitsz.strategy.StraightShootStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirePowerControllerTest {

    @Test
    void shouldRestoreStraightStrategyAfterRecoveryTaskRuns() {
        TestAircraft aircraft = new TestAircraft();
        List<Runnable> recoveryTasks = new ArrayList<>();
        FirePowerController controller = new FirePowerController(aircraft, 3000, recoveryTasks::add, duration -> {
        });
        ShootStrategy scatterStrategy = new ScatterShootStrategy(3, 30, -1);

        controller.activate(scatterStrategy);
        assertSame(scatterStrategy, aircraft.getShootStrategy());

        recoveryTasks.get(0).run();
        assertTrue(aircraft.getShootStrategy() instanceof StraightShootStrategy);
    }

    @Test
    void shouldLetLatestFirePowerWinWhenMultipleTasksOverlap() {
        TestAircraft aircraft = new TestAircraft();
        List<Runnable> recoveryTasks = new ArrayList<>();
        FirePowerController controller = new FirePowerController(aircraft, 3000, recoveryTasks::add, duration -> {
        });
        ShootStrategy scatterStrategy = new ScatterShootStrategy(3, 30, -1);
        ShootStrategy circleStrategy = new CircleShootStrategy(12, 30, 5);

        controller.activate(scatterStrategy);
        controller.activate(circleStrategy);

        recoveryTasks.get(0).run();
        assertSame(circleStrategy, aircraft.getShootStrategy());

        recoveryTasks.get(1).run();
        assertTrue(aircraft.getShootStrategy() instanceof StraightShootStrategy);
    }

    private static class TestAircraft extends AbstractAircraft {
        TestAircraft() {
            super(0, 0, 0, 0, 100);
        }

        @Override
        public void forward() {
        }
    }
}
