package eu.nets.robocode.example;

import eu.nets.robocode.TeamDroid;
import eu.nets.robocode.message.Message;
import robocode.HitByBulletEvent;
import robocode.TurnCompleteCondition;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExampleTeamDroid extends TeamDroid
{
    private final Stack<Behaviour> behaviourStack = new Stack<Behaviour>();

    public ExampleTeamDroid()
    {
        behaviourStack.push(new IdleBehaviour());
    }

    @Override
    protected void onMessage(Message message)
    {
    }

    @Override
    protected void behavior()
    {
        behaviourStack.peek().doIt();
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event)
    {
        behaviourStack.push(new DodgeBehaviour());
    }

    private interface Behaviour
    {
        void doIt();
    }

    private class IdleBehaviour implements Behaviour
    {

        public void doIt()
        {
            setTurnRadarLeft(10);
        }
    }

    private class DodgeBehaviour implements Behaviour
    {
        private final AtomicBoolean flip = new AtomicBoolean(false);

        public void doIt()
        {
            boolean currentFlip = flip.get();

            if (currentFlip) {
                setTurnRadarRight(60);
                setAhead(100);
            } else {
                setTurnRadarRight(60);
                setBack(100);
            }
            flip.set(!currentFlip);
            waitFor(new TurnCompleteCondition(ExampleTeamDroid.this));
            behaviourStack.pop();
        }
    }

}
