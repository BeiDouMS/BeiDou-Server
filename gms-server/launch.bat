@echo off
@title BeiDou
chcp 65001

.\jdk-21.0.2\bin\java.exe  -Dspring.config.location=application.yml -jar BeiDou.jar
pause