package edu.hitsz.prop;

/**
 * 道具简单工厂类
 * 负责根据传入的类型，实例化对应的道具对象
 */
public class PropFactory {

    /**
     * @param propType 道具类型 (Blood, Fire, SuperFire, Bomb, Freeze)
     * @param x        掉落的 X 坐标 (通常是坠毁敌机的 X 坐标)
     * @param y        掉落的 Y 坐标 (通常是坠毁敌机的 Y 坐标)
     * @return 实例化后的具体道具对象
     */
    public static AbstractProp createProp(String propType, int x, int y) {
        // 道具默认以速度为 4 向下掉落，横向速度为 0
        int speedX = 0;
        int speedY = 4;

        switch (propType) {
            case "Blood":
                return new BloodProp(x, y, speedX, speedY);
            case "Fire":
                return new FireProp(x, y, speedX, speedY);
            case "SuperFire":
                return new SuperFireProp(x, y, speedX, speedY);
            case "Bomb":
                return new BombProp(x, y, speedX, speedY);
            case "Freeze":
                return new FreezeProp(x, y, speedX, speedY);
            default:
                throw new IllegalArgumentException("未知的道具类型: " + propType);
        }
    }
}
