@echo off
@title BeiDou
chcp 65001

.\jdk-21.0.11+10-jre\bin\java.exe  -Dspring.config.location=application.yml -jar BeiDou.jar
pause