"C:\Program Files\Java\jdk-15.0.1\bin\java.exe" "-Dmaven.multiModuleProjectDirectory=D:\Programming\IdeaProjects\Nicehash Test" "-Dmaven.home=C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven3" "-Dclassworlds.conf=C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven3\bin\m2.conf" "-Dmaven.ext.class.path=C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven-event-listener.jar" "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\lib\idea_rt.jar=50869:C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\bin" -Dfile.encoding=UTF-8 -classpath "C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven3\boot\plexus-classworlds-2.6.0.jar;C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven3\boot\plexus-classworlds.license" org.codehaus.classworlds.Launcher -Didea.version2020.1.1 org.apache.maven.plugins:maven-clean-plugin:2.5:clean
"C:\Program Files\Java\jdk-15.0.1\bin\java.exe" "-Dmaven.multiModuleProjectDirectory=D:\Programming\IdeaProjects\Nicehash Test" "-Dmaven.home=C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven3" "-Dclassworlds.conf=C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven3\bin\m2.conf" "-Dmaven.ext.class.path=C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven-event-listener.jar" "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\lib\idea_rt.jar=65145:C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\bin" -Dfile.encoding=UTF-8 -classpath "C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven3\boot\plexus-classworlds-2.6.0.jar;C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven3\boot\plexus-classworlds.license" org.codehaus.classworlds.Launcher -Didea.version2020.1.1 org.apache.maven.plugins:maven-compiler-plugin:3.1:compile
"C:\Program Files\Java\jdk-15.0.1\bin\java.exe" "-Dmaven.multiModuleProjectDirectory=D:\Programming\IdeaProjects\Nicehash Test" "-Dmaven.home=C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven3" "-Dclassworlds.conf=C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven3\bin\m2.conf" "-Dmaven.ext.class.path=C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven-event-listener.jar" "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\lib\idea_rt.jar=65148:C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\bin" -Dfile.encoding=UTF-8 -classpath "C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven3\boot\plexus-classworlds-2.6.0.jar;C:\Program Files\JetBrains\IntelliJ IDEA 2020.1\plugins\maven\lib\maven3\boot\plexus-classworlds.license" org.codehaus.classworlds.Launcher -Didea.version2020.1.1 org.apache.maven.plugins:maven-assembly-plugin:2.2-beta-5:single
ssh evan@192.168.86.36 -x screen -X -S AdjustBot quit
scp target\NicehashTest-1.0-SNAPSHOT-jar-with-dependencies.jar evan@192.168.86.36:/home/evan/Mining/AdjustBot/AdjustBot.jar
ssh evan@192.168.86.36 -x /home/evan/Mining/AdjustBot/screenStart.sh