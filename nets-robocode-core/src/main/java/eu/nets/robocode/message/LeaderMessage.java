package eu.nets.robocode.message;

import java.awt.*;

public class LeaderMessage extends Message
{
    private final String leaderId;
    private final Color teamColor;

    public LeaderMessage(String leaderId, Color teamColor)
    {
        super(leaderId);
        this.leaderId = leaderId;
        this.teamColor = teamColor;
    }

    public String getLeaderId()
    {
        return leaderId;
    }

    public Color getTeamColor()
    {
        return teamColor;
    }
}
