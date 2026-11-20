# Developer guide

How to build and test Data Collector (dc), use it as a library, and extend it with a content store connector or a
server controller. For how dc is put together, read [Architecture](architecture.md) first. The specification format is
in [Specification DSL](specification-dsl.md).

- [Building](#building)
- [Testing](#testing)
- [Using dc as a library](#using-dc-as-a-library)
- [The HTTP client](#the-http-client)
- [Writing a content store connector](#writing-a-content-store-connector)
- [Extending the server](#extending-the-server)

## Building

### Requirements

- A JDK, 17 or later. The build compiles with `release 17`, so the class files are Java 17 on any JDK. The enforcer
  plugin fails the build on a Java older than 17.
- Maven comes with the Maven Wrapper, `./mvnw`. The enforcer plugin requires Maven 3.6.3 or later and below 4.
- Docker with Compose v2, only to build and run the server image. See [Getting started](getting-started.md).

### Commands

```sh
./mvnw verify                    # compile with -Xlint:all -Werror, run the tests
./mvnw verify javadoc:javadoc    # also build the Javadoc with doclint; warnings fail the build
./mvnw verify -P coverage        # also write a JaCoCo report per module, in <module>/target/site/jacoco
```

To build one module and what it needs, use `-pl <module> -am`, for example
`./mvnw -pl data-collector-core -am verify`.

### Maven Wrapper

`./mvnw` downloads the Maven version pinned in `.mvn/wrapper/maven-wrapper.properties` (type `only-script`, so there is
no wrapper jar in the repository). It checks the download against `distributionSha256Sum`; a wrong checksum fails the
build. When Maven is bumped, `distributionSha256Sum` is set to the SHA-256 of the new `apache-maven-<version>-bin.zip`,
after that zip is checked against the `.sha512` file Maven publishes next to it.

### Lint and Javadoc rules

- javac runs with `-Xlint:all,-classfile -Werror`. `-classfile` is off because dependencies' class files reference
  annotation classes they do not ship. On JDK 21 and later, the profile `jdk21-lint` also turns off `this-escape`, so
  the 2.x classes keep their constructors and stay open for subclassing. javac 17 does not know that category.
- No annotation processors run (`<proc>none</proc>`).
- Javadoc runs with doclint `all` and `failOnWarnings`.
- A module that is not yet revised lists the lint categories it still breaks in the properties `lint.exclusions` and
  `doclint.exclusions` in its POM. A revision of the module empties them; nothing is added to them. Only categories
  that javac 17 knows may be listed.
- Test classes are compiled with two more lint categories off: `exports` and `missing-explicit-ctor`. Test classes are
  not API.

### Continuous integration

`.github/workflows/ci.yml` runs on pushes to `master`, release tags, pull requests to `master`, and by hand. It builds
on JDK 17 and JDK 27 (Temurin) with `./mvnw --batch-mode --no-transfer-progress verify javadoc:javadoc`. On JDK 17,
for pushes and for pull requests from this repository, it adds the `coverage` profile and a SonarCloud analysis
(project `descoped_data-collector-project`, profile `sonar`); the analysis does not fail the build. CI publishes
nothing.

## Testing

### Module tests

Tests are JUnit 6, run by Maven Surefire. In the named modules, the tests are compiled into the module (patch mode).
`data-collector-testutils` and `data-collector-samples` are not JPMS modules; their tests run on the class path.

The core, server and samples modules run Surefire with `-XX:+EnableDynamicAgentLoading`, so that tests may install the
ByteBuddy agent at run time (`MetricsAgent.premain(null, ByteBuddyAgent.install())`); core's metrics tests do. The
server module also passes
`--add-opens`, `--add-reads` and `--add-exports` switches; see its POM.

Most tests run a `Worker` against an embedded server from `data-collector-testutils` on localhost. A test chooses the
content store with configuration, for example `Map.of("content.stream.connector", "discarding")` or
`Map.of("content.stream.connector", "rawdata", "rawdata.client.provider", "memory")`.

When a change switches on tests that never ran, run the suite about ten times before pushing:

```sh
for n in $(seq 1 10); do ./mvnw -B -q verify || break; done
```

A test is disabled only with its root cause in a comment.

### The test harness (data-collector-testutils)

`data-collector-testutils` is a class path library (no `module-info.java`). It starts an `UndertowApplication` from
`data-collector-server-base` as a test server, with the controllers, services and components found on the test class
path.

| Class | Package | Role |
|---|---|---|
| `TestServerExtension` | `io.descoped.dc.test.server` | JUnit extension. Starts the test server for a test, injects it, and resolves parameters. |
| `TestServerExecutionListener` | `io.descoped.dc.test.server` | JUnit Platform `TestExecutionListener`, registered in `META-INF/services/org.junit.platform.launcher.TestExecutionListener`. Before the tests run, it reads `@ConfigurationProfile` and `@ConfigurationOverride` on test methods and prepares one server configuration per binding. |
| `TestServer` | `io.descoped.dc.test.server` | The embedded server. `testURL(uri)` returns an absolute URL on it; `getConfiguration()` and `getApplication()` give the configuration and the `UndertowApplication`. |
| `TestClient` | `io.descoped.dc.test.client` | A `java.net.http` client for the test server: `get`, `put`, `post`, `postJson`, `postForm`, `delete`, `head`, `options`. Each returns a `ResponseHelper` with `expect200Ok()`, `expect201Created()`, `expect204NoContent()`, `expect400BadRequest()`, `expect404NotFound()`, `expectAnyOf(...)` and `body()`. |
| `ConfigurationProfile` | `io.descoped.dc.test.config` | Method annotation: also read `application-<value>.properties` (default `test`). |
| `ConfigurationOverride` | `io.descoped.dc.test.config` | Annotation with key and value pairs that override the configuration. Only method-level annotations are read. |
| `MockDataController` | `io.descoped.dc.test.controller` | The mock data API on `/api`, registered in `META-INF/services/io.descoped.dc.application.spi.Controller`. |

A test class uses the extension and gets the server and client by field injection (`javax.inject.Inject`) or as test
method parameters:

```java
import io.descoped.dc.test.client.TestClient;
import io.descoped.dc.test.server.TestServer;
import io.descoped.dc.test.server.TestServerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

@ExtendWith(TestServerExtension.class)
class EventsTest {

    @Inject
    TestServer server;

    @Inject
    TestClient client;

    @Test
    void thatEventsAreListed() {
        client.get("/api/events?position=1&pageSize=10").expect200Ok();
    }
}
```

The configuration of a test server is built from these sources, a later one winning:

1. `application-defaults.properties` and `application-test.properties` from the class path (every match);
2. `application-<profile>.properties` for the `@ConfigurationProfile` on the test method;
3. the pairs of `@ConfigurationOverride` on the test method;
4. system properties;
5. environment variables with the prefix `DC_`, without the prefix.

Tests without a profile or override share one server for the whole run. A test method with a profile or override gets
a server of its own. Each server listens on a free port between 9000 and 9499.

The mock data API (`MockDataController`):

| Request | Returns | Query parameters |
|---|---|---|
| `GET /api/events` | A page of events, as JSON or XML by the `Accept` (or `Content-Type`) header; JSON by default. The page links to the next page. | `position` (default 1), `pageSize` (50), `stopAt` (25; `-1` for no end), `gzip`, `retries` |
| `GET /api/events/<id>` | One event document. | `generateRandomEventItemData`, `generateEventItemRecordSize`, `generateEventItemRecordKeySize`, `generateEventItemRecordElementSize`, `failWithStatusCode`, `failAt`, `404withResponseError` |
| `GET /api/atom` | An Atom feed page. | `marker` (default 1000; `first` means 1000), `pageSize` (50), `stopAt` (1025) |
| `POST /api/authorize` | `{"access_token": "SECRET"}` for a form URL encoded body. The credentials are ignored. | |

A module's tests can add controllers, services and components of their own: list them in
`src/test/resources/META-INF/services/io.descoped.dc.application.spi.Controller` (or `...spi.Service`,
`...spi.Component`). The core tests register `TestController`, `LoopbackController` and `TestService` that way.

### Samples

`data-collector-samples` holds example specifications for real APIs, run as tests, in five submodules:
`altinn3-test-worker`, `enhetsregisteret-worker`, `moveit-test-worker`, `ske-sirius-worker` and `toll-tvinn-worker`.
The module is not published (`maven.deploy.skip`, `maven.install.skip`, and excluded from the Central upload).

The samples that need real sources are `@Disabled`: they need Maskinporten, business certificates, or access to
Skatteetaten, Altinn, Toll or MoveIt. Some read credentials from an `application-override.properties` that is not in
the repository. The enabled ones:

- `SimpleEnhetregisterUpdatesTest` calls the public `data.brreg.no` API, so it needs network access.
- `SimpleFregKomplettUttrekkTest.match404ErrorMessage` and `ValidateSiriusDataTest.testDbRepository` run locally.

Several tests in `data-collector-core` and `data-collector-server` are also `@Disabled`.

## Using dc as a library

### Dependencies

Import the BOM and depend on core and the connectors you use. Releases are on Maven Central; SNAPSHOTs are not
published.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.descoped.dc</groupId>
            <artifactId>data-collector-bom</artifactId>
            <version>${data-collector.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.descoped.dc</groupId>
        <artifactId>data-collector-core</artifactId>
    </dependency>
    <dependency>
        <groupId>io.descoped.dc</groupId>
        <artifactId>data-collector-connector-content-stream-rawdata</artifactId>
    </dependency>
</dependencies>
```

`data-collector-core` brings `data-collector-api` with it. It does not bring a connector: add
`data-collector-connector-content-stream-discarding` for the `discarding` provider, which the `Worker` uses when no
connector is configured, or `data-collector-connector-content-stream-rawdata` for `rawdata`. The BOM does not manage
rawdata-client providers; add the one you use (for example `io.descoped.rawdata:rawdata-client-provider-filesystem`)
with the version of the root POM's `rawdata-client.version` property.

On the module path, `requires io.descoped.dc.core;` gives the API too (`requires transitive`). `io.descoped.dc.api`
does not declare `uses` for `ContentStoreInitializer`, so a connector module is resolved only when a module requires it
or it is added with `--add-modules`. The server's start script uses `--add-modules ALL-MODULE-PATH`.

### Running a specification

Build a specification with the DSL (see [Specification DSL](specification-dsl.md)), give it to
`Worker.newBuilder()`, and run it. This one pages through an XML feed, splits each page into entries, buffers each
entry and publishes it as a position:

```java
import io.descoped.dc.api.Specification;
import io.descoped.dc.api.content.ContentStore;
import io.descoped.dc.api.content.ContentStoreInitializer;
import io.descoped.dc.api.context.ExecutionContext;
import io.descoped.dc.api.node.builder.SpecificationBuilder;
import io.descoped.dc.core.executor.Worker;
import io.descoped.service.provider.api.ProviderConfigurator;

import java.util.Map;

import static io.descoped.dc.api.Builders.addContent;
import static io.descoped.dc.api.Builders.context;
import static io.descoped.dc.api.Builders.execute;
import static io.descoped.dc.api.Builders.get;
import static io.descoped.dc.api.Builders.nextPage;
import static io.descoped.dc.api.Builders.paginate;
import static io.descoped.dc.api.Builders.parallel;
import static io.descoped.dc.api.Builders.publish;
import static io.descoped.dc.api.Builders.regex;
import static io.descoped.dc.api.Builders.sequence;
import static io.descoped.dc.api.Builders.status;
import static io.descoped.dc.api.Builders.whenVariableIsNull;
import static io.descoped.dc.api.Builders.xpath;

public class Collect {

    public static void main(String[] args) throws Exception {
        SpecificationBuilder specification = Specification.start("events", "Collect events", "page-loop")
                .configure(context()
                        .topic("events")
                        .header("accept", "application/xml")
                        .variable("baseURL", "https://api.example.com")
                        .variable("nextPosition", "${contentStream.lastOrInitialPosition(1)}")
                )
                .function(paginate("page-loop")
                        .variable("fromPosition", "${nextPosition}")
                        .addPageContent("fromPosition")
                        .iterate(execute("page"))
                        .prefetchThreshold(5)
                        .until(whenVariableIsNull("nextPosition"))
                )
                .function(get("page")
                        .url("${baseURL}/events?position=${fromPosition}&pageSize=10")
                        .validate(status().success(200, 299).fail(300, 599))
                        .pipe(sequence(xpath("/feed/entry"))
                                .expected(xpath("/entry/id"))
                        )
                        .pipe(nextPage()
                                .output("nextPosition", regex(xpath("/feed/link[@rel=\"next\"]/@href"), "(?<=[?&]position=)[^&]*"))
                        )
                        .pipe(parallel(xpath("/feed/entry"))
                                .variable("position", xpath("/entry/id"))
                                .pipe(addContent("${position}", "entry"))
                                .pipe(publish("${position}"))
                        )
                        .returnVariables("nextPosition")
                );

        Map<String, String> configuration = Map.of(
                "content.stream.connector", "rawdata",
                "rawdata.client.provider", "memory");

        try (ContentStore contentStore = ProviderConfigurator.configure(ContentStoreInitializer.class,
                configuration.get("content.stream.connector"), configuration)) {
            ExecutionContext output = Worker.newBuilder()
                    .specification(specification)
                    .configuration(configuration)
                    .contentStore(contentStore)
                    .build()
                    .run();
        }
    }
}
```

`WorkerBuilder` also takes:

| Method | Effect |
|---|---|
| `specification(NodeBuilder)` | Runs a single node instead of a specification. The builder's simple class name is the specification id. |
| `header(name, value)` | A request header sent with every request, unless the specification context sets headers of its own; then only those are sent. |
| `variable(name, value)`, `globalState(key, value)`, `state(key, value)` | Initial context values. |
| `topic(String)` | Overrides the specification's topic. |
| `contentStore(ContentStore)` | The content store to use. Without it, the worker creates one and logs a warning. |
| `keepContentStoreOpenOnWorkerCompletion(boolean)` | Leaves the topic open when the run ends. By default the run closes the topic. |
| `workerObserver(WorkerObserver)` | Callbacks at start and finish. |
| `buildCertificateFactory(Path)`, `buildCertificateFactory(Path, String)` | Load client certificate bundles from a directory, and optionally pick one. See [Client certificates](#client-certificates). |
| `useBusinessSSLResourceSupplier(Supplier)` | Load one certificate bundle from a `BusinessSSLResource`. |
| `stopAtNumberOfIterations(Integer)` | Stop `parallel` after that many entries. Meant for tests. |
| `printExecutionPlan()`, `printConfiguration()` | Log the execution plan, or the specification as YAML. |

`run()` blocks and returns the output context. `runAsync()` returns a `CompletableFuture<ExecutionContext>`.
`terminate()` asks a running worker to stop; the run then ends with status `CANCELED`.

### Worker configuration

`WorkerBuilder.configuration(Map<String, String>)` sets the configuration. The worker and the handlers read these keys:

| Key | Default | Meaning |
|---|---|---|
| `content.stream.connector` | `discarding` | Provider name of the content store the worker creates when none is given. |
| `data.collector.worker.threads` | available processors + 2 | Threads in the worker's pool. |
| `data.collector.http.version` | `HTTP_2` | `HTTP_1_1` or `HTTP_2`. |
| `data.collector.http.followRedirects` | not set (the `java.net.http` default, never) | `NEVER`, `ALWAYS` or `NORMAL`. |
| `data.collector.http.client.timeout.seconds` | 20 | Connect timeout. |
| `data.collector.http.request.timeout.seconds` | 15 | Time to wait for a response. |
| `data.collector.http.request.retryCount` | 3 | Attempts per request. |
| `data.collector.http.request.napOnRetryInMillis` | 150 | Pause between attempts. When the key is set, the pause is read from the value of `data.collector.http.request.retryCount`. |
| `data.collector.parallel.queueBuffer.threshold` | not set | `parallel` waits for its running tasks after this many entries. |

The whole map is also passed to the content store provider, and expressions see it as `ENV`.

### Choosing a content store

The content store decides where published positions go:

- `discarding`: nothing is written. Use it for tests and dry runs.
- `rawdata` with `rawdata.client.provider` set to a rawdata-client provider (`memory`, `discard`, `filesystem`,
  `postgres`, `gcs`, if on the path) and that provider's configuration. Add `rawdata.encryption.*` keys to encrypt
  content; see [Administration](administration.md).

Create the store once with `ProviderConfigurator.configure(ContentStoreInitializer.class, name, configuration)`, pass
it to each worker with `contentStore(...)`, and close it when you are done. `ContentStore` is `AutoCloseable`.

### Metrics

The Prometheus metrics of the HTTP client and the content store come from ByteBuddy agents. Install them once, before
the first worker runs:

```java
import io.descoped.dc.core.metrics.MetricsAgent;
import net.bytebuddy.agent.ByteBuddyAgent;

MetricsAgent.premain(null, ByteBuddyAgent.install());
```

On JDK 21 and later, loading an agent at run time needs `-XX:+EnableDynamicAgentLoading` to run without a warning, or
start the JVM with `-javaagent:<byte-buddy-agent jar>`. The agent rewrites the `ContentStore` implementation to call
`io.descoped.dc.core.content.ContentStoreExporter`; on the module path, the connector module must then read
`io.descoped.dc.core`. The server's start script passes `--add-reads io.descoped.dc.content.rawdata=io.descoped.dc.core`.
The metric names are listed in [Architecture](architecture.md#metrics-and-monitoring).

## The HTTP client

### Abstractions

The API (`io.descoped.dc.api.http`) defines the client the engine uses:

| Type | Role |
|---|---|
| `Client` | `send`, `sendAsync` (each with an optional `BodyHandler`), `version()`. `Client.newClient()` and `Client.newClientBuilder()` load the implementation. |
| `Client.Builder` | `version`, `priority`, `authenticator`, `sslContext`, `sslParameters`, `x509TrustManager`, `executor`, `connectTimeout`, `followRedirects`, `proxy`. |
| `Request`, `Request.Builder` | URL, method (`GET`, `PUT`, `POST`, `DELETE`), headers, body (`byte[]` or `Flow.Publisher<ByteBuffer>`), `expectContinue`, `timeout`. `Request.newRequestBuilder()` loads the implementation. |
| `Response`, `Response.Builder` | URL, headers, status code, body as `byte[]`, the `BodyHandler` that received the body, the previous response. |
| `BodyHandler<T>` | A `Flow.Subscriber<List<ByteBuffer>>` that consumes the body and returns a result from `body()`. |
| `Headers` | Request and response headers. |

The three builders are services. `io.descoped.dc.api` declares `uses Client.Builder`, `uses Request.Builder` and
`uses Response.Builder`; the static factories call `ServiceLoader.load(...).findFirst()`.

### Implementation

`io.descoped.dc.core` provides them, with `provides` in `module-info.java` and files in `META-INF/services`:

- `HttpClientDelegate.ClientBuilder` builds an `HttpClientDelegate` around a `java.net.http.HttpClient`. The default
  version is HTTP/2. The `x509TrustManager` it is given is not used; the trust manager comes with the `SSLContext`.
- `HttpRequestDelegate.RequestBuilder` builds an `HttpRequestDelegate` around an `HttpRequest`.
- `HttpResponseDelegate.ResponseBuilder` wraps an `HttpResponse`.

Without a `BodyHandler` the body is read into a `byte[]`. With one, the client uses
`HttpResponse.BodyHandlers.fromSubscriber(bodyHandler)`, and the result is in `response.bodyHandler()`. The engine
does this for `get` followed by a token-based `sequence`: `TempFileBodyHandler` writes the body to a temporary file.

Multipart bodies (`bodyPublisher().formPart(...)`, `textPart(...)`) are built with Methanol's
`MultipartBodyPublisher`.

### Client certificates

A specification uses client certificates (business SSL) through a named bundle. The worker loads bundles into a
`CertificateFactory`, which builds a `CertificateContext` (an `SSLContext`, a trust manager and a key pair) per bundle.

Bundles come from one of two sources.

From a directory, with `WorkerBuilder.buildCertificateFactory(Path)`: the worker walks the directory for files named
`secret.properties`. Each such file defines one bundle, named after its directory. It is read with the secrets-client
provider `safe-configuration`, and holds these keys:

| Key | Bundle type | Value |
|---|---|---|
| `private.key` | PEM | File name of the private key, relative to the directory |
| `public.certificate` | PEM | File name of the certificate, relative to the directory |
| `archive.certificate` | PKCS#12 | File name of the archive, relative to the directory |
| `secret.passphrase` | both | Passphrase. Required: the walk stops at a bundle without it. |

A bundle with both `private.key` and `public.certificate` is PEM; one with `archive.certificate` is PKCS#12. When a
bundle name occurs twice, or a bundle has a missing key or file, the walk stops: bundles after it in the walk are not
loaded. The server uses the directory `data.collector.certs.directory`, or the working directory when the key is not
set. See [`examples/README.md`](../examples/README.md) for the layout used with Docker Compose.

From Google Secret Manager, with `WorkerBuilder.useBusinessSSLResourceSupplier(Supplier)`: the supplier returns a
`io.descoped.dc.api.security.BusinessSSLResource` (bundle name, type `pem` or `p12`, certificate, private key, archive,
passphrase). The server's implementation is `io.descoped.dc.application.ssl.SecretManagerSSLResource`, used when
`data.collector.sslBundle.provider` is set; it reads the `data.collector.sslBundle.*` keys. See
[Administration](administration.md).

A specification selects a bundle in two places:

- For the TLS connection: `security().sslBundleName("<bundle>")` in `configure(...)`, in JSON
  `{"type": "Security", "bundleName": "<bundle>"}`. The worker builds its `Client` with that bundle's `SSLContext`.
  `buildCertificateFactory(Path, String)` names the bundle from code instead. Without a bundle name, the client uses the
  JDK's default `SSLContext`.
- For a Maskinporten JWT grant: the JWT identity's header claim `sslBundleName`, set with
  `headerClaims().x509CertChain("<bundle>")`. `JwtTokenBodyPublisherProducerHandler` signs the grant with the bundle's
  private key (RS256) and puts the bundle's certificates in the `x5c` header.

The bundle's `SSLContext` uses TLSv1.2. Its trust manager first asks the JDK's default trust manager; when that rejects
the server's chain, it accepts the chain if the last certificate of the chain equals the bundle's own certificate.

## Writing a content store connector

A connector is a module that provides `io.descoped.dc.api.content.ContentStoreInitializer`.

### The SPI

`ContentStoreInitializer` extends `io.descoped.service.provider.api.ProviderInitializer<ContentStore>`:

- `Set<String> configurationKeys()`: the keys the provider requires. `ProviderConfigurator` fails with an
  `IllegalArgumentException` when one is missing. Optional keys are not listed. The default is an empty set.
- `ContentStore initialize(Map<String, String> configuration)`: creates the store. The map is unmodifiable and holds
  the whole configuration given to the worker or the server.

The class needs a public no-argument constructor and `@ProviderName("<name>")`. The name is what
`content.stream.connector` selects. Two providers with the same name are an error.

```java
import io.descoped.dc.api.content.ContentStore;
import io.descoped.dc.api.content.ContentStoreInitializer;
import io.descoped.service.provider.api.ProviderName;

import java.util.Map;
import java.util.Set;

@ProviderName("my-store")
public class MyContentStoreInitializer implements ContentStoreInitializer {

    @Override
    public Set<String> configurationKeys() {
        return Set.of("my-store.location");
    }

    @Override
    public ContentStore initialize(Map<String, String> configuration) {
        return new MyContentStore(configuration.get("my-store.location"));
    }
}
```

### The ContentStore contract

The engine calls a `ContentStore` from many threads at once. What it calls and when:

| Method | Called by | Expected behaviour |
|---|---|---|
| `bufferPaginationEntryDocument(topic, position, contentKey, content, httpRequestInfo, state)` | `addContent` inside `parallel` | Keep the entry content for the position until it is published. |
| `bufferDocument(topic, position, contentKey, content, httpRequestInfo)` | `addContent` in a function run from `parallel`, or inside `forEach` | Keep the response body for the position until it is published. |
| `contentKeys(topic, position)` | `publish` | The content keys buffered for the position. An empty set means the position is not published. |
| `lock(topic)`, `unlock(topic)` | `publish` | Serialize publishing per topic. |
| `publish(topic, positions...)` | `publish`, with the positions in source order | Write each position's buffers as one message, then drop the buffers. |
| `addPaginationDocument(topic, position, contentKey, content, httpRequestInfo)` | `get` in a `paginate` with `addPageContent` | Write the page. The `rawdata` connector writes it to `<topic>-pages` at once. |
| `lastPosition(topic)` | `contentStream.lastPosition()` and related expressions | The last published position, or null. |
| `contentStream()` | the server (integrity check, recovery, raw data dump) | Producers and consumers per topic. |
| `monitor()` | the worker's health monitor | A `HealthContentStreamMonitor`. |
| `closeTopic(topic)` | the worker, at the end of a run unless kept open | Release the topic's resources. |
| `isClosed()`, `close()` | the worker, the server's `ContentStoreComponent` | |

`HttpRequestInfo` carries the URL, status code, request and response headers and the request duration, for the
metadata of the content. `MetadataContent` describes one content item: correlation ids, topic, position, content key,
resource type (`PAGE`, `ENTRY` or `DOCUMENT`), content type and length, URL, status code, request and response headers,
request duration and state. The `rawdata` connector stores these in each message's
`manifest.json`. `ContentStateKey` is a (topic, position) key for buffers.

`DiscardingContentStore` (module `data-collector-connector-content-stream-discarding`) is the smallest implementation;
`RawdataClientContentStore` is the complete one.

### Registration

Register the initializer both ways, as the existing connectors do:

```java
module com.example.dc.store {
    requires io.descoped.service.provider.api;
    requires transitive io.descoped.dc.api;

    provides io.descoped.dc.api.content.ContentStoreInitializer with com.example.dc.store.MyContentStoreInitializer;

    exports com.example.dc.store;
}
```

and in `src/main/resources/META-INF/services/io.descoped.dc.api.content.ContentStoreInitializer`:

```text
com.example.dc.store.MyContentStoreInitializer
```

`provides` serves the module path; the `META-INF/services` file serves the class path and automatic modules.
`ProviderConfigurator.configure` adds the `uses` at run time, but on the module path it does not resolve modules: the
connector module must be required by a module, or added with `--add-modules`.

Then select it with `content.stream.connector=my-store`, or create it yourself with
`ProviderConfigurator.configure(ContentStoreInitializer.class, "my-store", configuration)`.

## Extending the server

`data-collector-server-base` discovers three kinds of extension: `Component`, `Service` and `Controller`, in
`io.descoped.dc.application.spi`. [Architecture](architecture.md#the-application-data-collector-server-base) describes
their lifecycle.

### Registration and injection

List the class in a `META-INF/services` file named after the interface, here
`src/main/resources/META-INF/services/io.descoped.dc.application.spi.Controller`. Write one class name per line; the
discovery reads every line as a class name, so the file cannot hold comments or blank lines:

```text
com.example.dc.StatusController
```

`UndertowApplication` reads these files with ClassGraph from the class path and the module path, and loads each class
with `Class.forName`. It does not use `ServiceLoader`, so no `provides` clause is involved. Classes are created in the
order they are found. Because they are created by reflection from `io.descoped.dc.api` (`ObjectCreator`), the class and
its constructor must be public, and its package exported; `data-collector-server` exports the packages that hold its
components, services and controllers.

`ObjectCreator` uses the first public constructor whose parameter types are all registered:

| Kind | Can take |
|---|---|
| `Component` | `DynamicConfiguration`, `BusinessSSLResourceSupplier` (null when not configured), components created before it |
| `Service` | everything a component can take, every component, `MetricsResourceFactory`, `HealthResourceFactory` |
| `Controller` | everything a service can take, and every enabled service |

A service cannot take another service. A service whose `isEnabled()` returns false is dropped and cannot be injected.

### A controller

A controller handles every request whose first path segment routes to its `contextPath()` (see
[Architecture](architecture.md#the-application-data-collector-server-base)). It matches methods and paths itself, and
dispatches off the IO thread before blocking work. This one answers on `/status` and uses the server's
`WorkerService`:

```java
package com.example.dc;

import io.descoped.dc.api.http.Request;
import io.descoped.dc.api.util.JsonParser;
import io.descoped.dc.application.spi.Controller;
import io.descoped.dc.server.task.WorkerService;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.util.Set;

public class StatusController implements Controller {

    private final WorkerService workerService;

    public StatusController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @Override
    public String contextPath() {
        return "/status";
    }

    @Override
    public Set<Request.Method> allowedMethods() {
        return Set.of(Request.Method.GET);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod().toString()) && "/status".equals(exchange.getRequestPath())) {
            exchange.setStatusCode(200);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(JsonParser.createJsonParser().toPrettyJSON(workerService.list()));
            return;
        }
        exchange.setStatusCode(400);
    }
}
```

`allowedMethods()` is reported in the `contexts` health resource; it does not restrict requests.

### Services and components

A `Service` is started when the application starts and stopped when it stops; `WorkerService`, `IntegrityCheckService`
and `RecoveryService` are examples. A `Component` holds a shared resource: `initialize()` runs when it is created,
`getDelegate()` returns the resource, `close()` runs at shutdown; `ContentStoreComponent` holds the server's
`ContentStore`.

### Health and metrics resources

Health and metrics resources are found by scanning, not by registration. `HealthResourceFactory` and
`MetricsResourceFactory` scan the packages under `io.descoped.dc` with ClassGraph for implementations of
`io.descoped.dc.api.health.HealthResource` and `io.descoped.dc.api.metrics.MetricsResource`, and create each with its
no-argument constructor. A health resource must be annotated with `@HealthRenderPriority`; one annotated with
`@HealthResourceExclude` is not created at start, and is added per instance with
`HealthResourceFactory.createAndAdddHealthResource(UUID, Class)`, as `WorkerService` does for each worker. Classes
outside `io.descoped.dc` packages are not found.
