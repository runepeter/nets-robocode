package eu.nets.robocode.blue;

import eu.nets.robocode.Position;
import eu.nets.robocode.TeamBot;
import eu.nets.robocode.message.EnemySpottedMessage;
import eu.nets.robocode.message.Message;
import eu.nets.robocode.message.PositionMessage;
import eu.nets.robocode.message.TargetEnemyMessage;
import eu.nets.robocode.util.BotUtils;
import robocode.BulletMissedEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class BlueTeamBot extends TeamBot
{
    private final Map<String, Position> enemyMap = new HashMap<String, Position>();
    private final Stack<Behaviour> behaviourStack = new Stack<Behaviour>();

    public BlueTeamBot()
    {
        behaviourStack.push(new IdleBehaviour());
    }

    @Override
    protected void onMessage(Message message)
    {
        if (message instanceof TargetEnemyMessage)
        {
            TargetEnemyMessage targetMessage = (TargetEnemyMessage) message;
            out.println("Leader wants me to take out " + targetMessage.getEnemyId() + ".");

            enemyMap.put(targetMessage.getEnemyId(), targetMessage.getEnemyPosition());
            behaviourStack.push(new AttackBehaviour(targetMessage.getEnemyId()));
        }

        if (message instanceof PositionMessage) {

            PositionMessage positionMessage = (PositionMessage) message;
            enemyMap.put(positionMessage.getRobotId(), positionMessage.getPosition());    
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event)
    {
        String enemyId = event.getName();
        if (!isTeammate(enemyId))
        {
            Position position = BotUtils.getPosition(this, event);
            enemyMap.put(enemyId, position);
            toLeader(new EnemySpottedMessage(getName(), enemyId, position));
        }
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event)
    {
        behaviourStack.push(new ScanBehaviour());
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
            turnLeft(360);
            ahead(50);
            turnRight(360);
            back(50);
        }
    }

    private class AttackBehaviour implements Behaviour
    {
        private final String enemyId;

        public AttackBehaviour(final String enemyId)
        {
            this.enemyId = enemyId;
        }

        public void doIt()
        {
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
                fire(3.0);
            }
        }
    }

    private class ScanBehaviour implements Behaviour {
        public void doIt()
        {
            turnRadarLeft(360);
            behaviourStack.pop();
        }
    }
}
