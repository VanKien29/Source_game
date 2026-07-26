@ECHO OFF
chcp 65001 >nul
cd /d "%~dp0"

:: ── Tu dong xin quyen Administrator (can cho L3/L4 Windows Firewall) ──
net session >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    ECHO Dang xin quyen Administrator de bat AntiDDoS L3/L4...
    powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    EXIT /B
)

IF NOT EXIST dist\NROKRAI.jar (
    ECHO Missing dist\NROKRAI.jar
    ECHO Build the project first in NetBeans (Clean and Build), then run this file again.
    PAUSE
    EXIT /B 1
)

ECHO ================================================
ECHO  NRO Server - AntiDDoS L3/L4/L7 (Administrator)
ECHO ================================================
java -server -Xms128m -Xmx768m -XX:+UseG1GC -Dfile.encoding=UTF-8 -jar dist/NROKRAI.jar
PAUSE
