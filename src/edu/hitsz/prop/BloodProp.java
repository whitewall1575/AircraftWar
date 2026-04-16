package edu.hitsz.prop;

public class BloodProp extends AbstractProp {

    public BloodProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    private int healAmount = 30;
    // 提供获取加血数值的方法
    public int getHealAmount() {
        return healAmount;
    }
}
