package edu.hitsz.prop;

import edu.hitsz.application.Main;
import edu.hitsz.basic.AbstractFlyingObject;

public abstract class AbstractProp extends AbstractFlyingObject {

    public AbstractProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    /**
     * 道具的通用移动逻辑：
     * 向屏幕下方移动，如果触碰到或者超出了界面底部，则标记为消失。
     */
    @Override
    public void forward() {
        super.forward();
        // 判定 Y 轴向下飞行是否出界
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }
}


