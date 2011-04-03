package eu.nets.robocode.message;

import eu.nets.robocode.Position;

public class PositionMessage extends Message
{
    private final Position position;

    public PositionMessage(final String robotId, Position position)
    {
        super(robotId);
        this.position = position;
    }

    public Position getPosition()
    {
        return position;
    }
}
