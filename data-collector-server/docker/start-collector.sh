#!/usr/bin/env bash
set -e

JPMS_SWITCHES="
  --add-opens java.base/java.nio=lmdbjava
  --add-exports java.base/sun.nio.ch=lmdbjava
  --add-reads io.descoped.dc.core=ALL-UNNAMED
  --add-opens io.descoped.dc.core/io.descoped.dc.core=ALL-UNNAMED
  --add-reads io.descoped.dc.content.rawdata=io.descoped.dc.core
  --add-modules ALL-MODULE-PATH
"

if [ -n "$JAVA_OPTS" ]; then
  echo "JAVA_OPTS: $JAVA_OPTS"
fi

if [ -n "$PROXY_HTTP_HOST" ]; then
  PROXY_OPTS="-Dhttp.proxyHost=$PROXY_HTTP_HOST"
fi

if [ -n "$PROXY_HTTP_PORT" ]; then
  PROXY_OPTS="$PROXY_OPTS -Dhttp.proxyPort=$PROXY_HTTP_PORT"
fi

if [ -n "$PROXY_HTTPS_HOST" ]; then
  PROXY_OPTS="-Dhttps.proxyHost=$PROXY_HTTPS_HOST"
fi

if [ -n "$PROXY_HTTPS_PORT" ]; then
  PROXY_OPTS="$PROXY_OPTS -Dhttps.proxyPort=$PROXY_HTTPS_PORT"
fi

if [ -n "$PROXY_OPTS" ]; then
  echo "PROXY_OPTS=$PROXY_OPTS"
fi

if [ "$ENABLE_JMX_REMOTE_DEBUGGING" = true ]; then
  JMX_REMOTE_OPTS="
    -Dcom.sun.management.jmxremote.rmi.port=9992
    -Dcom.sun.management.jmxremote=true
    -Dcom.sun.management.jmxremote.port=9992
    -Dcom.sun.management.jmxremote.ssl=false
    -Dcom.sun.management.jmxremote.authenticate=false
    -Dcom.sun.management.jmxremote.local.only=false
    -Djava.rmi.server.hostname=localhost
  "
else
  JMX_REMOTE_OPTS=""
fi

# lmdbjava picks its glibc library on Linux; the image is Alpine (musl)
DEFAULT_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseContainerSupport -Dlmdbjava.embedded.lib=org/lmdbjava/native/x86_64-linux-musl.so"

JAVA_AGENT_OPTS="-XX:+StartAttachListener -javaagent:$(find /opt/dc/lib/ -type f -iname 'byte-buddy-agent*')"

java $JPMS_SWITCHES $JAVA_OPTS $PROXY_OPTS $DEFAULT_OPTS $JMX_REMOTE_OPTS $JAVA_AGENT_OPTS -p /opt/dc/lib -cp '/opt/dc/classpath/*' -m io.descoped.dc.server/io.descoped.dc.server.Server
