# Examples

Ready-made setups for running the Data Collector server with Docker Compose, and real-world specifications to run on
it. The `Makefile` drives both: it starts a setup, submits a specification, and checks the server.

- [Requirements](#requirements)
- [Setups](#setups)
- [Profiles](#profiles)
- [Specifications](#specifications)
- [Make targets](#make-targets)

For how the server is configured, see [Administration](../docs/administration.md). For the specification format, see
[Specification DSL](../docs/specification-dsl.md).

## Requirements

- Docker with Compose v2.
- An image of the server. Each setup has two sets of targets:
  - targets ending in `-dev` use a local image, `data-collector:dev`, built from this repository with
    `make build-data-collector-dev-image` (JDK 17 or later);
  - the others use the released image, `descoped/data-collector:latest`.
- Client certificates in `certs/`, for the specifications that use business SSL. Each bundle is a directory of its
  own, and `certs/` is ignored by Git:

  ```
  certs/
  ├── ske-test-certs/
  │   ├── secret.properties
  │   ├── testauth-key.pem
  │   └── testauth-public.pem
  └── toll-test-certs/
      ├── secret.properties
      ├── testauthcert-key.pem
      └── testauthcert-public.pem
  ```

Run `make` to list every target. Run only one setup at a time: they all use port 9990.

## Setups

| Setup | Compose file | Content is written to | Notes |
|---|---|---|---|
| Filesystem | `docker-compose-filesystem.yml` | `target/avro/` in this folder | The simplest setup for trying dc. |
| Filesystem with Google Secret Manager | `docker-compose-filesystem-secrets.yml` | `target/avro/` | Client certificates and the content encryption key come from Google Secret Manager. Copy `profile/filesystem-secrets/conf/application-override.properties.sample` to `application-override.properties` and fill it in. Put the service account key where it points (`/certs/SA_KEY.json`, mounted from `profile/filesystem-secrets/certs/SA_KEY.json`). |
| PostgreSQL | `docker-compose-postgres.yml` | PostgreSQL 18 (`rdc` database, port 15432) | Also starts adminer (port 8980), Prometheus (9090) and Grafana (3000). |
| Google Cloud Storage | `docker-compose-gcs.yml` | A GCS bucket | Copy `profile/gcs/conf/application.properties.sample` to `application.properties`, set `gcs.bucket-name`, and put the service account key in `certs/gcs-secret/gcs_sa_test.json`. |

Each compose file mounts `profile/<profile>/conf` as `/conf` and `certs/` as `/certs`, except the Google Secret Manager
setup, which mounts `profile/filesystem-secrets/certs` as `/certs`. The two filesystem setups also
pass `-Dlog4j2.configurationFile=/conf/log4j2.xml` in `JAVA_OPTS`, which logs at `TRACE`.

## Profiles

`profile/<name>/conf/application.properties` is the server's configuration for a setup. An
`application-override.properties` next to it (ignored by Git) overrides single keys.

| Profile | Rawdata provider | Notes |
|---|---|---|
| `filesystem` | `filesystem` | Avro segments under `/target/avro`. |
| `filesystem-secrets` | `filesystem` | Client certificates (`data.collector.sslBundle.*`) and encryption credentials (`rawdata.encryption.*`) from Google Secret Manager. |
| `postgres` | `postgres` | `init-db-and-user.sql` creates the `rdc` user and database the first time the database container starts. `docker-postgres-init-db.bash` runs it again by hand. `application-sample_moveit.properties` is a template for the MoveIt specification. |
| `gcs` | `gcs` | A sample only; see the table above. |

## Specifications

`specs/` holds specifications for real APIs, in the JSON format the server accepts. Most of them were written by the
tests in [data-collector-samples](../data-collector-samples), which build each specification with the Java DSL and
serialize it here.

| Specification | Source | Needs |
|---|---|---|
| `enhetsregisteret-test-spec.json` | Brønnøysundregistrene, Enhetsregisteret (public) | Nothing: a good first run |
| `enhetsregisteret-updates-spec.json` | Enhetsregisteret updates (public) | Nothing |
| `enhetsregisteret-download-spec.json` | Enhetsregisteret full download (public, large) | Nothing |
| `ske-freg-playground-spec.json` | Skatteetaten, Folkeregisteret playground | Business SSL (`ske-test-certs`) |
| `ske-freg-konsument-test-spec.json` | Skatteetaten, Folkeregisteret consumer test | Business SSL (`ske-test-certs`) |
| `ske-freg-bulk-uttrekk-spec.json` | Skatteetaten, Folkeregisteret bulk extract | Business SSL (`ske-p12-certs`) |
| `ske-freg-brsv-bulk-uttrekk-spec.json`, `ske-freg-brsv-konsument-test-spec.json` | Skatteetaten, Folkeregisteret (Svalbard) | Maskinporten, signed with a business certificate (`ske-p12-certs`, `ssb-test-certs`) |
| `ske-sirius-person-utkast-spec.json`, `ske-sirius-person-fastsatt-spec.json` | Skatteetaten, Sirius | Business SSL (`ske-test-certs`) |
| `toll-tvinn-test-spec.json` | Tolletaten, Tvinn | Maskinporten, signed with a business certificate (`ssb-test-certs`) |
| `toll-tvinn-api-test-spec.json` | Tolletaten, Tvinn | Business SSL (`toll-test-certs`) |
| `altinn3-test-spec.json` | Altinn 3 | Maskinporten, signed with a business certificate (`ssb-test-certs`) |
| `moveit-test-spec.json` | A MoveIt Automation server | Credentials in `application-override.properties` |

These APIs require an agreement with their owners, except the Enhetsregisteret ones.

## Make targets

**Lifecycle:** for a setup `<s>` (`filesystem`, `postgres`, `gcs`):

| Target | Description |
|---|---|
| `start-<s>` / `start-<s>-dev` | Start in the background |
| `start-<s>-with-console` | Start in the foreground (`filesystem` and `postgres`) |
| `start-filesystem-using-secrets-with-console` | Start the filesystem setup with Google Secret Manager |
| `tail-<s>` / `tail-<s>-dev` | Follow the logs |
| `stop-<s>` / `stop-<s>-dev` | Stop. `stop-filesystem` also removes anonymous volumes. |
| `stop-<s>-clean` | Stop and remove the volumes (`postgres`, `gcs`); for `postgres` this deletes the database |
| `remove-<s>` | Remove the stopped containers |
| `pull-<s>` | Pull the images |
| `open-postgres-adminer` | Open adminer in the browser |
| `build-data-collector-dev-image` | Build the server and `data-collector:dev` |
| `update-data-collector-dev-image` | Rebuild `data-collector:dev` without building the server again |

**Specifications:** each one submits a specification with `PUT /tasks`. The targets are `collect-enheter-test`,
`collect-enheter-download`, `collect-freg-playground`, `collect-freg-konsument-test`,
`collect-freg-bulk-uttrekk-prod`, `collect-sirius-person-utkast`, `collect-sirius-person-fastsatt`,
`collect-tvinn-test`, `collect-tvinn-api-test`, `collect-altinn-test` and `collect-moveit-test`.

**Server:**

| Target | Description |
|---|---|
| `list-tasks` | `GET /tasks` |
| `cancel-task` | Prints the `DELETE /tasks/<id>` command to run |
| `health`, `health-all`, `health-alive`, `health-ready` | The health endpoints |

The content written to rawdata topics can be read with
[rawdata-client](https://github.com/descoped/rawdata-client-project) and the provider settings in the profile.
