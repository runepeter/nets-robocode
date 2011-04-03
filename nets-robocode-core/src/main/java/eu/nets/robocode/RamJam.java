package eu.nets.robocode;

import eu.nets.robocode.message.Message;
import eu.nets.robocode.message.PositionMessage;
import eu.nets.robocode.message.TargetEnemyMessage;
import org.apache.commons.lang.builder.EqualsBuilder;
import robocode.*;
import robocode.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class RamJam extends TeamMember implements Droid
{
    private final Map<String, Position> enemyMap = new HashMap<String, Position>();
    private final Stack<Behaviour> behaviourStack = new Stack<Behaviour>();

    public RamJam()
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

            RamThatShitBehaviour behaviour = new RamThatShitBehaviour(targetMessage.getEnemyId());
            if (!behaviourStack.contains(behaviour))
            {
                behaviourStack.push(behaviour);
            }
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event)
    {
        behaviourStack.push(new DodgeBulletBehaviour());
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event)
    {
        behaviourStack.pop();
    }

    @Override
    public void onHitRobot(HitRobotEvent event)
    {
        if (!(behaviourStack.peek() instanceof BackUpBehaviour))
        {
            behaviourStack.push(new BackUpBehaviour());
        }
    }

    @Override
    public void onEnemyDied(RobotDeathEvent event)
    {
        enemyMap.remove(event.getName());
    }

    @Override
    protected void behavior()
    {
        behaviourStack.peek().doIt();
        execute();
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

    private class RamThatShitBehaviour implements Behaviour
    {
        private final String enemyId;

        public RamThatShitBehaviour(final String enemyId)
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
                ahead(getDistanceTo(position));
                fire(3);
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (o == null)
            {
                return false;
            }
            if (o == this)
            {
                return true;
            }
            if (o.getClass() != getClass())
            {
                return false;
            }
            RamThatShitBehaviour rhs = (RamThatShitBehaviour) o;
            return new EqualsBuilder()
                    .append(enemyId, rhs.enemyId)
                    .isEquals();
        }
    }

    private class BackUpBehaviour implements Behaviour
    {

        public void doIt()
        {
            back(100);
            behaviourStack.pop();
        }
    }

    private class DodgeBulletBehaviour implements Behaviour {

        public void doIt()
        {
            setTurnLeft(Math.PI);
            setBack(100);
            behaviourStack.pop();
        }
    }

}
