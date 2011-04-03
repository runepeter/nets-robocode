package eu.nets.robocode.message;

import eu.nets.robocode.Position;

public class EnemySpottedMessage extends Message
{
    private final String enemyId;
    private final Position position;

    public EnemySpottedMessage(final String senderId, final String enemyId, final Position position)
    {
        super(senderId);
        this.enemyId = enemyId;
        this.position = position;
    }

    public String getEnemyId()
    {
        return enemyId;
    }

    public Position getPosition()
    {
        return position;
    }

}
