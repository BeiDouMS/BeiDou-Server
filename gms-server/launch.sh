#!/bin/sh

# cover write
nohup ./jdk-21.0.2/bin/java -Dspring.config.location=application.yml -jar BeiDou.jar > nohup.out 2>&1 &
