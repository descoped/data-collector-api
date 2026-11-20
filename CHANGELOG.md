# Changelog

All notable changes to Data Collector are recorded here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.0.0] - Unreleased

The first release from this repository. Until 2.x, every module was a repository of its own; they are now one Maven
reactor with one version, and the old repositories redirect here. 3.0.0 runs dc on the Descoped 3.x libraries, current
dependencies and Java 17. dc's own behaviour stays as in 2.x, except for the changes the libraries force, listed under
**Breaking**.

### Breaking

- **Message ids are UUIDv7**, as rawdata-client 3 gives them:
  - `ContentStreamBuffer.ulid()` is now `id()` and returns a `UUID`;
  - `io.descoped.dc.api.ulid` is replaced by `io.descoped.dc.api.ids.TimeOrderedIds`, whose order is unsigned;
  - worker ids, correlation ids and integrity report ids are UUIDv7 too. The integrity report keeps its `ulid` field
    name.
- **Storage:** rawdata-client 3 does not read data written by 2.x. The `postgres` provider uses three shared tables,
  and `filesystem` and `gcs` have a new segment layout.
- **Encrypted content** is written in encryption-util 3's ciphertext format 2, which encryption-util 2.0.1 cannot
  read. The key is still derived as in 2.x (PBKDF2-HMAC-SHA256, 65 536 iterations, a salt of any length).
- **Configuration is read once, at startup.** 2.x read environment variables and system properties again on every
  lookup.
- **Google credentials:** without a service-account key, the Google Secret Manager provider uses Application Default
  Credentials. 2.x used Compute Engine credentials only.
- **Java 17** is the minimum.

### Changed

- **One reactor, one version.**
  - Every module is released together under `io.descoped.dc`, with a new BOM, `data-collector-bom`.
  - Artifact ids and JPMS module names are unchanged.
- **Descoped libraries on 3.x:**

  | Library | Version |
  |---|---|
  | service-provider-api | 3.0.1 |
  | dynamic-configuration | 3.0.1 |
  | secrets-client | 3.0.0 |
  | encryption-util | 3.0.1 |
  | rawdata-client | 3.0.1 |

  Every other dependency is at its latest version except two:
  - classgraph stays on 4.8.200, because later versions need Java 25;
  - commons-jexl3 stays on 3.2.1, because later versions deny the class access that specification expressions use.
- **Configuration** keeps the 2.x rules on dynamic-configuration 3:
  - every class path match is read;
  - every `DC_` environment variable and every system property adds a key;
  - values are literal;
  - only `true`, in any case, reads as true.

  The secrets-client 3 key names are mapped internally, so dc's configuration keys are unchanged.
- **Server image:**
  - Temurin 17 on Ubuntu, for `linux/amd64` and `linux/arm64`;
  - runs as user `dc` (uid 10001);
  - JMX remote is off unless `ENABLE_JMX_REMOTE_DEBUGGING=true`, which the dev image sets;
  - only runtime jars, with no test jars or mock API;
  - the jars are split between module path and class path the way the tests run;
  - the release image is built from `Dockerfile`.
- **PostgreSQL 18** in the compose setups. `init-db-and-user.sql` makes `rdc` the owner of its database, which
  PostgreSQL 15 and later require.
- **Documentation** is Markdown in `docs/`, revised for 3.0. The examples (compose setups, profiles, specifications)
  are in `examples/`.

### Removed

- The OkHttp client delegates. They were never registered, so they never ran.
- The Kafka targets and profile in the examples: rawdata-client 3 has no Kafka provider.
- The production variants of the example specifications, and the scripts tied to one organisation's infrastructure.
- The AsciiDoc documentation and its PlantUML build.
- The unused `docker/entrypoint.sh`, which the image never copied.

### Fixed

- `start-collector.sh`: setting an HTTPS proxy no longer drops the HTTP proxy.
- `-XX:+EnableJVMCI` is no longer passed. Temurin has no Graal compiler, and JDK 27 rejects the option.
- jffi's classes and its native libraries are the same version.
- `build-and-run.sh` loads LMDB's native bridge on macOS, including Apple Silicon.
- `worker.config/page-test.json` is in the current specification format.
- The filesystem example setups pass their log4j2 file with `-Dlog4j2.configurationFile` in `JAVA_OPTS`. They used
  to set `LOGBACK_CONFIGURATION_FILE` to a logback file, and that variable has no effect: log4j2 is already configured
  when the server reads it.

## 2.x

2.x was released per module from the separate repositories. Their tags are kept here with the repository name as
prefix, for example `data-collector-api/2.0.4`.
