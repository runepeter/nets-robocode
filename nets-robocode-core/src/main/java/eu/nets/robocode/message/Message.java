package eu.nets.robocode.message;

import java.io.Serializable;

public class Message implements Serializable
{
    private final String robotId;

    public Message(final String robotId)
    {
        this.robotId = robotId;
    }

    public String getRobotId()
    {
        return robotId;
    }
}
