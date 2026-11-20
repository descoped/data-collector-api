#!/usr/bin/env bash

(cd .. && ./mvnw -pl data-collector-server -am clean verify dependency:copy-dependencies -DskipTests)

cp target/data-collector-server-*.jar target/dependency

# Split the jars into a module path and a class path, as Maven Surefire runs the tests
java docker/SplitModulePath.java target/dependency target/classpath io.descoped.dc.server

JAR_PATH="target/dependency"
BYTE_BUDDY_AGENT_JAR=$(find ~+/$JAR_PATH -type f -iname 'byte-buddy-agent*')

# JMX remote (VisualVM on port 9992, no authentication) only on request, as in the image
if [ "$ENABLE_JMX_REMOTE_DEBUGGING" = true ]; then
  JMX_REMOTE_OPTS="-Dcom.sun.management.jmxremote.rmi.port=9992 -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.port=9992 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.local.only=false -Djava.rmi.server.hostname=localhost"
fi

java --add-opens=java.base/java.nio=lmdbjava $JMX_REMOTE_OPTS -XX:+StartAttachListener -javaagent:$BYTE_BUDDY_AGENT_JAR --add-modules ALL-MODULE-PATH -p $PWD/target/dependency -cp "$PWD/target/classpath/*" -m io.descoped.dc.server/io.descoped.dc.server.Server
