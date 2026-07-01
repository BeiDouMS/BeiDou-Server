@echo off
REM Batch convert ALL IMG files from client Data to XML using ImgToXml
REM Usage: batch_all.bat

set EXE=E:\pro\WzImg-MCP-Server\Tools\ImgToXml\bin\Release\net10.0-windows\ImgToXml.exe
set INPUT=E:\mxd_soft\2.客户端\083\BeiDou-Client\Data
set OUTPUT=E:\pro\BeiDou-Server\gms-server\wz-zh-CN

echo ========================================
echo IMG to XML Batch Conversion
echo ========================================
echo Input:  %INPUT%
echo Output: %OUTPUT%
echo.

REM Category mapping: client_dir -> server_dir
call :convert base Base.wz
call :convert character Character.wz
call :convert effect Effect.wz
call :convert etc Etc.wz
call :convert item Item.wz
call :convert map Map.wz
call :convert mob Mob.wz
call :convert morph Morph.wz
call :convert npc Npc.wz
call :convert quest Quest.wz
call :convert reactor Reactor.wz
call :convert skill Skill.wz
call :convert sound Sound.wz
call :convert string String.wz
call :convert tamingmob TamingMob.wz
call :convert ui UI.wz

echo.
echo ========================================
echo ALL DONE
echo ========================================
pause
exit /b 0

:convert
echo.
echo [%1] -^> [%2]
"%EXE%" "%INPUT%\%1" "%OUTPUT%\%2"
if errorlevel 1 (
    echo WARNING: %1 had errors
)
exit /b 0
