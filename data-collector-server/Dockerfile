# check=skip=FromPlatformFlagConstDisallowed
# x86_64 only: the jffi and LMDB native libraries below are the x86_64 builds
FROM --platform=linux/amd64 eclipse-temurin:17.0.21_9-jdk-alpine-3.24 AS build

#
# Split the jars into a module path and a class path, as Maven Surefire runs the tests
#
COPY target/dependency /build/lib/
COPY target/data-collector-*.jar /build/lib/
COPY docker/SplitModulePath.java /build/
RUN java /build/SplitModulePath.java /build/lib /build/classpath io.descoped.dc.server

#
# Build DataCollector image
#
FROM --platform=linux/amd64 alpine:3.24.4

RUN apk add --no-cache bash su-exec curl openssl

ENV DC_HOME=/opt/dc

ENV JAVA_OPTS=
ENV PROXY_HTTP_HOST=
ENV PROXY_HTTP_PORT=
ENV PROXY_HTTPS_HOST=
ENV PROXY_HTTPS_PORT=
ENV ENABLE_JMX_REMOTE_DEBUGGING=true

#ENV UID=dc
#ENV GID=dc

#RUN useradd -r --create-home --home-dir $DC_HOME --groups $GID --shell /bin/bash $UID

#
# Resources from build image
#
COPY --from=build /opt/java/openjdk /opt/jdk/
COPY --from=build /build/lib $DC_HOME/lib/
COPY --from=build /build/classpath $DC_HOME/classpath/
RUN mkdir -p /ld_lib \
    && LMDB_NATIVE_JAR=$(find $DC_HOME/classpath -type f -iname 'jffi-*-native.jar') \
    && unzip "$LMDB_NATIVE_JAR" "jni/x86_64-Linux/*" -d /ld_lib \
    && rm -f "$LMDB_NATIVE_JAR"
COPY target/classes/log4j2-logstash.xml $DC_HOME/

ADD docker/start-collector.sh /run.sh
RUN chmod +x /run.sh

#COPY docker/entrypoint.bash /entrypoint.sh
#RUN chmod +x /entrypoint.sh

ENV PATH=/opt/jdk/bin:$PATH
ENV JAVA_HOME=/opt/jdk
ENV LD_LIBRARY_PATH=/ld_lib/jni/x86_64-Linux/:$LD_LIBRARY_PATH

WORKDIR $DC_HOME

VOLUME ["/conf", "/certs"]

EXPOSE 9990
EXPOSE 9992

#ENTRYPOINT ["/entrypoint.sh"]
CMD ["/run.sh"]
