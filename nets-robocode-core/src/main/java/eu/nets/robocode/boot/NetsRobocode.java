package eu.nets.robocode.boot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class NetsRobocode
{
    private final File robotJarDir = new File("");

    public static void main(String[] args)
    {
        try
        {
            Enumeration<URL> enumeration = NetsRobocode.class.getClassLoader().getResources("team.properties");
            while (enumeration.hasMoreElements())
            {
                System.err.println(enumeration.nextElement());
            }

        } catch (IOException e)
        {
            throw new RuntimeException("Unable to prepare for Nets Robocode launch.", e);
        }

        robocode.Robocode.main(args);
    }
}
