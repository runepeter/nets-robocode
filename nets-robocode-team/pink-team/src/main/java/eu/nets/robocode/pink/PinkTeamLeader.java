package eu.nets.robocode.pink;

import eu.nets.robocode.Position;
import eu.nets.robocode.TeamLeader;
import eu.nets.robocode.message.EnemySpottedMessage;
import eu.nets.robocode.message.Message;
import eu.nets.robocode.message.PositionMessage;
import eu.nets.robocode.message.TargetEnemyMessage;
import eu.nets.robocode.util.BotUtils;
import org.apache.commons.collections.iterators.LoopingIterator;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;
import robocode.TurnCompleteCondition;

import java.awt.*;
import java.util.*;

public class PinkTeamLeader extends TeamLeader
{
    private final Map<String, Position> enemyMap;
    private final Stack<Behaviour> behaviourStack;
    private Iterator<Position> waypointIterator = null;

    public PinkTeamLeader()
    {
        super(Color.PINK);

        this.enemyMap = new HashMap<String, Position>();
        this.behaviourStack = new Stack<Behaviour>();
    }

    @Override
    protected void doInit()
    {
        this.waypointIterator = new LoopingIterator(Arrays.asList(
                new Position(25, 25),
                new Position(getBattleFieldWidth() - 25, 25),
                new Position(getBattleFieldWidth() - 25, getBattleFieldHeight() - 25),
                new Position(25, getBattleFieldHeight() - 25)
        ));
        this.behaviourStack.push(new FollowWaypointsBehaviour(waypointIterator.next()));
    }

    @Override
    protected void onMessage(Message message)
    {
        super.onMessage(message);
        
        if (message instanceof EnemySpottedMessage)
        {
            EnemySpottedMessage enemy = (EnemySpottedMessage) message;
            enemyMap.put(enemy.getEnemyId(), enemy.getPosition());
            toTeam(new PositionMessage(enemy.getEnemyId(), enemy.getPosition()));
            out.println("Received updated enemy position for " + enemy.getEnemyId() + ".");
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e)
    {
        if (!isTeammate(e.getName()))
        {
            enemyMap.put(e.getName(), BotUtils.getPosition(this, e));
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent event)
    {
        behaviourStack.push(new DodgeBehaviour());
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event)
    {
        String attackerId = event.getBullet().getName();
        out.println("Hit by " + attackerId + ".");
        if (enemyMap.containsKey(attackerId) && !isTeammate(attackerId))
        {
            out.println("ENEMY!");
            toTeam(new TargetEnemyMessage(getName(), attackerId, enemyMap.get(attackerId)));
        }
    }

    @Override
    public void behavior()
    {
        behaviourStack.peek().doIt();
    }

    private interface Behaviour
    {
        void doIt();
    }

    private class FollowWaypointsBehaviour implements Behaviour
    {
        private final Position position;

        public FollowWaypointsBehaviour(final Position position)
        {
            this.position = position;
        }

        public void doIt()
        {
            if (getDistanceTo(position) < 1.5)
            {
                behaviourStack.pop();
                behaviourStack.push(new FollowWaypointsBehaviour(waypointIterator.next()));
            } else
            {
                moveTo(position, 25);
            }
        }
    }

    private class DodgeBehaviour implements Behaviour
    {
        public void doIt()
        {
            setBack(100); //blocking call.
            setTurnLeft(45); // non-blocking call.
            waitFor(new TurnCompleteCondition(PinkTeamLeader.this));
            behaviourStack.pop();
        }
    }

}
