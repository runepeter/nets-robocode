package eu.nets.robocode;

import java.awt.geom.Point2D;
import java.io.Serializable;

public class Position extends Point2D.Double implements Serializable
{
    public Position(double x, double y)
    {
        super(x, y);
    }
}
