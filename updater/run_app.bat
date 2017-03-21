@echo off

:restart
set APP_HOME=%~dp0
set UPDATER_CLASSPATH=%APP_HOME%\updater\*
set APP_CLASSPATH=%APP_HOME%\*

echo PATH = %APP_HOME%

rem update jar
java -cp "%UPDATER_CLASSPATH%" org.atline.jarupdater.AppMain

rem run app with new jar
java -jar %APP_HOME%\printerServer-0.0.1-SNAPSHOT.war

goto restart