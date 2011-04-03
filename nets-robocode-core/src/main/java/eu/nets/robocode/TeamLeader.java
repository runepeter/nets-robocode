package eu.nets.robocode;

import eu.nets.robocode.message.*;
import robocode.DeathEvent;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class TeamLeader extends TeamMember
{
    protected final Map<String, Position> teamMap;

    protected TeamLeader(final Color color)
    {
        setColor(color);
        this.teamMap = Collections.synchronizedMap(new HashMap<String, Position>());
    }

    @Override
    protected final void init()
    {
        setColors(getColor(), Color.ORANGE, Color.ORANGE, getColor(), Color.GREEN);
        toTeam(new LeaderMessage(getName(), getColor()));
        if (getTeammates().length != 2) {
            throw new IllegalStateException("Incorrect example size -> " + getTeammates().length + ".");
        }
        doInit();
    }

    protected void doInit() {
    }

    @Override
    protected void onMessage(Message message)
    {
        if (message instanceof PositionMessage)
        {
            PositionMessage positionMessage = (PositionMessage) message;
            if (isTeammate(positionMessage.getRobotId())) {
                teamMap.put(positionMessage.getRobotId(), positionMessage.getPosition());
            }
        }
    }

    public void toTeam(Message message)
    {
        try
        {
            broadcastMessage(message);
        } catch (IOException e)
        {
            throw new RuntimeException("Unable to broadcast message.", e);
        }
    }

    public final void toMember(String memberId, Message message)
    {
        try
        {
            sendMessage(memberId, message);
        } catch (IOException e)
        {
            throw new RuntimeException("Unable to send message to '" + memberId + "'.", e);
        }
    }

    @Override
    public final void onDeath(DeathEvent event)
    {
        out.println("Who died? " + event.getTime());

        toTeam(new SelfDestructMessage(getName()));
    }
    
}
