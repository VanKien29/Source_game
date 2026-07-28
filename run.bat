@ECHO OFF
chcp 65001 >nul
cd /d "%~dp0"

set "JAVA_EXE="
for %%J in ("%ProgramFiles%\Java\jdk-17\bin\java.exe" "%ProgramFiles%\Java\jdk-19\bin\java.exe" "%ProgramFiles%\Common Files\Oracle\Java\javapath\java.exe") do (
    if not defined JAVA_EXE if exist "%%~J" set "JAVA_EXE=%%~J"
)
if not defined JAVA_EXE set "JAVA_EXE=java"

:: Tu dong xin quyen Administrator (can cho L3/L4 Windows Firewall)
if /I "%~1" NEQ "--admin" (
    net session >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        ECHO Dang mo lai bang quyen Administrator...
        powershell -NoProfile -ExecutionPolicy Bypass -Command "Start-Process -FilePath 'cmd.exe' -ArgumentList '/k \"\"%~f0\" --admin\"' -WorkingDirectory '%~dp0' -Verb RunAs"
        ECHO Neu khong thay cua so moi, hay bam chuot phai run.bat va chon Run as administrator.
        TIMEOUT /T 3 >nul
        EXIT /B
    )
)

ECHO ================================================
ECHO  NRO Server - AntiDDoS L3/L4/L7 (Administrator)
ECHO ================================================
ECHO Java: %JAVA_EXE%

IF EXIST dist\NROKRAI.jar (
    "%JAVA_EXE%" -server -Xms128m -Xmx768m -XX:+UseG1GC -Dfile.encoding=UTF-8 -jar dist\NROKRAI.jar
    PAUSE
    EXIT /B
)

IF EXIST build\classes\server\ServerManager.class (
    ECHO Khong thay dist\NROKRAI.jar, dang chay truc tiep tu build\classes...
    "%JAVA_EXE%" -server -Xms128m -Xmx768m -XX:+UseG1GC -Dfile.encoding=UTF-8 -cp "lib\LIB_SALE.jar;build\classes" server.ServerManager
    PAUSE
    EXIT /B
)

ECHO Khong thay dist\NROKRAI.jar hoac build\classes\server\ServerManager.class
ECHO Hay mo NetBeans va chon Clean and Build de tao file chay.
PAUSE
EXIT /B 1
