@echo off
@title Cosmic
set PATH="C:\Program Files\Java\jdk1.8.0_241\bin";%PATH%
java -Xmx2048m -Dwzpath=wz\ -jar target\Cosmic.jar
pause