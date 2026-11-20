# Getting started

This guide takes you through a first run of the Data Collector server: build it, start it, run a specification against
a public API, follow the task, and check the topic it wrote. It ends with pointers to using dc as a library.

- [What you need](#what-you-need)
- [1. Build the server](#1-build-the-server)
- [2. Start the server](#2-start-the-server)
- [3. Run a specification](#3-run-a-specification)
- [4. Check the topic](#4-check-the-topic)
- [5. Stop the server](#5-stop-the-server)
- [Next steps](#next-steps)

## What you need

- JDK 17 or later. The Maven Wrapper (`./mvnw`) downloads Maven itself.
- Docker with Compose v2, for the image and the `examples/` setups.
- Network access to `https://data.brreg.no`, the public API the example specification reads.

All commands below start from the root of the repository unless they say otherwise.

## 1. Build the server

The server image is built from the jars Maven puts in `data-collector-server/target`. Build the server module and the
modules it depends on, and copy its dependencies:

```sh
./mvnw -pl data-collector-server -am clean verify dependency:copy-dependencies -DskipTests
```

Then build an image in `data-collector-server`. There are two Dockerfiles:

| Dockerfile | Typical tag | JMX remote on port 9992 |
|---|---|---|
| `Dockerfile` | the release image | off (`ENABLE_JMX_REMOTE_DEBUGGING=false`) |
| `Dockerfile-dev` | `data-collector:dev` | on (`ENABLE_JMX_REMOTE_DEBUGGING=true`), for VisualVM |

The `examples/` Makefile targets that end in `-dev` use the tag `data-collector:dev`, so build that one:

```sh
cd data-collector-server
docker build -t data-collector:dev -f Dockerfile-dev .
```

`data-collector-server/build-dev.sh` runs both steps (the Maven build and the `docker build` of `Dockerfile-dev`), and
`make build-data-collector-dev-image` in `examples/` does the same.

The dev image has JMX remote on, without authentication or TLS. See
[Administration: metrics and profiling](administration.md#metrics-and-profiling).

### Without Docker

`data-collector-server/build-and-run.sh` builds the server with Maven, splits the jars into a module path and a class
path, and starts the server in the foreground on port 9990:

```sh
cd data-collector-server
./build-and-run.sh
```

No `/conf` directory is read on a typical workstation, so the server runs with the defaults on its class path:
`content.stream.connector=rawdata` and `rawdata.client.provider=memory`. The memory provider keeps topics in memory
only; they are gone when the server stops. The log goes to the console and to `data-collector-server/application.log`,
and the integrity check index to `data-collector-server/lmdb`. The steps in sections 3 and 4 work the same way against
this server.

## 2. Start the server

The `examples/` directory holds configuration profiles and Compose files. The `filesystem` profile writes topics as Avro
files to `examples/target/avro/rawdata-store`, through the rawdata-client `filesystem` provider. Its configuration is
`examples/profile/filesystem/conf/application.properties`:

```properties
content.stream.connector=rawdata
data.collector.certs.directory=/certs

data.collector.worker.threads=20
data.collector.http.client.timeout.seconds=3600
data.collector.http.request.timeout.seconds=3600

rawdata.client.provider=filesystem
filesystem.storage-folder=/target/avro/rawdata-store
local-temp-folder=/target/avro/temp
```

Start it with the dev image:

```sh
cd examples
make start-filesystem-dev
make tail-filesystem-dev    # follow the log; Ctrl-C to stop following
```

`make start-filesystem-dev` runs `docker compose -f docker-compose-filesystem.yml up -d` with `DC_IMAGE=data-collector:dev`
and `PROFILE=filesystem`. The Compose file publishes ports 9990 (HTTP) and 9992 (JMX) and mounts
`profile/filesystem/conf` on `/conf`, `certs` on `/certs` and `target` on `/target`.

The same with plain `docker run`:

```sh
cd examples
docker run --rm -p 9990:9990 \
  -v "$PWD/profile/filesystem/conf:/conf" \
  -v "$PWD/target:/target" \
  data-collector:dev
```

The container runs as user 10001. On Linux, `examples/target` must be writable for that user (or for group 0), or the
filesystem provider cannot create its folders. On Docker Desktop, bind mounts are writable from the container.

Check that the server answers:

```sh
curl -i localhost:9990/ping            # 200, empty body
curl -i localhost:9990/health/ready    # 200 when the server is running, 503 otherwise
```

## 3. Run a specification

`examples/specs/enhetsregisteret-test-spec.json` pages through the public Enhetsregisteret API
(`https://data.brreg.no/enhetsregisteret/api/enheter`). It publishes each organisation as a position on the topic
`enhetsregister`, with the organisation number as the position. Its `globalState` sets `MAX_NUMBER_OF_ITERATIONS`,
which ends the run early, so the task finishes on its own.

Submit it with `PUT /tasks`:

```sh
cd examples
curl -X PUT -i localhost:9990/tasks -H 'content-type: application/json' -d @specs/enhetsregisteret-test-spec.json
```

The server answers `201 Created` with no body; the response does not carry the task id. A second `PUT` of the same specification while it runs is answered with `409 Conflict`:
the server runs one task per specification `id` (here `Enhetsregisteret`). `make collect-enheter-test` sends the same
request.

List the running tasks:

```sh
curl localhost:9990/tasks
```

```json
[ {
  "task-id" : "019aa1d2-7c3e-7b41-a1f0-5c2d9e8b3f60",
  "specification-id" : "Enhetsregisteret",
  "description" : "Collect enhetsregiseret"
} ]
```

Follow the task in the health report. While it runs, it has an entry `worker-<task-id>` with its request count, the
topic and the last published position:

```sh
curl localhost:9990/health
```

When the task ends, it leaves `GET /tasks` and its last report moves to the `worker-history` list in `/health`. To
stop it early, cancel it with its task id:

```sh
curl -X DELETE -i localhost:9990/tasks/019aa1d2-7c3e-7b41-a1f0-5c2d9e8b3f60
```

The content is under `examples/target/avro/rawdata-store`, in the layout of the rawdata-client filesystem provider.

## 4. Check the topic

The integrity check reads a topic from its first to its last position, indexes every message in an LMDB database, and
reports positions that were published more than once. Start it for the topic `enhetsregister`:

```sh
curl -X PUT -i localhost:9990/check-integrity/enhetsregister    # 201 Created
```

List the checks and read the summary of this one:

```sh
curl localhost:9990/check-integrity
curl localhost:9990/check-integrity/enhetsregister
```

```json
{
  "topic" : "enhetsregister",
  "status" : "CLOSED",
  "started" : "2026-11-20T09:20:11.204Z",
  "ended" : "2026-11-20T09:20:14.870Z",
  "since" : "0 days, 0 hours, 0 minutes, 9 seconds, 112 millis",
  "firstPosition" : "810034882",
  "lastPosition" : "999999999",
  "currentPosition" : "999999999",
  "checkedPositions" : 9980,
  "averageCheckedPositionsPerSecond" : 1108.89,
  "duplicatePositions" : 0,
  "affectedPositions" : 0
}
```

`status` is `RUNNING` while the check reads the topic and `CLOSED` when it is done. `GET
/check-integrity/enhetsregister/full` returns the summary with the list of duplicate positions and their message ids.
See [REST API: integrity check](rest-api.md#integrity-check) and
[Administration: integrity check and recovery](administration.md#integrity-check-and-recovery).

## 5. Stop the server

```sh
cd examples
make stop-filesystem-dev
```

This runs `docker compose ... down -v`. The topics stay in `examples/target`. The integrity check index lives in the
container (`/opt/dc/lmdb`, the default location) and is removed with it.

## Next steps

- Write your own specification: [Specification DSL](specification-dsl.md).
- Run specifications from your own Java application with a `Worker`, without the server:
  [Developer guide](developer-guide.md).
- Every endpoint of the server: [REST API](rest-api.md).
- Configuration keys, the Docker image, logging, metrics, PostgreSQL, encryption and client certificates:
  [Administration](administration.md).
- How the engine, the content streams and the server fit together: [Architecture](architecture.md).
- The other Compose setups (PostgreSQL, Google Cloud Storage, Google Secret Manager) and specifications:
  [Examples](../examples/README.md).
