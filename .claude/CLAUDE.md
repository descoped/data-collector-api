# CLAUDE.md

Guidance for Claude Code in this repository.

## What this is

Data Collector: a specification driven HTTP crawler. A specification is a tree of nodes (get, post, paginate, sequence,
parallel, queries with jq, XPath and regex, publish) that pages through an API and publishes each entry as a
position on a content stream, which the rawdata connector writes to rawdata-client topics. The server runs
specifications as tasks over HTTP. One Maven reactor, group `io.descoped.dc`, owned by Descoped.

| Module | JPMS module | Role |
|---|---|---|
| `data-collector-project` (root) | n/a | parent and aggregator |
| `data-collector-bom` | n/a | BOM of the published modules |
| `data-collector-api` | `io.descoped.dc.api` | the specification DSL (`Builders`, `node`, `node.builder`), the expression language (JEXL), and the service interfaces for HTTP, content stores (`ContentStoreInitializer`) and health |
| `data-collector-core` | `io.descoped.dc.core` | the engine: `Worker`, the node handlers, the `java.net.http` client, client certificates (BusinessSSL), Maskinporten JWT, metrics |
| `data-collector-connector-content-stream-discarding` | `io.descoped.dc.content.discarding` | content store provider `discarding`, the engine's default: keeps the last published position in memory and drops the content |
| `data-collector-connector-content-stream-rawdata` | `io.descoped.dc.content.rawdata` | content store provider `rawdata`: content to rawdata-client topics, optionally encrypted |
| `data-collector-server-base` | `io.descoped.dc.application` | the Undertow application: service discovery, routing, CORS, health, Prometheus metrics |
| `data-collector-server` | `io.descoped.dc.server` | the server: tasks (`PUT /tasks`), integrity check (LMDB) and recovery of topics |
| `data-collector-testutils` | n/a (class path) | JUnit harness: embedded test server with mock data, test client, configuration profiles |
| `data-collector-samples` | n/a | example specifications run as tests; not published |

`docs/` holds the documentation (Markdown; diagrams in Mermaid) and `examples/` the compose setups, configuration
profiles and real-world specifications; neither is a Maven module. `ExampleSpecificationsTest` (data-collector-api)
reads and builds every `examples/specs/*.json`, so a DSL change that breaks an example fails the build.
`CHANGELOG.md` (Keep a Changelog) records every user-visible change: a pull request that changes behaviour,
configuration, the image or the API adds its entry under the unreleased version. Documentation that a change makes
wrong is updated in the same pull request.

Until 2026-11 every module was a repository of its own; their histories were imported here with `git filter-repo`
(old tags prefixed with the repository name, e.g. `data-collector-api/2.0.4`). 3.0.0 is the first version built from
this repository. The plan and its order of work are in the owner's notes; the work goes PR by PR.

## Invariants (never violate)

- **Java 17 baseline** (`maven.compiler.release` 17): every class file is Java 17 whatever JDK builds it. CI builds and
  tests on JDK 17 and JDK 27 (`ci.yml`). Nothing above Java 17 in main or test code until the owner moves the engine
  and server to JDK 27 (planned, after the migration).
- **3.0.0 keeps the behaviour of 2.x.** The migration makes dc work on the 3.x libraries, current dependencies and
  JDK 17; it changes no feature. Where a library changed its rules, dc keeps its own: configuration is read through
  `io.descoped.dc.application.server.Configurations` (every class path match, environment variables and system
  properties add keys, values are literal), the content key is derived as encryption-util 2.0.1 did, and the
  `discarding` connector is the engine's default. A behaviour change needs the owner's decision first.
- **3.0.0 is a clean break.** No backward-compatibility shims, deprecated aliases, data-migration code or migration
  guides for 2.x.
- **One reactor, one version.** Every module inherits the root POM and is released with it; modules depend on each
  other with `${project.version}`, never on released 2.x versions. Dependency versions are properties in the root POM,
  not `dependencyManagement`, so the BOM manages the data collector modules only.
- **The Descoped libraries move together.** service-provider-api, dynamic-configuration, secrets-client,
  encryption-util and rawdata-client are on 3.x; a later major moves all of them in one pull request (rawdata-client 3
  needs service-provider-api 3 under the JPMS module name 2 used). Renovate groups their majors. Their providers
  (rawdata `filesystem`, `gcs`, `postgres`; secrets `safe-configuration`, `dynamic-configuration`,
  `google-secret-manager`) are `runtime` dependencies, never `requires`d: the API modules declare `uses`.
- **Ids are UUIDv7** (`io.descoped.dc.api.ids.TimeOrderedIds`), as rawdata-client 3 gives messages. They are ordered
  with `TimeOrderedIds.ORDER` (unsigned), never `UUID.compareTo`.
- **JPMS.** Every module except testutils is a named module. A module whose exported API exposes another module's types
  `requires transitive` it. No `requires` a module does not use. Services are registered with `provides` in
  `module-info.java` and in `META-INF/services`.
- **Lint.** `-Xlint:all -Werror` and Javadoc doclint `all` with warnings failing are the rule, with standing
  exceptions: `-classfile` (dependencies' class files that reference annotation classes they do not ship) and, on JDK 21
  and later only (profile `jdk21-lint`; javac 17 rejects the name), `-this-escape`, so the 2.x classes keep their
  constructors and stay open for subclassing. No annotation processors run (`-proc:none`). A module not yet revised
  lists the categories it still breaks in `lint.exclusions` / `doclint.exclusions` in its POM; its revision empties
  them, and nothing is added to them. Only categories javac 17 knows may be listed there.
- **Undertow 2.4.x** stays the HTTP server for 3.0.0 (owner's decision, 2026-11-18).
- **Security.** Specification expressions are evaluated only in a sandbox with an explicit allow-list; classes named in
  a specification are never loaded by name; every server endpoint requires authentication, and CORS and JMX are off by
  default; TLS chains are verified, never trusted by comparing certificates; topic names and positions are validated and
  never spliced into file paths, SQL or object names; encryption fails closed when a key or secret is missing; secrets,
  `Authorization` headers and configuration values never reach logs, exception messages or health output. (Known
  violations in the 2.x code are being fixed PR by PR; new code must not add any.)
- **Always the latest stable version** of every library, plugin, action, base image and engine (PostgreSQL 18), except
  where a rule above or in `renovate.json` holds one back. Pre-releases only on request. Old and outdated code and
  configuration is removed, not carried along.
- **Releases only with the owner's go-ahead.** Only releases are published, by hand, through the Central Portal
  (profiles `central,sign`); no SNAPSHOTs, by CI or by hand. CI publishes nothing. Container images are pushed only
  from release tags. Maven 3.10 sends a server's credentials only to the hosts it knows for that id, so the server
  `central` in `~/.m2/settings.xml` must list `https://central.sonatype.com` in `repositoryOrigins`.
- **SonarCloud**: one project, `descoped_data-collector-project`, for the whole reactor.

## Testing

- Tests are JUnit 6, compiled into their module (patch mode); test compilation relaxes only `-Xlint:exports` and
  `missing-explicit-ctor`, because test classes are not API.
- Most tests run against embedded servers from data-collector-testutils on localhost. The samples that need real
  sources (Maskinporten, business certificates, SKE, Altinn, Toll) are disabled; `SimpleEnhetregisterUpdatesTest`
  calls the public data.brreg.no API.
- When a change switches on tests that never ran, run the suite about ten times before pushing
  (`for n in $(seq 1 10); do ./mvnw -B -q verify || break; done`), and disable a test only with its root cause in a
  comment.

## Maven Wrapper

- `./mvnw` pins Maven (`.mvn/wrapper/maven-wrapper.properties`, type `only-script`, so no jar in the repository) and
  checks the download against `distributionSha256Sum`. CI runs `./mvnw`. When Maven is bumped, by Renovate or by hand,
  set `distributionSha256Sum` to the SHA-256 of the new `apache-maven-<version>-bin.zip`, after checking that zip
  against the `.sha512` Maven publishes next to it. A stale checksum fails the build, it never skips the check.

## Commands

```sh
./mvnw verify                    # compile with -Xlint:all -Werror, tests
./mvnw verify javadoc:javadoc    # plus Javadoc with doclint, warnings fail the build
./mvnw verify -P coverage        # JaCoCo report per module in <module>/target/site/jacoco
```
