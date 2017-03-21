This is a standalone java application to update jar on remote server.

Its source code is in https://github.com/atline/jarupdater.git

There are 2 kinds of ways you can use it.

1. Use the release.
   1.1 Compile the release
       a) mvn clean assembly:assembly
       b) you will get a folder in target/jarupdater-1.0-bin, something like:
        jarupdater-1.0
        ├── README.txt
        ├── updater
        │   ├── commons-codec-1.9.jar
        │   ├── commons-io-1.3.2.jar
        │   ├── commons-logging-1.2.jar
        │   ├── httpclient-4.5.2.jar
        │   ├── httpcore-4.4.4.jar
        │   ├── jarupdater-1.0.jar
        │   └── settings.conf
        └── updatesite
            ├── amqp-client-3.6.2.jar
            ├── bin
            │   ├── gen_version.py
            │   └── md5sum.exe
            ├── jarupdater_example-1.0.jar
            └── version.txt

   1.2 Move the updatesite to one of your http server, delete the sample jars & put you own jars in updatesite

   1.3 cd bin, python gen_version.py, this will generate a new version.txt in updatesite, its structure is something like:
        version=2016-06-27-16-07-50
        amqp-client-3.6.2.jar=401ddf2d0e5a4ed2a0f464ca830d7ae1
        jarupdater_example-1.0.jar=daba7024d1d97374a8c467b85ccd7dbf
   1.4 change settings.conf, the default is:
        updatesite=http://127.0.0.1/updatesite
        jarrepo=../lib

        the updatesite means the local updater needs to fetch the jars from this website
        the jarrepo means the local updater will fetch the jars and put them in ../lib, this path is based on the jarupdater-1.0.jar in updater
   1.5 in your java launch script you can use something like following if you put your script in a bin directory
        #!/usr/bin/env bash

        declare APP_HOME="$(cd "$(cd "$(dirname "$0")"; pwd -P)"/..; pwd)"

        [ -n "$UPDATER_CLASSPATH" ] || UPDATER_CLASSPATH="$APP_HOME/updater/*"
        [ -n "$APP_CLASSPATH" ] || APP_CLASSPATH="$APP_HOME/lib/*"

        # update jar
        java -cp "$UPDATER_CLASSPATH" org.atline.jarupdater.AppMain

        # run app with new jar
        java -cp "$APP_CLASSPATH" org.atline.jarupdater_example.MyApp "$@"

    NOTE: only when version in version.txt in remote changes, will the local updater download the jars, and it only download the jar which md5sum changes
          if local has a jar which do not specified in version.txt, the local updater will automatically delete the local one, so be careful

2. Use jarupdater in your maven project.
   You can add following to your pom.xml:
   <repositories>
        <repository>
            <id>atline-maven-repo</id>
            <url>https://raw.githubusercontent.com/atline/maven-repo/master/repository</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>org.atline.jarupdater</groupId>
            <artifactId>jarupdater</artifactId>
            <version>1.0</version>
        </dependency>
    </dependencies>

    This will download jarupdater and its dependency when you use maven.
    There is a sample code to describe the usage in the folder of jarupdater_example at https://github.com/atline/examples.git

(End)
