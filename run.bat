@ECHO OFF

IF NOT EXIST dist\NROKRAI.jar (
	ECHO Missing dist\NROKRAI.jar
	ECHO Build the project first in NetBeans (Clean and Build), then run this file again.
	PAUSE
	EXIT /B 1
)

java -server -Xms128m -Xmx768m -XX:+UseG1GC -Dfile.encoding=UTF-8 -jar dist/NROKRAI.jar
PAUSE
