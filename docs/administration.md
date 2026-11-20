# Administration

How to configure, run and operate the Data Collector server: configuration sources and keys, the Docker image, logging,
metrics, the integrity check and recovery, PostgreSQL, encryption of content, and client certificates.

- [Configuration sources](#configuration-sources)
- [Configuration reference](#configuration-reference)
- [The Docker image](#the-docker-image)
- [Running without Docker](#running-without-docker)
- [Logging](#logging)
- [Metrics and profiling](#metrics-and-profiling)
- [Integrity check and recovery](#integrity-check-and-recovery)
- [PostgreSQL](#postgresql)
- [Encryption of rawdata content](#encryption-of-rawdata-content)
- [Client certificates](#client-certificates)
- [The examples](#the-examples)

## Configuration sources

`io.descoped.dc.server.Server` builds the configuration from these sources, in this order. A key from a later source
replaces the same key from an earlier one.

| # | Source | Read from |
|---|---|---|
| 1 | `application-defaults.properties` | Every match on the class path, in class path order, a later one winning; then the file `application-defaults.properties` in the working directory, if it exists. In the image, the server jar and the core jar each hold one. |
| 2 | `/conf/application-defaults.properties` | The file, if it exists. |
| 3 | `/conf/application.properties` | The file, if it exists. |
| 4 | `/conf/application-override.properties` | The file, if it exists. |
| 5 | Environment variables with the prefix `DC_` | Every variable whose name starts with `DC_`. The key is the rest of the name, unchanged: `DC_http.port` sets `http.port`. There is no case conversion and no mapping of `_` to `.`. |
| 6 | Java system properties | Every system property, including the JVM's own (`java.version`, `user.dir`, ...). `-Dhttp.port=9991` in `JAVA_OPTS` sets `http.port`. |

The sources are implemented by `io.descoped.dc.application.server.Configurations`:

- Values are literal. A value that contains `${...}` is not resolved as a reference to another key.
- The profiles of dynamic-configuration (`config.profiles`, `CONFIG_PROFILES`) do not apply to these sources: no
  `application-<profile>.properties` is read.
- A missing file is skipped. A file that cannot be read stops the start-up.

Environment variable names with dots cannot be exported by a POSIX shell, but Docker, Compose and `env` pass them:

```sh
docker run -e 'DC_http.port=9991' -p 9991:9991 data-collector:dev
env 'DC_rawdata.client.provider=memory' java ...
```

```yaml
services:
  data-collector:
    environment:
      - DC_data.collector.worker.threads=8
```

Every key and value is visible in `GET /health?config`. Values of keys whose name contains `pass`, `pwd`, `secret`,
`credential`, `token` or `encryption` (any case) are masked there. A specification can read every configuration value,
masked or not, with `${ENV.'key'}` (see [Specification DSL](specification-dsl.md)); this is how specifications get
values such as `maskinporten.clientId`.

## Configuration reference

Every key the server and the engine read, by area. *Defaults file* is the value in the class path
`application-defaults.properties`; *Code default* is what the code uses when the key is absent. A value that cannot be
parsed (for example a non-number where a number is expected) is an error.

### HTTP server and CORS

Read by `data-collector-server-base` at start-up.

| Key | Defaults file | Code default | Meaning |
|---|---|---|---|
| `http.host` | `0.0.0.0` | `localhost` | Address the HTTP listener binds to. |
| `http.port` | `9990` | none, required | HTTP port. |
| `http.cors.allow.origin` | `.*` | `.*` | Comma-separated regular expressions. An `Origin` that one of them matches in full is allowed. |
| `http.cors.allow.methods` | `POST,GET,PUT,DELETE,HEAD` | same | Methods a preflight may ask for. |
| `http.cors.allow.header` | `Content-Type,Authorization` | same | Headers a preflight may ask for; also sent as `Access-Control-Expose-Headers`. |
| `http.cors.allow.credentials` | `false` | `false` | `true` adds `Access-Control-Allow-Credentials: true`. |
| `http.cors.allow.max-age` | `900` | `900` | `Access-Control-Max-Age` of a preflight, in seconds. |
| `prometheus.defaultExports.disabled` | not set | `false` | `true` leaves out the JVM and process metrics of the Prometheus client. |

How the CORS handler applies these is described in [REST API: authentication and CORS](rest-api.md#authentication-and-cors).
The server has no authentication settings.

### Content store

| Key | Defaults file | Code default | Meaning |
|---|---|---|---|
| `content.stream.connector` | `rawdata` | none in the server | The content store provider. The image contains the `rawdata` connector only. (A `Worker` used as a library defaults to `discarding`; see [Developer guide](developer-guide.md).) |
| `rawdata.client.provider` | `memory` | none | The rawdata-client provider. The image contains `memory` and `discard` (part of rawdata-client-api), `filesystem`, `gcs` and `postgres`. |

The rawdata connector passes the whole configuration to rawdata-client, which reads its provider's keys. Those keys
belong to rawdata-client; see the [rawdata-client project](https://github.com/descoped/rawdata-client-project). The
example profiles use these:

| Profile | Keys (rawdata-client) |
|---|---|
| `filesystem`, `filesystem-secrets` | `filesystem.storage-folder=/target/avro/rawdata-store`, `local-temp-folder=/target/avro/temp`, `avro-file.max.seconds=60`, `avro-file.max.bytes=67108864`, `avro-file.sync.interval=200`, `listing.min-interval-seconds=0` |
| `gcs` | `gcs.bucket-name`, `gcs.service-account.key-file=/certs/gcs-secret/gcs_sa_test.json`, `gcs.listing.min-interval-seconds=30`, `local-temp-folder=/target/_tmp_avro_`, `avro-file.max.seconds=86400`, `avro-file.max.bytes=67108864`, `avro-file.sync.interval=524288` |
| `postgres` | `postgres.driver.host=dc-postgres-db`, `postgres.driver.port=5432`, `postgres.driver.user=rdc`, `postgres.driver.password=rdc`, `postgres.driver.database=rdc`, `rawdata.postgres.consumer.prefetch-size=100`, `rawdata.postgres.consumer.prefetch-poll-interval-when-empty=1000` |

The class path defaults also hold the `postgres.driver.*` and `rawdata.postgres.consumer.*` keys above, with
`postgres.driver.host=localhost`, and `file.system.provider.directory=/rawdata`, which no code in the image reads.

`local-temp-folder` is also read by the server: the recovery content store uses its value with the suffix `-recovery`.

### Encryption

Read by the rawdata connector. See [Encryption of rawdata content](#encryption-of-rawdata-content).

| Key | Default | Meaning |
|---|---|---|
| `rawdata.encryption.provider` | not set | `google-secret-manager`, or `dynamic-secret-configuration` (`dynamic-configuration` is accepted for it). Not set: the key and salt are read from the configuration. |
| `rawdata.encryption.key` | not set | The password; with `google-secret-manager`, the name of the secret that holds it. |
| `rawdata.encryption.salt` | not set | The salt; with `google-secret-manager`, the name of the secret that holds it. |
| `rawdata.encryption.gcp.projectId` | not set | Google Cloud project of the secrets. Required with `google-secret-manager`. |
| `rawdata.encryption.gcp.serviceAccountKeyPath` | not set | With `google-secret-manager`: a service-account JSON key file; without it, Application Default Credentials. With `dynamic-secret-configuration`: a properties file that holds `rawdata.encryption.key` and `rawdata.encryption.salt`. |

### Engine and HTTP client

Read by the engine (`data-collector-core`) when a task is built. Every task gets the whole configuration.

| Key | Code default | Meaning |
|---|---|---|
| `data.collector.worker.threads` | available processors + 2 | Size of each task's fixed thread pool. |
| `data.collector.http.version` | `HTTP_2` | `HTTP_1_1` or `HTTP_2` (any case). |
| `data.collector.http.followRedirects` | not set: the Java HTTP client's default, `NEVER` | `NEVER`, `ALWAYS`, or `NORMAL` (always, except from HTTPS to HTTP); any case. |
| `data.collector.http.client.timeout.seconds` | `20` | Connect timeout of the HTTP client. |
| `data.collector.http.request.timeout.seconds` | `15` | How long to wait for each response. |
| `data.collector.http.request.retryCount` | `3` | Attempts per request, counting the first. |
| `data.collector.http.request.napOnRetryInMillis` | `150` | Pause between attempts, in milliseconds. When this key is set, the code reads the value of `data.collector.http.request.retryCount` as the pause, and fails when that key is not set. |
| `data.collector.parallel.queueBuffer.threshold` | `-1` (off) | A value above `-1` makes a `Parallel` node wait for its running entries to finish after every this many entries of a page, for responses that are not streamed to a body handler. `0` fails with a division by zero. |
| `data.collector.print-execution-plan` | `false` | `true` logs each task's specification (as YAML) and its execution plan when the task starts. |

### Client certificate settings

See [Client certificates](#client-certificates).

| Key | Code default | Meaning |
|---|---|---|
| `data.collector.certs.directory` | the working directory | Directory scanned for certificate bundles when a task starts. Not used when `data.collector.sslBundle.provider` is set. |
| `data.collector.sslBundle.provider` | not set | `google-secret-manager`: one bundle is read from Google Secret Manager instead of the directory. The value is passed to secrets-client as `secrets.provider`. |
| `data.collector.sslBundle.gcp.projectId` | not set | Google Cloud project of the secrets. |
| `data.collector.sslBundle.gcp.serviceAccountKeyPath` | not set | Service-account JSON key file; without it, Application Default Credentials. |
| `data.collector.sslBundle.type` | not set, required | `pem` (any case) or another value, which means a PKCS#12 archive (`p12` in the examples). |
| `data.collector.sslBundle.name` | not set | Name of the bundle. A specification selects it by this name. |
| `data.collector.sslBundle.publicCertificate` | not set | `pem`: name of the secret with the certificate. |
| `data.collector.sslBundle.privateCertificate` | not set | `pem`: name of the secret with the private key. |
| `data.collector.sslBundle.archiveCertificate` | not set | PKCS#12: name of the secret with the archive. |
| `data.collector.sslBundle.passphrase` | not set | Name of the secret with the passphrase. |

### Integrity check and recovery settings

| Key | Code default | Meaning |
|---|---|---|
| `data.collector.integrityCheck.database.location` | `<working directory>/lmdb` | Where the LMDB indexes live. Not set or empty: `<working directory>/lmdb`. Starting with `./`, `.\` or `.`: `<working directory>/<rest of the value>/lmdb`. Anything else: the path as given. |
| `data.collector.integrityCheck.dbSizeInMb` | `50` | LMDB map size of each topic's index, in MB. The size in bytes is computed as a Java `int`, so values from 2048 up overflow. |
| `data.collector.integrityCheck.consumer.timeoutInSeconds` | `15` | How long the check waits for the next message before it ends. |

### Rawdata dump

A service that writes a topic's content to files. It is registered only when it is enabled at start-up.

| Key | Code default | Meaning |
|---|---|---|
| `data.collector.rawdata.dump.enabled` | `false` | `true` starts the service. |
| `data.collector.rawdata.dump.location` | not set | Target directory. Not set: the service logs a warning and does not start. Empty: `./storage`. |
| `data.collector.rawdata.dump.topic` | not set | The topic to read. The service reads it until the server stops. |

Each message is written to `<location>/<topic>/<position>/<content key><extension>`, where the extension comes from
the detected media type (`.json`, `.xml`, or the subtype Apache Tika reports). The position is used as a directory
name as it is.

## The Docker image

`data-collector-server/Dockerfile` builds the release image and `Dockerfile-dev` the development image. They differ
only in `ENABLE_JMX_REMOTE_DEBUGGING`.

| | |
|---|---|
| Base image | `eclipse-temurin:17.0.21_9-jdk-noble` (Temurin JDK 17 on Ubuntu, glibc), for build and runtime stages |
| Platforms | `linux/amd64`, `linux/arm64` (the native libraries of jffi and lmdbjava are built for glibc) |
| User | `dc`, uid 10001, group `dc` (gid 10001). `/opt/dc` and `/rawdata` belong to `dc:0` and the group has the owner's rights, so any `--user` in group 0 can write them. |
| Working directory | `/opt/dc` (`DC_HOME`) |
| Ports | `9990` (HTTP, `http.port`), `9992` (JMX, only when enabled) |
| Volumes | `/conf` (configuration files), `/certs` (certificate bundles). `/rawdata` exists and is writable but is not a declared volume, and no key the code reads points at it by default. |
| Command | `/run.sh` (`docker/start-collector.sh`) |

Files in the image:

| Path | Content |
|---|---|
| `/opt/dc/lib` | jars on the module path |
| `/opt/dc/classpath` | jars on the class path |
| `/opt/dc/log4j2-logstash.xml` | the JSON log configuration (see [Logging](#logging)) |
| `/opt/dc/application.log` | the log file of the default log configuration, written at run time |
| `/opt/dc/lmdb` | the default location of the integrity check indexes, written at run time |

### Environment variables

| Variable | Default | Meaning |
|---|---|---|
| `JAVA_OPTS` | empty | Extra JVM options, for example `-Xmx2g` or `-Dhttp.port=9991`. |
| `PROXY_HTTP_HOST`, `PROXY_HTTP_PORT` | empty | Passed as `-Dhttp.proxyHost` and `-Dhttp.proxyPort`. |
| `PROXY_HTTPS_HOST`, `PROXY_HTTPS_PORT` | empty | Passed as `-Dhttps.proxyHost` and `-Dhttps.proxyPort`. |
| `ENABLE_JMX_REMOTE_DEBUGGING` | `false` in `Dockerfile`, `true` in `Dockerfile-dev` | `true` opens JMX remote on port 9992 (see [Metrics and profiling](#metrics-and-profiling)). |
| `LOGBACK_CONFIGURATION_FILE` | not set | Read by `Server`, which sets the system property `log4j.configurationFile` from it. See [Logging](#logging) for its effect. |
| `LOG4J_LEVEL` | not set | Log level of `io.descoped.dc` and of the root logger in the shipped log configurations. |
| `DC_<key>` | | Configuration keys (see [Configuration sources](#configuration-sources)). |

### Module path and class path

The server is a named module, `io.descoped.dc.server`. At image build time, `docker/SplitModulePath.java` splits the
jars from `target/dependency` and the server jar the way Maven Surefire runs the tests: the module path gets the root
module, every module it requires (also `requires static` when the jar is there), and the modules that provide the
services these modules `use`; every other jar moves to the class path directory. It prints
`Module path: N jars, class path: M jars`.

`/run.sh` starts the server like this (shown with JMX off):

```sh
java \
  --add-opens java.base/java.nio=lmdbjava \
  --add-exports java.base/sun.nio.ch=lmdbjava \
  --add-reads io.descoped.dc.core=ALL-UNNAMED \
  --add-opens io.descoped.dc.core/io.descoped.dc.core=ALL-UNNAMED \
  --add-reads io.descoped.dc.content.rawdata=io.descoped.dc.core \
  --add-modules ALL-MODULE-PATH \
  $JAVA_OPTS $PROXY_OPTS \
  -XX:+UseContainerSupport \
  -XX:+StartAttachListener -javaagent:/opt/dc/lib/byte-buddy-agent-<version>.jar \
  -p /opt/dc/lib -cp '/opt/dc/classpath/*' \
  -m io.descoped.dc.server/io.descoped.dc.server.Server
```

The Byte Buddy agent instruments the HTTP client and the content store for the Prometheus metrics.

### Building

From the root of the repository:

```sh
./mvnw -pl data-collector-server -am clean verify dependency:copy-dependencies -DskipTests
cd data-collector-server
docker build -t data-collector:local .                         # release Dockerfile
docker build -t data-collector:dev -f Dockerfile-dev .         # development image
```

`data-collector-server/build-dev.sh` does the Maven build and the `Dockerfile-dev` build in one step.
`data-collector-server/run-dev.sh` runs `data-collector:dev` with ports 9990 and 9992 and mounts `./certs` on
`/opt/dc/certs`.

### Releasing

Images are pushed only from release tags, by hand, with the owner's go-ahead; CI builds no image. From a checkout of
the release tag, build both platforms and push the version tag and `latest` (the version follows semantic versioning):

```sh
./mvnw -B -pl data-collector-server -am clean verify dependency:copy-dependencies -DskipTests
cd data-collector-server
docker login -u USERNAME
docker buildx build --no-cache --platform linux/amd64,linux/arm64 -f ./Dockerfile \
  -t descoped/data-collector:MAJOR.MINOR.PATCH -t descoped/data-collector:latest --push .
```

### Running

```sh
docker run -p 9990:9990 \
  -v "$PWD/conf:/conf" \
  -v "$PWD/certs:/certs" \
  descoped/data-collector:MAJOR.MINOR.PATCH
```

Put `application.properties` (and optionally `application-defaults.properties` and
`application-override.properties`) in `conf`. Mount a directory for whatever the content store writes (the examples
mount one on `/target`), and one for `data.collector.integrityCheck.database.location` if the indexes should outlive
the container.

## Running without Docker

`data-collector-server/build-and-run.sh` builds the server, copies the server jar into `target/dependency`, splits the
jars into `target/dependency` (module path) and `target/classpath` (class path) with `docker/SplitModulePath.java`,
and starts `io.descoped.dc.server.Server` in the foreground. It must be run from `data-collector-server`. It reads
`ENABLE_JMX_REMOTE_DEBUGGING` like the image. Its `java` command adds `--add-opens=java.base/java.nio=lmdbjava` and the
Byte Buddy agent, but not the other JPMS switches of `/run.sh`.

The working directory is `data-collector-server`, so the log file is `data-collector-server/application.log` and the
indexes go to `data-collector-server/lmdb`. Without `/conf`, the class path defaults apply: port 9990,
`content.stream.connector=rawdata`, `rawdata.client.provider=memory`.

For debugging in an IDE, `Server` has a constant `configurationOverride` (`NONE`). `DEBUG_USING_POSTGRES` adds the
class path resource `application-integration.properties` (PostgreSQL on `0.0.0.0:5432`, as started by
`data-collector-server/docker-compose.yml`) after the `/conf` files. `DEBUG_USING_GCS` adds
`examples/profile/gcs/conf/application.properties`, with `data.collector.certs.directory`,
`gcs.service-account.key-file` and `local-temp-folder` made absolute under `examples/`.

## Logging

The server logs through SLF4J to log4j2. `java.util.logging` is bridged to SLF4J at start-up.

The default configuration is `log4j2.xml` in the server jar:

| Logger | Level | Appenders |
|---|---|---|
| `io.descoped.dc` | system property `log4j2.level`, else environment variable `LOG4J_LEVEL`, else `DEBUG` | console, file |
| `io.descoped.rawdata` | `INFO` | console, file |
| root | system property `log4j2.level`, else `LOG4J_LEVEL`, else `INFO` | console, file |

The console appender writes a pattern with ANSI colours (`HH:mm:ss.SSS [thread] LEVEL logger - message`); the file
appender writes the same pattern to `application.log` in the working directory.

`/opt/dc/log4j2-logstash.xml` (from `src/main/resources/log4j2-logstash.xml`) writes every event to the console as one
compact JSON object per line (log4j2 `JsonLayout`, with the MDC, `timeMillis` and a field `timeZone` with the value
`UTC`). It has the same loggers and levels, without the file appender.

To select a configuration file, set the log4j2 system property in `JAVA_OPTS`:

```sh
docker run -e 'JAVA_OPTS=-Dlog4j2.configurationFile=/opt/dc/log4j2-logstash.xml' -p 9990:9990 data-collector:dev
```

`Server.main` also reads `LOGBACK_CONFIGURATION_FILE` and sets the system property `log4j.configurationFile` from it.
log4j2 is initialised before that, by the static logger of `Server`, and reads that property only when it initialises,
so setting `LOGBACK_CONFIGURATION_FILE` does not change the configuration in use. The `filesystem` and
`filesystem-secrets` Compose files of `examples/` pass `-Dlog4j2.configurationFile=/conf/log4j2.xml` in `JAVA_OPTS`
instead.

## Metrics and profiling

### Prometheus

`GET /metrics` returns the default Prometheus registry in the text format ([REST API](rest-api.md#metrics)). Counters
are written with the suffix `_total` (and a `_created` gauge); histograms as `_bucket`, `_count` and `_sum`.

| Metric | Type | Labels | Meaning |
|---|---|---|---|
| `dc_application_since` | gauge | | Server start, in epoch milliseconds |
| `dc_application_uptime` | gauge | | Registered; the server does not update it |
| `dc_client_request_started` | counter | `location` | HTTP requests started |
| `dc_client_request_completed` | counter | `location`, `statusCode`, `emptyResponse` | HTTP requests that returned (`statusCode` `-1` when there was no response) |
| `dc_client_request_failure` | counter | `location` | HTTP requests that threw |
| `dc_client_request_duration` | histogram | `location` | HTTP request duration in seconds |
| `dc_content_stream_pagination_page_publish_count` | counter | `topic` | Pages written to the content stream |
| `dc_content_stream_pagination_page_publish_duration` | histogram | `topic` | Time to write a page |
| `dc_content_stream_buffer_pagination_entry_count` | counter | `topic` | Page entries buffered |
| `dc_content_stream_buffer_entry_document_count` | counter | `topic` | Entry documents buffered |
| `dc_content_stream_pagination_entry_publish_position_count` | counter | `topic` | Positions published |
| `dc_content_stream_pagination_entry_publish_position_duration` | histogram | `topic` | Time to publish positions |

`location` is the scheme, host and port (when not 80 or 443) of the request URL, for example
`https://data.brreg.no`. Unless `prometheus.defaultExports.disabled=true`, the JVM and process metrics of the
Prometheus Java client (`simpleclient_hotspot` default exports) are registered as well.

`examples/docker-compose-postgres.yml` runs Prometheus (port 9090, `examples/prometheus.yml`, which scrapes
`data-collector-postgres:9990` every 15 seconds) and Grafana (port 3000).

### JMX and VisualVM

With `ENABLE_JMX_REMOTE_DEBUGGING=true` (the default in `Dockerfile-dev`), the JVM opens JMX remote with these
options:

```text
-Dcom.sun.management.jmxremote=true
-Dcom.sun.management.jmxremote.port=9992
-Dcom.sun.management.jmxremote.rmi.port=9992
-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.local.only=false
-Djava.rmi.server.hostname=localhost
```

JMX then has no authentication and no TLS: anyone who reaches port 9992 can use it. The release image has it off.

To profile with [VisualVM](https://visualvm.github.io/):

1. Build and start the dev image, for example `make build-data-collector-dev-image` and `make start-filesystem-dev` in
   `examples/`. The Compose files publish port 9992.
2. In VisualVM, add a JMX connection to `localhost:9992`.
3. Submit a task, for example `make collect-enheter-test`.

Without Docker, run `ENABLE_JMX_REMOTE_DEBUGGING=true ./build-and-run.sh` in `data-collector-server`, or start
`io.descoped.dc.server.Server` from an IDE with VisualVM attached.

## Integrity check and recovery

Both are driven over HTTP; the endpoints are in [REST API: integrity check](rest-api.md#integrity-check) and
[REST API: recovery](rest-api.md#recovery).

### Integrity check

`PUT /check-integrity/{topic}` deletes `<location>/<topic>` if it exists, opens a new LMDB environment there, and reads
the topic through the server's content store from its first message. It stores one key per message: the position
and the message id (UUIDv7). It ends when it reads the topic's last position, when no message arrives within
`data.collector.integrityCheck.consumer.timeoutInSeconds`, or when it is stopped with `DELETE`. It then writes:

| File | Content |
|---|---|
| `<location>/<topic>/report/<UUIDv7>.json` | JSON array: one object per position that occurs more than once, mapping the position to its message ids in time order |
| `<location>/<topic>/report/summary.json` | the summary |
| `<location>/<topic>/report/<topic>.json` | the summary with the duplicates, written on the first `GET /check-integrity/{topic}/full` |

`<location>` is `data.collector.integrityCheck.database.location` ([reference](#integrity-check-and-recovery-settings)); in
the image, by default, `/opt/dc/lmdb`, inside the container. The topic is used as a directory name as it is given in
the path. One check per topic runs at a time; checks of different topics run in parallel on the common fork-join pool.
The server keeps checks started since it started in memory; after a restart, only their directories are listed.

### Recovery

Recovery copies a topic to a new topic up to the first duplicated position that its index identifies (the rule is in
[REST API: recovery](rest-api.md#recovery)). Run an integrity check of the source topic first. The target topic is
written through a second content store, built from the same configuration with these changes:

- `rawdata.encryption.key` and `rawdata.encryption.salt` are removed, and the connector is told to skip encryption
  credentials, so encrypted messages are copied as they are, without being encrypted again.
- `local-temp-folder`, when set, gets the suffix `-recovery`.

After the copy, recovery reads the target topic from the time of the last copied message and writes what it finds to
`<location>/<target>/report/tail-report.json`.

## PostgreSQL

The rawdata-client `postgres` provider stores topics in PostgreSQL. The examples use PostgreSQL 18.

`examples/docker-compose-postgres.yml` (`make start-postgres`, `make start-postgres-dev`) runs:

| Service | Image | Host port | Notes |
|---|---|---|---|
| `dc-postgres-db` | `postgres:18.7-alpine` | 15432 | `POSTGRES_PASSWORD=postgres`, health check `pg_isready -U postgres`, volume `dc-postgres-test` on `/var/lib/postgresql`, `profile/postgres/init-db-and-user.sql` mounted as `/docker-entrypoint-initdb.d/init.sql` |
| `data-collector-postgres` | `$DC_IMAGE` | 9990, 9992 | profile `postgres`; starts after the database container |
| `dc-adminer` | `adminer:6.3.0` | 8980 | web database client; `make open-postgres-adminer` opens it |
| `prom` | `prom/prometheus:v3.16.0` | 9090 | scrapes the server |
| `grafana` | `grafana/grafana:13.3.1` | 3000 | |

`init-db-and-user.sql` creates the role and database the profile uses:

```sql
CREATE USER rdc WITH PASSWORD 'rdc';
CREATE DATABASE rdc OWNER rdc;
GRANT ALL PRIVILEGES ON DATABASE rdc TO rdc;
```

The PostgreSQL image runs it when it initialises an empty data volume. To run it against a running container, use
`examples/profile/postgres/docker-postgres-init-db.bash`. The rawdata-client provider creates its own tables when the
server starts.

The profile's connection keys (rawdata-client):

```properties
rawdata.client.provider=postgres
postgres.driver.host=dc-postgres-db
postgres.driver.port=5432
postgres.driver.user=rdc
postgres.driver.password=rdc
postgres.driver.database=rdc
rawdata.postgres.consumer.prefetch-size=100
rawdata.postgres.consumer.prefetch-poll-interval-when-empty=1000
```

`data-collector-server/docker-compose.yml` runs only PostgreSQL (`postgres:18.7-alpine` on port 5432, with the same
init script) and Adminer (port 8080), for a server started from the IDE with `DEBUG_USING_POSTGRES`.

## Encryption of rawdata content

The rawdata connector can encrypt the content of every message it writes. The key is derived from a password and a
salt with PBKDF2-HMAC-SHA256, 65 536 iterations, into a 256-bit AES key, as encryption-util 2.0.1 derived it, and
content is encrypted with encryption-util's `AES256` algorithm.

The password and salt are found like this:

| `rawdata.encryption.provider` | Password and salt |
|---|---|
| not set | `rawdata.encryption.key` and `rawdata.encryption.salt` from the configuration, when both are set |
| `dynamic-secret-configuration` or `dynamic-configuration`, without `rawdata.encryption.gcp.serviceAccountKeyPath` | `rawdata.encryption.key` and `rawdata.encryption.salt` from the configuration |
| `dynamic-secret-configuration` or `dynamic-configuration`, with `rawdata.encryption.gcp.serviceAccountKeyPath` | the keys `rawdata.encryption.key` and `rawdata.encryption.salt` in the properties file at that path |
| `google-secret-manager` | the secrets named by `rawdata.encryption.key` and `rawdata.encryption.salt` (both required) in the project `rawdata.encryption.gcp.projectId` (required), read with the key file `rawdata.encryption.gcp.serviceAccountKeyPath` or else Application Default Credentials |

Content is encrypted only when both a password and a salt are found and are not empty. When a secret is not found, or
a value is empty, the connector writes content unencrypted and does not fail. The start-up log line
`Rawdata encryption enabled: true|false` says only whether `rawdata.encryption.key` is set.

Example with the configuration (from the commented lines of the `postgres` profile):

```properties
rawdata.encryption.key=PASSPHRASE
rawdata.encryption.salt=SALT
```

Example with Google Secret Manager (from `examples/profile/filesystem-secrets/conf/application-override.properties.sample`):

```properties
rawdata.encryption.provider=google-secret-manager
rawdata.encryption.gcp.serviceAccountKeyPath=/certs/SA_KEY.json
rawdata.encryption.gcp.projectId=descoped-team
rawdata.encryption.key=<name of the secret with the password>
rawdata.encryption.salt=<name of the secret with the salt>
```

## Client certificates

Specifications that call services with client certificates (business certificates, Maskinporten) name a certificate
bundle in their `Security` configuration (see [Specification DSL](specification-dsl.md)). The server loads bundles
in one of two ways, each time a task starts.

### From a directory

Without `data.collector.sslBundle.provider`, the server scans `data.collector.certs.directory` (by default the working
directory) recursively for files named `secret.properties`. Each one makes a bundle named after its directory:

```text
/certs
├── ske-test-certs
│   ├── secret.properties
│   ├── testauth-key.pem
│   └── testauth-public.pem
└── ssb-test-certs
    ├── secret.properties
    └── ssb-test.p12
```

`secret.properties` is read with the secrets-client `safe-configuration` provider. File names are relative to the
bundle directory.

| Bundle | Keys |
|---|---|
| PEM | `private.key` (key file), `public.certificate` (certificate file), `secret.passphrase` |
| PKCS#12 | `archive.certificate` (archive file), `secret.passphrase` |

```properties
secret.passphrase=changeit
private.key=testauth-key.pem
public.certificate=testauth-public.pem
```

When a PEM or PKCS#12 bundle lacks `secret.passphrase` or one of its files, or a second bundle has the same name, the
server logs a warning, skips that bundle and stops scanning. The image declares `/certs` as a volume; set `data.collector.certs.directory=/certs`,
as the example profiles do.

### From Google Secret Manager

With `data.collector.sslBundle.provider=google-secret-manager`, the server reads one bundle from Google Secret Manager
instead, named `data.collector.sslBundle.name`. A specification uses it by that name. The keys are in the
[reference](#client-certificate-settings). Example from
`examples/profile/filesystem-secrets/conf/application-override.properties.sample`:

```properties
data.collector.sslBundle.provider=google-secret-manager
data.collector.sslBundle.gcp.serviceAccountKeyPath=/certs/SA_KEY.json
data.collector.sslBundle.gcp.projectId=descoped-team
data.collector.sslBundle.type=p12
data.collector.sslBundle.name=ssb-test-certs
data.collector.sslBundle.archiveCertificate=ssb-test-p12-certificate
data.collector.sslBundle.passphrase=ssb-test-p12-passphrase
```

For a PEM bundle, set `data.collector.sslBundle.type=pem` and name the secrets in
`data.collector.sslBundle.publicCertificate` and `data.collector.sslBundle.privateCertificate` instead of
`archiveCertificate`.

## The examples

`examples/` holds Compose files, configuration profiles and specifications. See [Examples](../examples/README.md).
The Makefile there passes `WORKDIR`, `PROFILE` and `DC_IMAGE` to `docker compose`; `make` alone lists the targets.

| Profile | Compose file | Content store | Mounts |
|---|---|---|---|
| `filesystem` | `docker-compose-filesystem.yml` | rawdata-client `filesystem` under `examples/target/avro` | `profile/filesystem/conf` on `/conf`, `certs` on `/certs`, `target` on `/target` |
| `filesystem-secrets` | `docker-compose-filesystem-secrets.yml` | as `filesystem`; certificates and encryption credentials from Google Secret Manager | `profile/filesystem-secrets/conf` on `/conf`, `profile/filesystem-secrets/certs` on `/certs`, `target` on `/target` |
| `gcs` | `docker-compose-gcs.yml` | rawdata-client `gcs` | `profile/gcs/conf` on `/conf`, `certs` on `/certs`, `target` on `/target` |
| `postgres` | `docker-compose-postgres.yml` | rawdata-client `postgres`, with PostgreSQL, Adminer, Prometheus and Grafana | `profile/postgres/conf` on `/conf`, `certs` on `/certs` |

Before use:

- `filesystem-secrets`: copy `profile/filesystem-secrets/conf/application-override.properties.sample` to
  `application-override.properties` in the same directory and fill it in; put the service-account key at
  `examples/profile/filesystem-secrets/certs/SA_KEY.json`.
- `gcs`: copy `profile/gcs/conf/application.properties.sample` to `application.properties` in the same directory and
  set `gcs.bucket-name`; put the service-account key at `examples/certs/gcs-secret/gcs_sa_test.json`.
- `postgres`, for the MoveIt specification: copy `profile/postgres/conf/application-sample_moveit.properties` to
  `application-override.properties` in the same directory and fill it in.

`application-override.properties`, `certs/` and other secret paths are ignored by git.

The container runs as uid 10001. On Linux, the bind-mounted directories it writes to (such as `examples/target`) must be
writable for that user or for group 0.

Makefile targets, by group (each `-dev` variant uses the local image `data-collector:dev`, the others
`descoped/data-collector:latest`):

| Group | Targets |
|---|---|
| Filesystem | `pull-filesystem`, `start-filesystem`, `start-filesystem-with-console`, `start-filesystem-using-secrets-with-console`, `tail-filesystem`, `stop-filesystem`, `remove-filesystem`, and the `-dev` variants (`start-filesystem-with-console-dev`, `start-filesystem-using-secrets-with-console-dev`, ...) |
| PostgreSQL | `pull-postgres`, `start-postgres`, `start-postgres-with-console`, `tail-postgres`, `stop-postgres`, `stop-postgres-clean` (also removes the volumes, the database volume `dc-postgres-test` included), `remove-postgres`, `open-postgres-adminer`, and the `-dev` variants |
| GCS | `pull-gcs`, `start-gcs`, `tail-gcs`, `stop-gcs`, `stop-gcs-clean`, `remove-gcs`, `open-gcs-adminer`, and the `-dev` variants |
| Dev image | `build-data-collector-dev-image` (Maven build and `Dockerfile-dev`), `update-data-collector-dev-image` (`Dockerfile-dev` only) |
| Tasks | `collect-enheter-test`, `collect-enheter-download`, `collect-freg-playground`, `collect-freg-konsument-test`, `collect-freg-bulk-uttrekk-prod`, `collect-sirius-person-utkast`, `collect-sirius-person-fastsatt`, `collect-tvinn-test`, `collect-tvinn-api-test`, `collect-altinn-test`, `collect-moveit-test`: each sends `PUT /tasks` with a file from `specs/` |
| Operations | `list-tasks`, `cancel-task` (prints the `curl` command to cancel a task), `health`, `health-all`, `health-alive`, `health-ready` |

The `filesystem` stop targets run `docker compose down -v`.
