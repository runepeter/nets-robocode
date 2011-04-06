package eu.nets.robocode;

import eu.nets.robocode.message.LeaderMessage;
import eu.nets.robocode.message.Message;
import eu.nets.robocode.message.PositionMessage;
import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class TeamMember extends TeamRobot
{
    private final AtomicBoolean running = new AtomicBoolean(false);

    private String leaderId;
    private Color color;

    protected abstract void onMessage(Message message);

    protected abstract void behavior();

    protected void init()
    {
    }

    @Override
    public final void run()
    {
        if (running.getAndSet(true))
        {
            throw new IllegalStateException("Robot is already running.");
        }

        init();

        while (running.get())
        {
            if (!(this instanceof TeamLeader))
            {
                toLeader(new PositionMessage(getName(), getPosition()));
            }
            behavior();
            execute();
        }

        // self-destruct mode.
        while (true)
        {
            turnLeft(360);
            turnRadarRight(360);
        }
    }

    @Override
    public final void setColors(Color bodyColor, Color gunColor, Color radarColor, Color bulletColor, Color scanColor)
    {
        if (!running.get())
            throw new UnsupportedOperationException("Bot must be running before colors may be set.");

        this.setBodyColor(bodyColor);
        this.setScanColor(scanColor);
        this.setGunColor(gunColor);
        this.setRadarColor(radarColor);
    }

    @Override
    public final void onRobotDeath(RobotDeathEvent event)
    {
        if (leaderId != null && leaderId.equals(event.getName()))
        {
            out.println("Leader died. Time to end the war.");
            running.set(false);
            stop(true);
        } else if (isTeammate(event.getName()))
        {
            onTeamMateDied(event);
        } else
        {
            onEnemyDied(event);
        }
    }

    protected void onTeamMateDied(RobotDeathEvent event)
    {
        out.println("Team-mate '" + event.getName() + "' died.");
    }

    protected void onEnemyDied(RobotDeathEvent event)
    {
        out.println("Enemy '" + event.getName() + "' died.");
    }

    @Override
    public final void onMessageReceived(MessageEvent e)
    {
        if (e.getMessage() instanceof Message)
        {
            Message message = (Message) e.getMessage();

            if (message instanceof LeaderMessage)
            {
                onLeaderMessage((LeaderMessage) message);
            } else
            {
                onMessage(message);
            }
        }
    }

    private void onLeaderMessage(LeaderMessage message)
    {
        this.leaderId = message.getLeaderId();
        this.color = message.getTeamColor();

        setColors(color, color, color, color, Color.GREEN);
    }

    protected void moveTo(Position position, int speed)
    {
        moveTo(position.getX(), position.getY(), speed);
    }

    protected void moveTo(double x, double y, int speed)
    {
        setTurnRightRadians(getRelativeAngleTo(x, y));
        setAhead(Math.min(getDistanceTo(x, y), speed));
        waitFor(new TurnCompleteCondition(this));
    }

    protected double getRelativeAngleTo(double x, double y)
    {
        double angle = Utils.normalAbsoluteAngle(Math.atan2(x - getX(), y - getY()));
        return Utils.normalRelativeAngle(angle - getHeadingRadians());
    }

    protected double getDistanceTo(Position position)
    {
        return getDistanceTo(position.getX(), position.getY());
    }

    protected double getDistanceTo(double x, double y)
    {
        double dx = x - getPosition().getX();
        double dy = y - getPosition().getY();
        return Math.hypot(dx, dy);
    }

    protected final void toLeader(Message message)
    {
        try
        {
            sendMessage(leaderId, message);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected final Position getPosition()
    {
        return new Position(getX(), getY());
    }

    public final Color getColor()
    {
        return color;
    }

    public final void setColor(Color color)
    {
        this.color = color;
    }

    public final String getLeaderId()
    {
        return leaderId;
    }

    public final boolean isRunning()
    {
        return running.get();
    }
}
