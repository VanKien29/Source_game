@ECHO OFF

IF NOT EXIST dist\NROKRAI.jar (
	ECHO Missing dist\NROKRAI.jar
	ECHO Build the project first in NetBeans (Clean and Build), then run this file again.
	PAUSE
	EXIT /B 1
)

java -server -jar -Dfile.encoding=UTF-8 dist/NROKRAI.jar
PAUSE