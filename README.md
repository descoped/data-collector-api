# Data Collector

[![CI](https://github.com/descoped/data-collector-project/actions/workflows/ci.yml/badge.svg)](https://github.com/descoped/data-collector-project/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=descoped_data-collector-project&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=descoped_data-collector-project)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=descoped_data-collector-project&metric=coverage)](https://sonarcloud.io/summary/new_code?id=descoped_data-collector-project)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

Data Collector (dc) is a specification-driven HTTP crawler. A specification describes how to page through an API:
which requests to make, how to split each response into entries (XPath, jq, regular expressions), how to find the next
page, and what to publish. dc runs the specification and publishes each entry as a position on a content stream, which
the rawdata connector writes to [rawdata-client](https://github.com/descoped/rawdata-client-project) topics (PostgreSQL,
filesystem or Google Cloud Storage), optionally encrypted.

You can use dc in two ways:
- **As a server:** an HTTP server runs specifications as tasks (`PUT /tasks`). It also indexes a topic and reports its
  duplicate positions (the integrity check), and copies an indexed topic to a new topic (recovery). It ships as a
  Docker image.
- **As a library:** you build a specification with the Java DSL and run it with a `Worker` in your own application.

## Quick start

You need JDK 17 or later and Docker with Compose v2.

```sh
# Build the server and its development image (data-collector:dev)
cd data-collector-server && ./build-dev.sh

# Start it with the filesystem profile; content is written under examples/target
cd ../examples && make start-filesystem-dev

# Run a specification: the public Enhetsregisteret API (data.brreg.no)
curl -X PUT -i localhost:9990/tasks -H 'content-type: application/json' -d @specs/enhetsregisteret-test-spec.json
curl localhost:9990/tasks
curl localhost:9990/health

make stop-filesystem-dev
```

[Getting started](docs/getting-started.md) walks through this step by step, including the integrity check.

## Documentation

| Guide | Contents |
|---|---|
| [Getting started](docs/getting-started.md) | A first run, end to end |
| [Specification DSL](docs/specification-dsl.md) | Writing specifications: nodes, queries, expressions, security |
| [Architecture](docs/architecture.md) | Modules, the execution model, content streams, the server |
| [REST API](docs/rest-api.md) | Every endpoint of the server |
| [Administration](docs/administration.md) | Configuration reference, the Docker image, operations |
| [Developer guide](docs/developer-guide.md) | Building, testing, using dc as a library, writing connectors |
| [Examples](examples/README.md) | Compose setups, configuration profiles and real-world specifications |
| [Changelog](CHANGELOG.md) | What changed in each release |

## Modules

Every module is released together under the Maven group `io.descoped.dc`. Import `data-collector-bom` to align their
versions.

| Module | JPMS module | Role |
|---|---|---|
| [data-collector-api](data-collector-api) | `io.descoped.dc.api` | The specification DSL, the expression language, and the service interfaces |
| [data-collector-core](data-collector-core) | `io.descoped.dc.core` | The engine: `Worker`, node handlers, HTTP client, client certificates, Maskinporten JWT, metrics |
| [data-collector-connector-content-stream-discarding](data-collector-connector-content-stream-discarding) | `io.descoped.dc.content.discarding` | Content store `discarding`, the engine's default: keeps the last position, drops the content |
| [data-collector-connector-content-stream-rawdata](data-collector-connector-content-stream-rawdata) | `io.descoped.dc.content.rawdata` | Content store `rawdata`: content to rawdata-client topics, optionally encrypted |
| [data-collector-server-base](data-collector-server-base) | `io.descoped.dc.application` | The Undertow application: configuration, routing, CORS, health, Prometheus metrics |
| [data-collector-server](data-collector-server) | `io.descoped.dc.server` | The server: tasks, integrity check (LMDB) and recovery; the Docker image |
| [data-collector-testutils](data-collector-testutils) | (class path) | JUnit harness: embedded test server with mock data, test client |
| [data-collector-samples](data-collector-samples) | (class path) | Example specifications run as tests; not published |
| [data-collector-bom](data-collector-bom) | | Bill of materials for the published modules |

## Building

```sh
./mvnw verify                    # compile and test
./mvnw verify javadoc:javadoc    # plus Javadoc
./mvnw verify -P coverage        # JaCoCo report per module
```

The code is Java 17. CI builds and tests on JDK 17 and JDK 27. See the [developer guide](docs/developer-guide.md).

## History

Until 2.x, each module was a repository of its own. The repositories were merged into this one with their full history,
and the old repository addresses redirect here. Their tags carry the repository name as prefix, for example
`data-collector-api/2.0.4`.

## License

[Apache License 2.0](LICENSE)
