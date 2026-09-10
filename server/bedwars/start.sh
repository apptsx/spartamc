#!/bin/bash
cd /home/bedwars
java -Xms1024M -Xmx1024M -javaagent:modifier.jar -jar server.jar nogui
