# amd64 and arm64: Temurin on Ubuntu (glibc), the platform jffi's and lmdbjava's native libraries are built for
FROM eclipse-temurin:17.0.21_9-jdk-noble AS build

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
FROM eclipse-temurin:17.0.21_9-jdk-noble

ENV DC_HOME=/opt/dc

ENV JAVA_OPTS=
ENV PROXY_HTTP_HOST=
ENV PROXY_HTTP_PORT=
ENV PROXY_HTTPS_HOST=
ENV PROXY_HTTPS_PORT=
ENV ENABLE_JMX_REMOTE_DEBUGGING=false

#
# Resources from build image
#
COPY --from=build /build/lib $DC_HOME/lib/
COPY --from=build /build/classpath $DC_HOME/classpath/
COPY target/classes/log4j2-logstash.xml $DC_HOME/

COPY --chmod=755 docker/start-collector.sh /run.sh

#
# Runs as user dc (10001); its home and /rawdata are writable by group root too, so any --user with group 0 works
#
RUN groupadd -r -g 10001 dc \
    && useradd -r -u 10001 -g dc -d $DC_HOME -s /usr/sbin/nologin dc \
    && mkdir -p /rawdata /conf /certs \
    && chown -R dc:0 $DC_HOME /rawdata \
    && chmod -R g=u $DC_HOME /rawdata

USER 10001

WORKDIR $DC_HOME

VOLUME ["/conf", "/certs"]

EXPOSE 9990
EXPOSE 9992

CMD ["/run.sh"]
