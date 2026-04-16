package edu.hitsz.aircraft;

import edu.hitsz.strategy.StraightShootStrategy;

public class EliteEnemy extends AbstractEnemy{
    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        score = 30;
        this.setShootStrategy(new StraightShootStrategy(1, 10, 1));
    }
}
