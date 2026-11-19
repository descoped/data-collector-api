#!/usr/bin/env bash

(cd .. && ./mvnw -pl data-collector-server -am clean verify dependency:copy-dependencies -DskipTests)

cp target/data-collector-server-*.jar target/dependency

# Split the jars into a module path and a class path, as Maven Surefire runs the tests
java docker/SplitModulePath.java target/dependency target/classpath io.descoped.dc.server

JAR_PATH="target/dependency"
BYTE_BUDDY_AGENT_JAR=$(find ~+/$JAR_PATH -type f -iname 'byte-buddy-agent*')

LMDB_NATIVE_JAR=$(find ~+/target/classpath -type f -iname 'jffi-*-native.jar')
unzip "$LMDB_NATIVE_JAR" "jni/Darwin/*" -d target/ld_lib
export LD_LIBRARY_PATH=$PWD/target/ld_lib/jni/Darwin/:$LD_LIBRARY_PATH
rm -f "$LMDB_NATIVE_JAR"

java -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI --add-opens=java.base/java.nio=lmdbjava -Dcom.sun.management.jmxremote.rmi.port=9992 -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.port=9992 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.local.only=false -Djava.rmi.server.hostname=localhost -XX:+StartAttachListener -javaagent:$BYTE_BUDDY_AGENT_JAR --add-modules ALL-MODULE-PATH -p $PWD/target/dependency -cp "$PWD/target/classpath/*" -m io.descoped.dc.server/io.descoped.dc.server.Server
