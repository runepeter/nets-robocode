package eu.nets.robocode.util;

import eu.nets.robocode.Position;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public final class BotUtils
{
    private BotUtils() {}


    public static Position getPosition(AdvancedRobot scannerBot, ScannedRobotEvent event)
    {
        double absoluteBearing = scannerBot.getHeadingRadians() + event.getBearingRadians();
        double x = scannerBot.getX() + event.getDistance() * Math.sin(absoluteBearing);
        double y = scannerBot.getY() + event.getDistance() * Math.cos(absoluteBearing);

        return new Position(x, y);
    }
}
