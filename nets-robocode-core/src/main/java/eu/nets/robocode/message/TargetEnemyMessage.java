package eu.nets.robocode.message;

import eu.nets.robocode.Position;

public class TargetEnemyMessage extends Message
{
    private final String enemyId;
    private final Position enemyPosition;

    public TargetEnemyMessage(final String leaderId, final String enemyId, final Position position)
    {
        super(leaderId);
        this.enemyId = enemyId;
        this.enemyPosition = position;
    }

    public String getEnemyId()
    {
        return enemyId;
    }

    public Position getEnemyPosition()
    {
        return enemyPosition;
    }
}
