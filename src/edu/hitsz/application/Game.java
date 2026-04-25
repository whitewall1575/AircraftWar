package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.application.audio.AudioManager;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.enemyfactory.*;
import edu.hitsz.prop.*;
import edu.hitsz.strategy.CircleShootStrategy;
import edu.hitsz.strategy.ScatterShootStrategy;
import edu.hitsz.leaderboard.dao.FileScoreRecordDao;
import edu.hitsz.leaderboard.service.LeaderboardService;
import edu.hitsz.leaderboard.view.LeaderboardDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

/**
 * 游戏主面板，游戏启动
 * @author hitsz
 */
public class Game extends JPanel {

    private int backGroundTop = 0;
    private final BufferedImage backgroundImage;

    //调度器, 用于定时任务调度
    private final GameTimer timer;
    //时间间隔(ms)，控制刷新频率
    private final int timeInterval = 40;

    private final HeroAircraft heroAircraft;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<AbstractProp> props;
    private final EnemyFactory mobFactory = new MobFactory();
    private final EnemyFactory eliteFactory = new EliteFactory();
    private final EnemyFactory elitePlusFactory = new ElitePlusFactory();
    private final EnemyFactory eliteProFactory = new EliteProFactory();
    private final EnemyFactory bossFactory = new BossFactory();
    //屏幕中出现的敌机最大数量
    private final int enemyMaxNumber = 5;

    //敌机生成周期
    protected double enemySpawnCycle  =  20;
    private int enemySpawnCounter = 0;

    //英雄机和敌机射击周期
    protected double shootCycle = 20;
    private int shootCounter = 0;

    //当前玩家分数
    private int score = 0;

    // Boss 生成阈值：每达到一个阈值且场上无 Boss 时生成
    private int nextBossScoreThreshold = 500;

    //游戏结束标志
    private boolean gameOverFlag = false;
    private boolean gameOverHandled = false;

    private static final String DEFAULT_PLAYER_NAME = "PLAYER";
    private final LeaderboardService leaderboardService = new LeaderboardService(new FileScoreRecordDao());
    private boolean leaderboardRecorded = false;
    private final FirePowerController firePowerController;
    private final AudioManager audioManager;
    private final Runnable gameOverRecorder;

    public Game() {
        this(Difficulty.NORMAL);
    }

    public Game(Difficulty difficulty) {
        this(difficulty, new AudioManager());
    }

    Game(Difficulty difficulty, AudioManager audioManager) {
        this(difficulty, audioManager, null);
    }

    Game(Difficulty difficulty, AudioManager audioManager, Runnable gameOverRecorder) {
        this.backgroundImage = ImageManager.getBackgroundImage(difficulty);
        heroAircraft = HeroAircraft.getInstance();
        firePowerController = new FirePowerController(heroAircraft);
        this.audioManager = audioManager;
        this.gameOverRecorder = gameOverRecorder;

        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        props = new LinkedList<>();
        //启动英雄机鼠标监听
        new HeroController(this, heroAircraft);

        this.timer = new GameTimer(timeInterval);

    }

    /**
     * 游戏启动入口，执行游戏逻辑
     */
    public void action() {
        audioManager.startBackgroundMusic();

        // 定时任务：绘制、对象产生、碰撞判定、及结束判定
        timer.addActionListener(event -> {

                enemySpawnCounter++;
                if (enemySpawnCounter >= enemySpawnCycle) {
                    enemySpawnCounter = 0;
                    if (enemyAircrafts.size() < enemyMaxNumber) {

                        // 声明一个抽象的工厂引用
                        EnemyFactory currentFactory;
                        double rand = Math.random();

                        // 按照概率分配工厂 (假设：普通 50%, 精英 25%, 精锐 15%, 王牌 10%)
                        if (rand < 0.50) {
                            currentFactory = mobFactory;
                        } else if (rand < 0.75) {
                            currentFactory = eliteFactory;
                        } else if (rand < 0.90) {
                            currentFactory = elitePlusFactory;
                        } else {
                            currentFactory = eliteProFactory;
                        }

                        // 【核心精髓】：利用多态，统一调用抽象的 createEnemy() 方法！
                        // Game 类根本不需要知道它具体 new 了哪种敌机，也不用管坐标和血量怎么算
                        AbstractEnemy newEnemy = currentFactory.createEnemy();
                        enemyAircrafts.add(newEnemy);
                    }
                }
                spawnBossIfNeeded();

                // 飞机发射子弹
                shootAction();
                // 子弹移动
                bulletsMoveAction();
                // 飞机移动
                aircraftsMoveAction();
                // 道具移动
                propsMoveAction();
                // 撞击检测
                crashCheckAction();
                // 后处理
                postProcessAction();
                // 重绘界面
                repaint();
                // 游戏结束检查
                checkResultAction();
        });
        // 以固定延迟时间进行执行：本次任务执行完成后，延迟 timeInterval 再执行下一次
        timer.start();

    }

    //***********************
    //      Action 各部分
    //***********************

    private void shootAction() {
        shootCounter++;
        if (shootCounter >= shootCycle) {
            shootCounter = 0;
            //英雄机射击
            heroBullets.addAll(heroAircraft.shoot());
            // 敌机射击：遍历所有敌机。精英敌机会返回子弹，普通敌机会返回空列表。
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                enemyBullets.addAll(enemyAircraft.shoot());
            }
        }
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }


    private void propsMoveAction() {
        for (AbstractProp prop : props) {
            prop.forward();
        }
    }

    /**
     * 碰撞检测：
     * 1. 敌机攻击英雄
     * 2. 英雄攻击/撞击敌机
     * 3. 英雄获得补给
     */
    private void crashCheckAction() {
        // 1. 敌机子弹攻击英雄机
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) continue;
            if (heroAircraft.crash(bullet)) {
                damageHero(bullet.getPower());
                bullet.vanish();
                if (gameOverFlag) {
                    return;
                }
            }
        }

        // 英雄子弹攻击敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    // 已被其他子弹击毁的敌机，不再检测
                    // 避免多个子弹重复击毁同一敌机的判定
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    // 敌机撞击到英雄机子弹
                    // 敌机损失一定生命值
                    enemyAircraft.decreaseHp(bullet.getPower());
                    audioManager.playBulletHitSound();
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        handleEnemyDestroyed(enemyAircraft);
                    }
                    // 一颗子弹只能命中一个目标
                    break;
                }
            }
        }

        // 英雄机与敌机相撞，均损毁
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft.notValid()) {
                continue;
            }
            if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                if (enemyAircraft instanceof BossEnemy) {
                    audioManager.stopBossMusic();
                }
                enemyAircraft.vanish();
                damageHero(Integer.MAX_VALUE);
                if (gameOverFlag) {
                    return;
                }
            }
        }

        // 3. 我方获得道具，道具生效
        for (AbstractProp prop : props) {
            if (prop.notValid()) continue;
            if (heroAircraft.crash(prop)) {
                applyPropEffect(prop);
                prop.vanish(); // 道具生效后消失
            }
        }
    }

    private void spawnBossIfNeeded() {
        if (score < nextBossScoreThreshold || hasBossInScene()) {
            return;
        }
        enemyAircrafts.add(bossFactory.createEnemy());
        audioManager.startBossMusic();
        nextBossScoreThreshold += 500;
    }

    private boolean hasBossInScene() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft instanceof BossEnemy && !enemyAircraft.notValid()) {
                return true;
            }
        }
        return false;
    }

    private void handleEnemyDestroyed(AbstractAircraft enemyAircraft) {
        score += ((AbstractEnemy) enemyAircraft).getScore();

        int x = enemyAircraft.getLocationX();
        int y = enemyAircraft.getLocationY();
        double rand = Math.random();

        if (enemyAircraft instanceof BossEnemy) {
            audioManager.stopBossMusic();
            for (int i = 0; i < 3; i++) {
                props.add(createRandomProp(x, y));
            }
            return;
        }

        if (enemyAircraft instanceof EliteEnemy) {
            if (rand < 0.3) {
                props.add(PropFactory.createProp("Blood", x, y));
            } else if (rand < 0.6) {
                props.add(PropFactory.createProp("Fire", x, y));
            } else if (rand < 0.9) {
                props.add(PropFactory.createProp("SuperFire", x, y));
            }
        } else if (enemyAircraft instanceof ElitePlusEnemy) {
            if (rand < 0.2) {
                props.add(PropFactory.createProp("Blood", x, y));
            } else if (rand < 0.4) {
                props.add(PropFactory.createProp("Fire", x, y));
            } else if (rand < 0.6) {
                props.add(PropFactory.createProp("SuperFire", x, y));
            } else if (rand < 0.8) {
                props.add(PropFactory.createProp("Bomb", x, y));
            }
        } else if (enemyAircraft instanceof EliteProEnemy) {
            if (rand < 0.2) {
                props.add(PropFactory.createProp("Blood", x, y));
            } else if (rand < 0.4) {
                props.add(PropFactory.createProp("Fire", x, y));
            } else if (rand < 0.6) {
                props.add(PropFactory.createProp("SuperFire", x, y));
            } else if (rand < 0.8) {
                props.add(PropFactory.createProp("Bomb", x, y));
            } else {
                props.add(PropFactory.createProp("Freeze", x, y));
            }
        }
    }

    private AbstractProp createRandomProp(int x, int y) {
        String[] propTypes = {"Blood", "Fire", "SuperFire", "Bomb", "Freeze"};
        int randomIndex = (int) (Math.random() * propTypes.length);
        return PropFactory.createProp(propTypes[randomIndex], x, y);
    }

    private void applyPropEffect(AbstractProp prop) {
        audioManager.playSupplySound();
        if (prop instanceof BloodProp) {
            heroAircraft.decreaseHp(-((BloodProp) prop).getHealAmount());
            System.out.println("BloodSupply active!");
        } else if (prop instanceof FireProp) {
            // 火力道具将英雄机弹道切换为散射
            firePowerController.activate(new ScatterShootStrategy(3, 30, -1));
            System.out.println("FireSupply active!");
        } else if (prop instanceof SuperFireProp) {
            // 超级火力道具将英雄机弹道切换为环射
            firePowerController.activate(new CircleShootStrategy(12, 30, 5));
            System.out.println("FirePlusSupply active!");
        } else if (prop instanceof BombProp) {
            audioManager.playBombExplosionSound();
            System.out.println("BombSupply active!");
        } else if (prop instanceof FreezeProp) {
            System.out.println("FreezeSupply active!");
        }
    }

    private void damageHero(int damage) {
        heroAircraft.decreaseHp(damage);
        if (heroAircraft.getHp() <= 0) {
            finishGame();
        }
    }

    /**
     * 后处理：
     * 1. 删除无效的子弹
     * 2. 删除无效的敌机
     * 3. 删除无效的道具
     */
    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        // 删除无效道具
        props.removeIf(AbstractFlyingObject::notValid);
    }

    /**
     * 检查游戏是否结束，若结束：关闭线程池
     */
    private void checkResultAction(){
        // 游戏结束检查英雄机是否存活
        if (heroAircraft.getHp() <= 0) {
            timer.cancel(); // 取消定时器并终止所有调度任务
            gameOverFlag = true;
            finishGame();
        }
    }

    private void finishGame() {
        if (gameOverHandled) {
            return;
        }
        gameOverHandled = true;
        timer.cancel();
        gameOverFlag = true;
        audioManager.stopAll();
        audioManager.playGameOverSound();
        System.out.println("Game Over!");
        if (gameOverRecorder == null) {
            recordAndPrintLeaderboardOnce();
        } else {
            gameOverRecorder.run();
        }
    }

    private void recordAndPrintLeaderboardOnce() {
        if (leaderboardRecorded) {
            return;
        }
        leaderboardRecorded = true;
        SwingUtilities.invokeLater(() -> {
            String playerName = JOptionPane.showInputDialog(
                    this,
                    "请输入玩家姓名：",
                    DEFAULT_PLAYER_NAME
            );
            if (playerName == null) {
                playerName = DEFAULT_PLAYER_NAME;
            }
            playerName = playerName.trim();
            if (playerName.isEmpty()) {
                playerName = DEFAULT_PLAYER_NAME;
            }
            leaderboardService.addRecord(playerName, this.score, LocalDateTime.now());
            showLeaderboardDialog();
        });
    }

    private void showLeaderboardDialog() {
        Window window = SwingUtilities.getWindowAncestor(this);
        Frame owner = window instanceof Frame ? (Frame) window : null;
        LeaderboardDialog leaderboardDialog = new LeaderboardDialog(owner, leaderboardService);
        leaderboardDialog.setVisible(true);
    }

    //***********************
    //      Paint 各部分
    //***********************
    /**
     * 重写 paint方法
     * 通过重复调用paint方法，实现游戏动画
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // 绘制背景,图片滚动
        g.drawImage(backgroundImage, 0, this.backGroundTop - Main.WINDOW_HEIGHT, null);
        g.drawImage(backgroundImage, 0, this.backGroundTop, null);
        this.backGroundTop += 1;
        if (this.backGroundTop == Main.WINDOW_HEIGHT) {
            this.backGroundTop = 0;
        }

        // 先绘制子弹，后绘制飞机
        // 这样子弹显示在飞机的下层
        paintImageWithPositionRevised(g, enemyBullets);
        paintImageWithPositionRevised(g, heroBullets);
        paintImageWithPositionRevised(g, enemyAircrafts);

        // 绘制道具 (确保加在绘制英雄机之前，使其图层在下方)
        paintImageWithPositionRevised(g, props);

        g.drawImage(ImageManager.HERO_IMAGE, heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2,
                heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2, null);

        //绘制得分和生命值
        paintScoreAndLife(g);

    }

    private void paintImageWithPositionRevised(Graphics g, List<? extends AbstractFlyingObject> objects) {
        if (objects.isEmpty()) {
            return;
        }

        for (AbstractFlyingObject object : objects) {
            BufferedImage image = object.getImage();
            assert image != null : objects.getClass().getName() + " has no image! ";
            g.drawImage(image, object.getLocationX() - image.getWidth() / 2,
                    object.getLocationY() - image.getHeight() / 2, null);
        }
    }

    private void paintScoreAndLife(Graphics g) {
        int x = 10;
        int y = 25;
        g.setColor(Color.RED);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SCORE: " + this.score, x, y);
        y = y + 20;
        g.drawString("LIFE: " + this.heroAircraft.getHp(), x, y);
    }

    private static class GameTimer extends javax.swing.Timer {
        private GameTimer(int delay) {
            super(delay, null);
        }

        private void cancel() {
            stop();
        }
    }

}
