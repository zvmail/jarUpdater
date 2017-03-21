#!/usr/bin/env bash

declare APP_HOME="$(cd "$(cd "$(dirname "$0")"; pwd -P)"/..; pwd)"

[ -n "$UPDATER_CLASSPATH" ] || UPDATER_CLASSPATH="$APP_HOME/updater/*"
[ -n "$APP_CLASSPATH" ] || APP_CLASSPATH="$APP_HOME/lib/*"

while true; do

	# update jar
	java -cp "$UPDATER_CLASSPATH" org.atline.jarupdater.AppMain

	# run app with new jar
	java -jar "$APP_HOME/printerServer-0.0.1-SNAPSHOT.war"

done
