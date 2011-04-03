package eu.nets.robocode;

import eu.nets.robocode.message.Message;

import java.awt.*;

public class BlueTeamLeader extends TeamLeader
{
    public BlueTeamLeader()
    {
        super(Color.BLUE);
    }

    @Override
    protected void behavior()
    {
        turnRadarRight(5);
    }
}
