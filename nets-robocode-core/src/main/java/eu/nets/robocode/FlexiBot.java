package eu.nets.robocode;

import eu.nets.robocode.message.EnemySpottedMessage;
import eu.nets.robocode.message.Message;
import eu.nets.robocode.message.PositionMessage;
import eu.nets.robocode.message.TargetEnemyMessage;
import robocode.HitByBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class FlexiBot extends TeamMember
{
    private final Map<String, Position> enemyMap = new HashMap<String, Position>();
    private final Stack<Behaviour> behaviourStack = new Stack<Behaviour>();

    public FlexiBot()
    {
        behaviourStack.push(new IdleBehaviour());
    }

    @Override
    protected void onMessage(Message message)
    {
        if (message instanceof TargetEnemyMessage)
        {
            TargetEnemyMessage targetMessage = (TargetEnemyMessage) message;

            enemyMap.put(targetMessage.getEnemyId(), targetMessage.getEnemyPosition());
            behaviourStack.push(new PredatorBehaviour(targetMessage.getEnemyId()));
        }
    }

    @Override
    protected void init()
    {
        setAdjustGunForRobotTurn(false);
    }

    @Override
    protected void behavior()
    {
        behaviourStack.peek().doIt();
        execute();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event)
    {
        if (!isTeammate(event.getName()))
        {
            double absoluteBearing = getHeadingRadians() + event.getBearingRadians();
            double enemyX = getX() + event.getDistance() * Math.sin(absoluteBearing);
            double enemyY = getY() + event.getDistance() * Math.cos(absoluteBearing);

            EnemySpottedMessage message = new EnemySpottedMessage(getName(), event.getName(), new Position(enemyX, enemyY));
            toLeader(message);
        }
    }

    @Override
    public void onEnemyDied(RobotDeathEvent event)
    {
        enemyMap.remove(event.getName());
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event)
    {
        behaviourStack.push(new DodgeBulletBehaviour());
    }

    private interface Behaviour
    {
        void doIt();
    }

    private class IdleBehaviour implements Behaviour
    {
        public void doIt()
        {
            toLeader(new PositionMessage(getName(), getPosition()));
            setTurnLeft(Math.PI);
        }
    }

    private class PredatorBehaviour implements Behaviour
    {

        private final String enemyId;

        public PredatorBehaviour(final String enemyId)
        {
            this.enemyId = enemyId;
        }

        public void doIt()
        {
            toLeader(new PositionMessage(getName(), getPosition()));

            Position position = enemyMap.get(enemyId);
            if (position == null)
            {
                behaviourStack.pop();
                return;
            }

            double angle = Utils.normalAbsoluteAngle(Math.atan2(position.getX() - getX(), position.getY() - getY()));
            double angleDifference = Utils.normalRelativeAngle(angle - getHeadingRadians());
            if (Math.abs(angleDifference) > 0)
            {
                turnRightRadians(angleDifference);
            } else
            {
                fire(2.0);
            }
        }
    }

    private class DodgeBulletBehaviour implements Behaviour
    {

        public void doIt()
        {
            setTurnLeft(Math.PI);
            setBack(100);
            behaviourStack.pop();
        }
    }

}
