@echo on

set CURR=%CD%

cmd /c mvn archetype:generate -DarchetypeGroupId=eu.nets.robocode -DarchetypeArtifactId=robot-archetype -DarchetypeVersion=0.1-SNAPSHOT -DgroupId=eu.nets.robocode.team -DartifactId=%1 -Dversion=1.0-SNAPSHOT -Dpackaging=jar

cd "%CURR%/%1/src/main/java/eu/nets/robocode/team"

move MyTeamBot.java "%1Bot.java"
move MyTeamDroid.java "%1Droid.java"
move MyTeamLeader.java "%1Leader.java"

cd "%CURR%/%1/src/main/resources/eu/nets/robocode/team"
move MyTeam.team "%1.team"
move MyTeamBot.properties "%1Bot.properties"
move MyTeamDroid.properties "%1Droid.properties"
move MyTeamLeader.properties "%1Leader.properties"

cd "%CURR%/%1"

cmd /c mvn clean install

cd %CURR%
