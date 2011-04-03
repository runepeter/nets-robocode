package eu.nets.robocode;

import eu.nets.robocode.message.EnemySpottedMessage;
import eu.nets.robocode.message.Message;
import eu.nets.robocode.message.TargetEnemyMessage;
import eu.nets.robocode.red.EvasiveManouver;
import robocode.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RedTeamLeader extends TeamLeader
{
    private final Map<String, Position> enemyMap;
    private final List<EvasiveManouver> evasiveManouvers = Arrays.asList(new BackAndTurnLeft(), new BackAndTurnRight(), new BackAndMoveAround());

    private Stack<Behaviour> behaviourStack = new Stack<Behaviour>()
    {
        @Override
        public Behaviour pop()
        {
            if (size() > 1)
            {
                return super.pop();    //To change body of overridden methods use File | Settings | File Templates.
            } else
            {
                return peek();
            }
        }
    };

    private final Random evasiveRandom;

    private final AtomicBoolean underFire = new AtomicBoolean(false);

    public RedTeamLeader()
    {
        super(Color.RED);
        this.enemyMap = new HashMap<String, Position>();
        this.evasiveRandom = new Random();

        behaviourStack.push(new DefaultBehaviour());
    }

    @Override
    protected void onMessage(Message message)
    {
        if (message instanceof EnemySpottedMessage)
        {
            EnemySpottedMessage enemy = (EnemySpottedMessage) message;
            enemyMap.put(enemy.getRobotId(), enemy.getPosition());
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e)
    {
        if (!isTeammate(e.getName()))
        {

            double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
            double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
            double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);

            enemyMap.put(e.getName(), new Position(enemyX, enemyY));
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent event)
    {
        int index = evasiveRandom.nextInt(evasiveManouvers.size());
        evasiveManouvers.get(index).behaviour(this);
    }

    long lastHit = -1;
    double bulletBearing = -1;
    String shooter = null;

    @Override
    public void onHitByBullet(HitByBulletEvent event)
    {
        this.lastHit = System.currentTimeMillis();
        this.bulletBearing = event.getBearing();
        this.shooter = event.getBullet().getName();

        if (!(behaviourStack.peek() instanceof UnderFireBehaviour))
        {
            behaviourStack.push(new UnderFireBehaviour());
        }
    }

    @Override
    public void onEnemyDied(RobotDeathEvent event)
    {
        enemyMap.remove(event.getName());
        turnLeft(360);
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event)
    {
        behaviourStack.pop();
    }

    @Override
    protected void behavior()
    {
        behaviourStack.peek().doIt();
    }

    private interface Behaviour
    {
        void doIt();
    }

    private class DefaultBehaviour implements Behaviour
    {
        public void doIt()
        {
            if (getDistanceTo(25, 25) > 1.5)
            {
                moveTo(25, 25, 35);
            } else
            {
                Iterator<Position> it = enemyMap.values().iterator();
                if (it.hasNext())
                {
                    Position position = it.next();
                    double angle = getRelativeAngleTo(position.getX(), position.getY());
                    if (angle != 0)
                    {
                        setTurnRightRadians(angle);
                    } else
                    {
                        fire(1.0);
                    }
                } else
                {
                    doNothing();
                }
            }
        }
    }

    private class UnderFireBehaviour implements Behaviour
    {
        public void doIt()
        {
            if (System.currentTimeMillis() - lastHit > 2000)
            {
                behaviourStack.pop();
                return;
            }

            toTeam(new TargetEnemyMessage(getName(), shooter, enemyMap.get(shooter)));

            if (Math.abs(bulletBearing) > 0.5)
            {
                turnRight(bulletBearing);
            } else
            {
                fire(2.0);
            }

            bulletBearing = 0;
        }
    }

    private class BackAndTurnLeft implements EvasiveManouver
    {
        public void behaviour(TeamMember member)
        {
            setBack(50);
            setTurnLeft(45);
        }
    }

    private class BackAndTurnRight implements EvasiveManouver
    {
        public void behaviour(TeamMember member)
        {
            setBack(50);
            setTurnRight(45);
        }
    }

    private class BackAndMoveAround implements EvasiveManouver
    {
        public void behaviour(TeamMember member)
        {
            setBack(50);
            setTurnLeft(45);
            setAhead(150);
            setTurnRight(30);
        }
    }

}
