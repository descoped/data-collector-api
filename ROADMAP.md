# Roadmap

3.x is Data Collector's line from now on. 2.x is retired: it gets no further releases, and its repositories redirect
here. What has shipped is in the [changelog](CHANGELOG.md); this page is what comes next.

## 3.0.0: the migration (in progress)

3.0.0 makes dc fully operational again on the Descoped 3.x libraries, current dependencies and Java 17, with the
behaviour of 2.x.

Done:
- One repository and one Maven reactor; the old repositories redirect here.
- The Descoped libraries on 3.x (rawdata-client, dynamic-configuration, secrets-client, encryption-util,
  service-provider-api) and every other dependency at its latest version.
- CI on JDK 17 and 27.
- The server image for amd64 and arm64, running as a non-root user.
- Documentation in `docs/` and the examples in `examples/`.

Before the release:
- **Correctness:** the defects found in the 2.x code while it was migrated and documented, decided one by one.
  Examples:
  - response validators that ignore their body checks;
  - retry settings read from the wrong key;
  - request headers lost when a specification is read from JSON;
  - integrity check and recovery errors.
- **Security:** the rules in [`CLAUDE.md`](.claude/CLAUDE.md) that the 2.x code does not meet yet:
  - specification expressions evaluated in a sandbox with an explicit allow-list (this also unblocks commons-jexl3
    after 3.2.1), and no class loaded by a name from a specification;
  - authentication on every server endpoint, with CORS and JMX off by default;
  - TLS chains verified, never trusted by comparing certificates;
  - topic names and positions validated before they reach file paths;
  - encryption that fails closed when a key or secret is missing;
  - secrets and `Authorization` headers kept out of logs, exception messages and health output.
- **Build:** test coverage for every module in SonarCloud.
- **Release:** the artifacts on Maven Central and the image from the release tag.

## After 3.0.0

- **Java 27** for the engine and the server, including their concurrency. Until then the code stays on Java 17.

## Open from 2.x

Kept from the 2.x roadmap because they still apply:
- A generic builder deserializer. `NodeBuilderDeserializer` still reads every node type in a hand-written case.
- A task scheduler, to run a specification on a schedule. The server only runs tasks on request (`PUT /tasks`).
- Better test coverage.

Known issues listed in 2.x, not yet confirmed against 3.0:
- Correlation ids are not generated correctly.
- The last event list is not captured when an `HttpErrorException` is raised.
