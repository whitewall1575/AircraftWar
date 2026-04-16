package edu.hitsz.enemyfactory;

import edu.hitsz.aircraft.AbstractEnemy;

/**
 * 敌机工厂接口 (创建者 Creator)
 */
public interface EnemyFactory {
    /**
     * 创建敌机对象的工厂方法
     * @return 实例化的敌机对象
     */
    AbstractEnemy createEnemy();
}