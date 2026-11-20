# Specification DSL

A specification tells Data Collector (dc) how to page through an HTTP API and what to write to the content stream. It
is a tree of nodes: functions that make HTTP requests, steps that split and query the responses, and nodes that store
and publish content. A specification is written in JSON or YAML, or built in Java with the builders in
`io.descoped.dc.api.Builders`. This document is the reference for every node type, the expression language and position
handling, as the 3.0 code implements them.

Related documents: [architecture](architecture.md) (engine and content stream), [developer guide](developer-guide.md)
(embedding the engine, writing handlers), [getting started](getting-started.md), [REST API](rest-api.md) (running a
specification as a task with `PUT /tasks`) and [administration](administration.md) (configuration keys, certificate
bundles).

## Contents

- [Specification](#specification)
- [Serialization](#serialization)
- [How a specification runs](#how-a-specification-runs)
- [Node reference](#node-reference)
  - [Configuration nodes](#configuration-nodes)
  - [Function nodes](#function-nodes)
  - [Step nodes](#step-nodes)
  - [Conditions](#conditions)
  - [Response validators and retry](#response-validators-and-retry)
  - [Request bodies](#request-bodies)
  - [Queries](#queries)
- [Expression language](#expression-language)
- [Positions and the content stream](#positions-and-the-content-stream)
- [Configuration keys that affect a specification](#configuration-keys-that-affect-a-specification)
- [Worked example](#worked-example)
- [Further examples](#further-examples)

## Specification

A specification has an id, a name, the id of the function to start with, a list of configuration nodes and a map of
functions.

```json
{
  "type": "Specification",
  "id": "TEST",
  "name": "paging",
  "startFunctionId": "page-loop",
  "configure": [ { "type": "SpecificationContext" } ],
  "functions": { "page-loop": { "type": "Paginate", "id": "page-loop" } }
}
```

| Field | Required | Description |
|---|---|---|
| `type` | yes | `Specification`. |
| `id` | yes | Specification id. The server allows one running task per specification id. |
| `name` | yes | Display name. |
| `startFunctionId` | yes | Id of the function the worker runs first. If no function has this id, building the worker fails with `Start function is incorrect!`. |
| `configure` | no | List of configuration nodes: `SpecificationContext` and `Security`. |
| `functions` | yes | Map of function nodes (`Paginate`, `Get`, `Post`, `Put`, `Delete`). The node's `id` field is what identifies it; the map key is not read (`NodeBuilderDeserializer`). Use the id as the key. |

In Java, `Specification.start(id, name, startFunctionId)` returns a `SpecificationBuilder`:

| Method | Description |
|---|---|
| `configure(ConfigurationBuilder)` | Adds a configuration node (`context()` or `security()`). |
| `function(NodeWithIdBuilder)` | Adds a function, keyed by its id. |
| `end()` | Builds the `Specification`: configuration nodes first, then every function. An `Execute` that points to an id not in `functions` fails here. |
| `get(String id)` | Returns the builder of a function. |
| `serialize()` / `serializeAsYaml()` | See [Serialization](#serialization). |

When a specification has no `SpecificationContext`, `Configurations.Builder` adds one with topic `topic`, and adds an
empty `Security`. When a `SpecificationContext` is present but sets no topic, and the worker sets none either
(`Worker.WorkerBuilder.topic`), building the worker fails with `Topic is NOT configured!`.

## Serialization

| Direction | Entry point | Format |
|---|---|---|
| Read | `Specification.deserialize(String source)` returns a `SpecificationBuilder` | JSON or YAML. The source is read with a Jackson YAML mapper and `NodeBuilderDeserializer`. |
| Read | `Specification.deserialize(String source, Class<R> builderClass)` | Same, for another builder class. |
| Write | `SpecificationBuilder.serialize()` | Pretty-printed JSON. |
| Write | `SpecificationBuilder.serializeAsYaml()` | YAML. |
| Write | `NodeBuilder.serialize()`, `LeafNodeBuilder.serialize()` | Pretty-printed JSON of a single node. |

The server reads the body of `PUT /tasks` with `Specification.deserialize` (`TaskController`). The test
`ExampleSpecificationsTest` reads and builds every file in `examples/specs` and the server's `page-test.json` the same
way.

Rules of the reader (`NodeBuilderDeserializer`):

- Every node has a `type`. The value is matched against `BuilderType` without regard to case. A missing `type` fails
  with `Error resolving node: <path>`; an unknown `type` fails with a `NullPointerException`.
- Only the fields listed in this document are read. Other fields are ignored.
- Values in `SpecificationContext.variables` may be integers, booleans, strings or `null`. Other JSON types (for
  example a decimal number or an integer too large for `int`) fail with `The context variable type is not supported`.
- Values in `SpecificationContext.globalState`, `Paginate.variables`, `JwtIdentity.headerClaims` and
  `JwtIdentity.claims` must be strings; any other value is read as `null`.
- Values in `AddContent.state` may be integers, booleans or strings.
- Header values in `requestHeaders` may be integers, booleans or strings; they are sent as text.

The JSON `type` names are: `Specification`, `SpecificationContext`, `Security`, `JwtIdentity`,
`JwtIdentityTokenBodyPublisherProducer`, `Paginate`, `ForEach`, `Sequence`, `NextPage`, `Parallel`, `Execute`,
`Process`, `QueryBody`, `QueryEval`, `QueryXPath`, `QueryJsonToken`, `QueryXmlToken`, `QueryJqPath`, `QueryRegEx`,
`ConditionWhenVariableIsNull`, `ConditionWhenExpressionIsTrue`, `AddContent`, `Publish`, `Get`, `Post`, `Put`,
`Delete`, `HttpStatusRetryWhile`, `HttpStatusValidation`, `BodyPublisher`, `StringBodyPublisherProducer`,
`HttpResponseBodyContains` and `Console`.

## How a specification runs

The `Worker` (`data-collector-core`) builds the specification, then runs the start function. Each node type has a
handler in `io.descoped.dc.core.handler`, found by the `@Handler` annotation. Handlers pass an `ExecutionContext` to each
other. It holds:

- **variables**: named values that expressions read. They come from the `SpecificationContext`, from the worker
  (`Worker.WorkerBuilder.variable`), from `Paginate.variables`, from `NextPage` outputs, from `Parallel.variables`, from
  `Execute.inputVariables`, and from processors.
- **state**: internal values passed between handlers, such as the current `Response` and the current page entry.
- **global state**: values shared by the whole run, such as `global.topic`.

Flow node handlers (`AbstractNodeHandler`) first join the specification's global context into their input: variables, state and headers
from the `SpecificationContext` are added, and values already in the input win.

An HTTP function (`Get`, `Post`, `Put`, `Delete`) makes one request, validates the response, then runs its `pipes` in
order on the response. Each step sees the variables produced by the steps before it. If a step throws, the remaining
steps still run, and the first failure is rethrown afterwards; an end of stream raised by `Sequence` is not rethrown but
marks the page as the last one (`OperationHandler.handleResponse`). The function returns every variable its steps
produced.

A typical specification has this shape:

```text
Paginate                      loops over pages
└── Execute → Get             fetches one page
    ├── Sequence              lists the positions on the page
    ├── NextPage              computes the variables for the next page
    └── Parallel              for each entry on the page, concurrently
        ├── AddContent        buffers the entry under its position
        ├── Execute → Get     fetches a related document
        │   └── AddContent    buffers that document under the same position
        └── Publish           publishes the position
```

## Node reference

The table lists every node type, its Java builder and where it may appear.

| JSON `type` | Java builder (`Builders.*` unless noted) | May appear in | Handler |
|---|---|---|---|
| `Specification` | `Specification.start(id, name, startFunctionId)` | root | `Worker` |
| `SpecificationContext` | `context()` | `configure` | none (configuration) |
| `Security` | `security()` | `configure` | none (configuration) |
| `JwtIdentity` | `jwt(id, headerClaims(), claims())` | `Security.identities` | none (read by `JwtTokenBodyPublisherProducerHandler`) |
| `Paginate` | `paginate(id)` | `functions` | `PaginateHandler` |
| `Get` | `get(id)` | `functions` | `GetHandler` |
| `Post` | `post(id)` | `functions` | `PostHandler` |
| `Put` | `put(id)` | `functions` | `PutHandler` |
| `Delete` | `delete(id)` | `functions` | `DeleteHandler` |
| `Execute` | `execute(functionId)` | `Paginate.iterate`, `pipes` | `ExecuteHandler` |
| `Sequence` | `sequence(splitQuery).expected(query)` | `pipes` of an HTTP function | `SequenceHandler` |
| `NextPage` | `nextPage()` | `pipes` of an HTTP function | `NextPageHandler` |
| `Parallel` | `parallel(splitQuery)` | `pipes` | `ParallelHandler` |
| `ForEach` | `forEach(splitQuery)` | `pipes` | `ForEachHandler` |
| `Process` | `process(Class)` | `pipes` | `ProcessHandler` |
| `Console` | `console()` | `pipes` | `ConsoleHandler` |
| `AddContent` | `addContent(positionExpression, contentKey)` | `pipes` | `AddContentHandler` |
| `Publish` | `publish(positionExpression)` | `pipes` | `PublishHandler` |
| `ConditionWhenVariableIsNull` | `whenVariableIsNull(name)` | `Paginate.until` | `WhenVariableIsNullHandler` |
| `ConditionWhenExpressionIsTrue` | `whenExpressionIsTrue(expression)` | `Paginate.until` | `WhenExpressionIsNotTrueHandler` |
| `HttpStatusValidation` | `status()` | `responseValidators` | `HttpStatusValidationHandler` |
| `HttpResponseBodyContains` | `bodyContains(query, literal)` | `HttpStatusValidation.success` | `BodyContainsHandler` |
| `HttpStatusRetryWhile` | `statusCode().is(code, unit, amount)` | `Get.retryWhile` | `HttpStatusRetryWhileHandler` |
| `BodyPublisher` | `bodyPublisher()` | `Post.bodyPublisher`, `Put.bodyPublisher` | `BodyPublisherHandler` |
| `StringBodyPublisherProducer` | `bodyPublisher().plainText(String)` or `.urlEncoded(String)` | `BodyPublisher` | `StringBodyPublisherProducerHandler` |
| `JwtIdentityTokenBodyPublisherProducer` | `jwtToken()` | `BodyPublisher` | `JwtTokenBodyPublisherProducerHandler` |
| `QueryXPath` | `xpath(expression)` | query positions | `XPathHandler` |
| `QueryJqPath` | `jqpath(expression)` | query positions | `JqPathHandler` |
| `QueryRegEx` | `regex(query, expression)` | query positions | `RegExHandler` |
| `QueryEval` | `eval(expression)`, `eval(query, bindTo, expression)` | query positions | `EvalHandler` |
| `QueryBody` | `body()` | query positions | `BodyHandler` |
| `QueryJsonToken` | `jsonToken()` | `Sequence.splitQuery`, `Parallel.splitQuery` | `JsonTokenHandler` |
| `QueryXmlToken` | `new XmlTokenBuilder()` (no `Builders` method) | `Sequence.splitQuery`, `Parallel.splitQuery` | `XmlTokenHandler` |

There is no JSONPath node; JSON is queried with jq expressions (`QueryJqPath`).

### Configuration nodes

#### SpecificationContext

Global values for the whole specification (`SpecificationContextBuilder`).

| JSON field | Java method | Description |
|---|---|---|
| `variables` | `variable(name, value)` | Variables available to every node. String values that contain `${...}` are evaluated once, when a `Paginate` starts (`PaginateHandler`); see [Expression language](#expression-language). |
| `globalState` | `globalState(key, value)` | Global state. The engine reads one key: `global.topic`, the name of the content stream topic. |
| (in `globalState`) | `topic(name)` | Sets `global.topic`. |
| `headers` | `header(name, value)` | Map of header name to a list of values. Sent with every HTTP request, before the function's own `requestHeaders`. These values are not evaluated as expressions. |

```json
{
  "type": "SpecificationContext",
  "variables": {
    "baseURL": "https://data.brreg.no/enhetsregisteret/api",
    "nextPosition": "${contentStream.lastOrInitialPosition(1)}"
  },
  "globalState": { "global.topic": "enhetsregister" },
  "headers": { "accept": [ "application/json" ] }
}
```

#### Security

Client certificate and identities (`SecurityBuilder`).

| JSON field | Java method | Description |
|---|---|---|
| `bundleName` | `sslBundleName(name)` | Name of the certificate bundle used for TLS client authentication (business certificate). The worker sets the HTTP client's SSL context and trust manager from this bundle. An unknown name fails with `Unable to resolve certificate bundle`. Bundles are loaded from directories that contain a `secret.properties` file, named after the directory, or from the business SSL resource; see [administration](administration.md). |
| `identities` | `identity(IdentityBuilder)` | List of identities. The only identity type is `JwtIdentity`. |

#### JwtIdentity

A JWT signing identity, used by `JwtIdentityTokenBodyPublisherProducer` to build a JWT grant, for example for
Maskinporten (`JwtIdentityBuilder`, `JwtHeaderClaims`, `JwtClaims`).

| JSON field | Java | Description |
|---|---|---|
| `id` | first argument of `jwt(...)` | Id that `JwtIdentityTokenBodyPublisherProducer.identityId` refers to. |
| `headerClaims.alg` | `headerClaims().alg("RS256")` | Written to the JWT header `alg`. |
| `headerClaims.sslBundleName` | `headerClaims().x509CertChain(bundleName)` | Certificate bundle that holds the signing key. |
| `claims.issuer` | `claims().issuer(...)` | JWT `iss`. Expressions are evaluated. |
| `claims.audience` | `claims().audience(...)` | JWT `aud`. Not evaluated. |
| `claims.timeToLiveInSeconds` | `claims().timeToLiveInSeconds(...)` | Seconds until `exp`. Expressions are evaluated. Required. |
| `claims.<name>` | `claims().claim(name, value)` | Any other claim, for example `scope` or `resource`. Not evaluated. |

What `JwtTokenBodyPublisherProducerHandler` does:

- It signs with RSA SHA-256 (`Algorithm.RSA256`) using the key pair of the bundle named in `sslBundleName`. The value of
  `alg` is only written to the header.
- The header has `alg` and `x5c`; `x5c` holds the bundle's certificates, which must be valid at signing time.
- It sets `iss`, `aud`, `iat` (now), `exp` (now plus `timeToLiveInSeconds`) and `jti` (a random UUID).
- It also adds every entry of `claims` as a claim with its literal value, including `issuer`, `audience` and
  `timeToLiveInSeconds`.
- Before it evaluates `issuer` and `timeToLiveInSeconds`, it evaluates every context variable as an expression. Each
  context variable must be a string or `null` at this point, or the handler fails with a `ClassCastException`.

```json
{
  "type": "Security",
  "identities": [ {
    "type": "JwtIdentity",
    "id": "maskinporten",
    "headerClaims": { "alg": "RS256", "sslBundleName": "ssb-test-certs" },
    "claims": {
      "audience": "https://ver2.maskinporten.no/",
      "issuer": "${clientId}",
      "scope": "altinn:serviceowner/instances.read",
      "timeToLiveInSeconds": "${jwtGrantTimeToLiveInSeconds}"
    }
  } ]
}
```

### Function nodes

Functions are the nodes in `functions`. They have an `id` and are the targets of `Execute`.

#### Paginate

Fetches pages until a condition holds, prefetching the next page while the current one is processed
(`PaginateHandler`, `PaginationLifecycle`, `PrefetchAlgorithm`).

| JSON field | Java method | Required | Description |
|---|---|---|---|
| `id` | `paginate(id)` | yes | Function id. |
| `variables` | `variable(name, expression)` | yes (may be `{}`) | Variables set on each page's input. See below. |
| `iterate` | `iterate(execute(...))` | yes | List of `Execute` nodes, run in order for each page. |
| `threshold` | `prefetchThreshold(int)` | yes | Prefetch threshold, in positions. See [Prefetch](#prefetch). |
| `until` | `until(condition)` | yes | Condition that ends the loop. |
| `addPageContent` | `addPageContent(positionVariable)` | no | When `true`, the whole response of each page request is written to the topic `<topic>-pages`. |
| `positionVariable` | (argument of `addPageContent`) | with `addPageContent` | Variable whose value is the position of the page document. |

At start, `PaginateHandler` evaluates the `SpecificationContext` variables that contain `${...}` and adds the result to
the context. Then it fetches the first page and keeps fetching pages until one of these happens:

- the `until` condition is true after a page;
- a `Sequence` finds no entries on a page (end of stream);
- the task is cancelled, or the worker's iteration limit (`Worker.WorkerBuilder.stopAtNumberOfIterations`) is reached.

For each page and each `iterate` target, the handler copies the input and, for each entry of `variables`:

- if the variable is not already set, sets it to the text of the entry;
- if the entry has the form `${name}` and `name` is a variable in the input, sets it to the value of `name`.

`Paginate` variables are therefore aliases, such as `"fromPosition": "${nextPosition}"`. A longer expression in this map
is not evaluated here.

The next page starts from the output of the previous page, which holds the variables that the page's `NextPage`
produced.

With `addPageContent`, the first request made by each `iterate` target writes its response body to `<topic>-pages`,
with content key `page`, at the position given by the value of `positionVariable` (`OperationHandler.doRequest`). If the
variable has no value, the request fails with `Unable to resolve position`.

#### Get

Makes an HTTP GET request (`GetBuilder`, `GetHandler`, `OperationHandler`).

| JSON field | Java method | Description |
|---|---|---|
| `id` | `get(id)` | Function id. |
| `url` | `url(String)` | Request URL. Expressions are evaluated. Required. |
| `requestHeaders` | `header(name, value)` | Map of header name to a list of values. Expressions in the values are evaluated. |
| `retryWhile` | `retryWhile(statusCode().is(...))` | List of `HttpStatusRetryWhile`. Only the first entry is used. |
| `responseValidators` | `validate(status()...)` | List of `HttpStatusValidation`. When the list is empty, the builder adds `status().success(200)`. |
| `pipes` | `pipe(NodeBuilder)` | Steps run on the response, in order. |
| `returnVariables` | `returnVariables(String...)` | Names of the variables a page hands back to `Paginate`. See below. |

`returnVariables` names the variables that `PaginationLifecycle` uses to check the `until` condition after a page. Their
values are taken from the page's `NextPage` outputs. The list does not filter the function's output: all variables
produced by the steps are returned.

When a `Sequence` in `pipes` splits with `QueryJsonToken` or `QueryXmlToken`, `GetHandler` writes the response body to a
temporary file (`TempFileBodyHandler`) so that `Sequence` and `Parallel` can stream it.

Request handling, common to all HTTP functions (`OperationHandler`):

- Headers from the `SpecificationContext` are added first, then the function's `requestHeaders`.
- The URL and header values are evaluated as templates.
- The request waits at most `data.collector.http.request.timeout.seconds` for a response (default 15).
- A request that fails, including a response that fails validation, is tried up to
  `data.collector.http.request.retryCount` times in all (default 3). If no attempt succeeds, the function fails with
  `No response received`; with `retryWhile`, it goes on with the last response instead.

#### Post and Put

Make an HTTP POST or PUT request (`PostBuilder`/`PutBuilder`, `PostHandler`/`PutHandler`). Fields as `Get`, without
`retryWhile`, plus:

| JSON field | Java method | Description |
|---|---|---|
| `bodyPublisher` | `data(bodyPublisher()...)` | Request body. Without it, the request has no body. See [Request bodies](#request-bodies). |

The reader does not read `requestHeaders` for `Post`, `Put` and `Delete`. Headers set with `header(...)` in Java are
sent, but they are lost when the specification is written to JSON or YAML and read back. Headers from the
`SpecificationContext` are sent in both cases.

#### Delete

Makes an HTTP DELETE request (`DeleteBuilder`, `DeleteHandler`). Fields: `id`, `url`, `responseValidators`, `pipes`,
`returnVariables`. Java also has `header(name, value)`, with the same limit as `Post`.

### Step nodes

Steps appear in `pipes` (and `Execute` also in `Paginate.iterate`).

#### Execute

Runs another function (`ExecuteBuilder`, `ExecuteHandler`).

| JSON field | Java method | Description |
|---|---|---|
| `executeId` | `execute(functionId)` | Id of the function to run. Required. |
| `inputVariables` | `inputVariable(name, query)` | Variables computed with a query before the call. |
| `requiredInputs` | `requiredInput(name)` | Variables that must be set when the function is called; otherwise the step fails with `Required input variable: '<name>' NOT found!`. |

Each input variable query is evaluated as a string. Inside `Parallel` or `ForEach` it runs on the current entry;
otherwise it runs on the current response body. The queries run in order, and each sees the variables set by the
previous ones. The target function gets a copy of the current context plus these variables, and its output is the
output of `Execute`.

#### Sequence

Lists the positions on a page (`SequenceBuilder`, `SequenceHandler`).

| JSON field | Java | Description |
|---|---|---|
| `splitQuery` | `sequence(query)` | Query that splits the response into entries. Required. |
| `expectedQuery` | `.expected(query)` | Query run on each entry; its string result is the entry's position. Required. |

The positions, in document order, are registered as expected positions with the worker's `BufferedReordering`, and
their number is reported to the prefetch algorithm. If the page has no entries, `Sequence` raises end of stream and the
page is the last one.

#### NextPage

Computes the variables for the next page (`NextPageBuilder`, `NextPageHandler`).

| JSON field | Java method | Description |
|---|---|---|
| `outputs` | `output(name, query)` | Map of variable name to a query. Each query is evaluated as a string on the response body. Required. |

Each output becomes a variable in the function's output and a next-position value for `Paginate`. A `QueryJqPath` or
`QueryRegEx` that finds nothing gives `null`; a `QueryXPath` that matches nothing fails the step.

#### Parallel

Runs steps for each entry of the response, concurrently (`ParallelBuilder`, `ParallelHandler`).

| JSON field | Java method | Description |
|---|---|---|
| `splitQuery` | `parallel(query)` | Query that splits the response into entries. Required. Must be `QueryXPath`, `QueryJqPath`, `QueryJsonToken` or `QueryXmlToken`. |
| `variables` | `variable(name, query)` | Variables computed with a query on each entry, as strings. Required (may be `{}`). |
| `pipes` | `pipe(NodeBuilder)` | Steps run for each entry, in order. |
| `publish` | `publish(publish(...))` | Read and written, but not used by `ParallelHandler`. Put `Publish` in `pipes`. |

For each entry the handler evaluates `variables`, then runs `pipes` as one task on the worker thread pool. Steps of one
entry run in order and see the variables produced by earlier steps. Different entries run concurrently. When an entry
fails, the page is marked as failed, entries that start after that do nothing, and the failure ends the run.

With `data.collector.parallel.queueBuffer.threshold` set to N, the handler waits for every N scheduled entries to
complete before it schedules more. With `QueryJsonToken` it streams entries from the temporary file and waits after
every 1000 entries.

#### ForEach

Runs steps for each item of a list inside the current entry or response, one item at a time (`ForEachBuilder`,
`ForEachHandler`).

| JSON field | Java method | Description |
|---|---|---|
| `splitQuery` | `forEach(query)` | Query that splits the current entry (inside `Parallel`) or the response body into items. Required. |
| `pipes` | `pipe(NodeBuilder)` | Steps run for each item, in order. |

`ForEachHandler` treats the items as JSON nodes, so the split query must be `QueryJqPath`. The split query is evaluated
without the current variables. Steps run on the calling thread, and their output is discarded: `ForEach` returns no
variables.

#### Process

Runs a Java `io.descoped.dc.api.Processor` (`ProcessBuilder`, `ProcessHandler`).

| JSON field | Java method | Description |
|---|---|---|
| `processorClass` | `process(Class<? extends Processor>)` | Fully qualified class name. |
| `requiredOutputs` | `output(name)` | Variables the processor is expected to return. |

The reader loads `processorClass` with `Class.forName`. `ProcessHandler` creates the processor with
`ObjectCreator.newInstance`, which fills constructor parameters from the worker's services, and calls
`process(ExecutionContext)` with the current context and response. The step fails with `A variable is missing!` when
none of `requiredOutputs` is among the processor's output variables; with an empty list it always fails.

#### Console

Logs the current state (`ConsoleBuilder`, `ConsoleHandler`). No fields. At log level DEBUG it logs the response status
and URL, all variables and the current body (the entry inside `Parallel`, else the response body).

#### AddContent

Buffers content in the content store under a position (`AddContentBuilder`, `AddContentHandler`).

| JSON field | Java | Description |
|---|---|---|
| `positionVariableExpression` | first argument of `addContent(...)` | Expression for the position, for example `${position}`. Evaluated as a single expression; the result must be a string. |
| `contentKey` | second argument of `addContent(...)` | Name of the content under the position, for example `entry`. Expressions are evaluated. |
| `state` | `storeState(key, value)` | Map stored in the content's metadata. Keys with `${...}` are evaluated as a single expression; string values are evaluated as templates. Used only when the content is the page entry. |

What is stored depends on where the step runs:

- Directly in `Parallel.pipes`: the current page entry, with its `state` (`bufferPaginationEntryDocument`).
- In the `pipes` of a function reached through an `Execute` in `Parallel`, or in `ForEach.pipes`: the body of the
  current response (`bufferDocument`); directly in `ForEach.pipes` that is the whole response, not the item. `state`
  is not stored.

`AddContent` needs one of these: outside them there is no entry to store and the step fails.

#### Publish

Publishes a position to the content stream (`PublishBuilder`, `PublishHandler`).

| JSON field | Java | Description |
|---|---|---|
| `positionVariableExpression` | `publish(positionExpression)` | Expression for the position. Evaluated as a single expression; the result must be a string. |

If the content store holds content for the position, the position is marked as completed. Positions are published in
the order `Sequence` expected them; see [Publishing order](#publishing-order). If no content was added for the
position, `Publish` does nothing.

### Conditions

Conditions end a `Paginate` loop. They are evaluated after each page, on the page's output, and again by
`PaginationLifecycle` with only the page's `returnVariables` set, each with its value from `NextPage`.

| JSON `type` | Java | Field | True when |
|---|---|---|---|
| `ConditionWhenVariableIsNull` | `whenVariableIsNull(name)` | `identifier`: a variable name | The variable is not set or is `null`. |
| `ConditionWhenExpressionIsTrue` | `whenExpressionIsTrue(expression)` | `identifier`: an expression | The expression evaluates to `true`. It must contain `${...}` and return a boolean. |

Because of the second check, the variable a condition reads must be listed in the page function's `returnVariables`
and set by its `NextPage`. If it is not listed, `ConditionWhenVariableIsNull` is true after the first page and the loop
stops.

```json
"until": { "type": "ConditionWhenVariableIsNull", "identifier": "nextPosition" }
```

```json
"until": { "type": "ConditionWhenExpressionIsTrue", "identifier": "${nextPage > totalPages}" }
```

### Response validators and retry

#### HttpStatusValidation

Decides whether a response is accepted (`HttpStatusValidationBuilder`, `HttpStatusValidationHandler`).

| JSON field | Java method | Description |
|---|---|---|
| `success` | `success(Integer...)`, `success(from, to)`, `success(code, predicate)` | Object keyed by status code. Each value is a list of response predicates (`HttpResponseBodyContains`), usually empty: `{"200": []}`. Required. |
| `failed` | `fail(Integer...)`, `fail(from, to)` | List of status codes. |

A response whose status is a key of `success` is accepted. Any other status fails with `HttpErrorException`, whether or
not it is listed in `failed`; `failed` does not change the outcome. The failure message holds the URL, the status, the
response body and the request and response headers. A failed validation is retried as described under [Get](#get).

When a status has predicates, the handler evaluates them in order until one returns false. In 3.0 the status is
accepted whatever the predicates return.

Only the status codes in `io.descoped.dc.api.http.HttpStatus` are known: 200 to 206, 300 to 305, 400 to 415 and 500 to
505. Building a validator with another code fails with `HttpStatusCode is not supported`, and so does a response with
another code that is not accepted.

In Java, `success` and `fail` with exactly two arguments are ranges: `success(200, 299)` accepts every known code from
200 to 299. With one argument or three or more, they are lists.

```json
"responseValidators": [ {
  "type": "HttpStatusValidation",
  "success": {
    "200": [],
    "404": [ {
      "type": "HttpResponseBodyContains",
      "queryBuilder": { "type": "QueryXPath", "expression": "/feil/kode" },
      "equalToStringLiteral": "SM-001"
    } ]
  },
  "failed": [ 400, 500 ]
} ]
```

#### HttpResponseBodyContains

A response predicate (`BodyContainsBuilder`, `BodyContainsHandler`).

| JSON field | Java | Description |
|---|---|---|
| `queryBuilder` | first argument of `bodyContains(query, literal)` | Query evaluated as a string on the response body. |
| `equalToStringLiteral` | second argument | The predicate is true when the query result equals this text. |

#### HttpStatusRetryWhile

Waits and repeats a `Get` while the response has a given status (`HttpStatusRetryWhileBuilder`,
`HttpStatusRetryWhileHandler`).

| JSON field | Java | Description |
|---|---|---|
| `statusCode` | `statusCode().is(code, unit, amount)` | Status that triggers a retry. |
| `duration` | `unit` | A `java.util.concurrent.TimeUnit` name, for example `SECONDS`. |
| `amount` | `amount` | How many units to wait. |

While the response has `statusCode`, the handler waits `amount` `duration` and repeats the request. There is no upper
limit on the number of repeats. Validators run after the last response. Only the first entry of `retryWhile` is used.

```json
"retryWhile": [ { "type": "HttpStatusRetryWhile", "statusCode": 404, "duration": "SECONDS", "amount": 15 } ]
```

### Request bodies

#### BodyPublisher

The body of a `Post` or `Put` (`BodyPublisherBuilder`, `BodyPublisherHandler`). Set one of the three forms.

| JSON field | Java method | Content-Type set by the handler |
|---|---|---|
| `plainTextData` | `plainText(String)`, `plainText(producer)` | none |
| `urlEncodedData` | `urlEncoded(String)`, `urlEncoded(producer)` | `application/x-www-form-urlencoded` |
| `partsData` | `textPart(name, value)`, `formPart(name, filename, value)` | `multipart/form-data;boundary=...` |

`plainTextData` and `urlEncodedData` hold a producer: `StringBodyPublisherProducer` or
`JwtIdentityTokenBodyPublisherProducer`. `partsData` is a list of parts with `name`, `value` and, for a form part,
`filename`. Parts are not evaluated as expressions. The reader also reads `charset` on a part, but does not apply it.

#### StringBodyPublisherProducer

| JSON field | Java | Description |
|---|---|---|
| `data` | `bodyPublisher().plainText(String)` or `.urlEncoded(String)` | Body text. Expressions are evaluated. |

```json
"bodyPublisher": {
  "type": "BodyPublisher",
  "plainTextData": {
    "type": "StringBodyPublisherProducer",
    "data": "grant_type=password&username=${ENV.'moveIt.server.username'}&password=${ENV.'moveIt.server.password'}"
  }
}
```

#### JwtIdentityTokenBodyPublisherProducer

Builds a signed JWT from a `JwtIdentity` and places it in a body template (`JwtIdentityTokenBodyPublisherProducerBuilder`,
`JwtTokenBodyPublisherProducerHandler`).

| JSON field | Java method | Description |
|---|---|---|
| `identityId` | `identityId(id)` | Id of a `JwtIdentity` in `Security.identities`. An unknown id fails the build with `Incorrect identity reference!`. |
| `bindTo` | `bindTo(name)` | Variable that holds the signed JWT while `token` is evaluated. |
| `token` | `token(template)` | Body template. Expressions are evaluated. |

```json
"bodyPublisher": {
  "type": "BodyPublisher",
  "urlEncodedData": {
    "type": "JwtIdentityTokenBodyPublisherProducer",
    "identityId": "maskinporten",
    "bindTo": "JWT_GRANT",
    "token": "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=${JWT_GRANT}"
  }
}
```

### Queries

A query reads a value or a list from a document. It is used in three ways:

- **split**: returns a list of entries (`Sequence.splitQuery`, `Parallel.splitQuery`, `ForEach.splitQuery`);
- **string**: returns one string (`Sequence.expectedQuery`, `NextPage.outputs`, `Parallel.variables`,
  `Execute.inputVariables`, `HttpResponseBodyContains.queryBuilder`, the inner query of `QueryRegEx` and `QueryEval`);
- **object**: returns one node (part of the query API; no node of the DSL uses it).

| JSON `type` | Fields | Split | String |
|---|---|---|---|
| `QueryXPath` | `expression` | yes | yes |
| `QueryJqPath` | `expression` | yes | yes |
| `QueryRegEx` | `query`, `expression` | no | yes |
| `QueryEval` | `expression`, optional `query` and `bindToVariable` | no | yes |
| `QueryBody` | none | no | yes |
| `QueryJsonToken` | none | streaming, in `Sequence` and `Parallel` | no |
| `QueryXmlToken` | none | streaming, in `Sequence` and `Parallel` | no |

#### QueryXPath

XPath over XML (`XPathHandler`, `XPathParser`). The `expression` is evaluated as a template first. XML is parsed with
namespace awareness off, and documents with a DOCTYPE declaration are rejected. In split mode each matching node becomes
a document of its own, so a query on an entry starts at the entry's root element, for example `/entry/id`. In string
mode an element gives its text content, an attribute or text node its value; other node types fail, and so does a
query that matches nothing.

#### QueryJqPath

jq over JSON (`JqPathHandler`, jackson-jq with the jq 1.6 function set). The `expression` is evaluated as a template
first. A document given as bytes or text must be a JSON object: a top-level array fails. Use `QueryJsonToken` to split a
top-level array. In split mode every result is an entry; an empty object gives no entries. In string mode the first
result is used: a number or string gives its text, `null` gives `null`, and any other type logs an error and gives
`null`.

#### QueryRegEx

Applies a Java regular expression to the string result of `query` (`RegExHandler`). Returns the whole first match, or
`null` when nothing matches. The `expression` is not evaluated as an expression.

```json
{ "type": "QueryRegEx", "expression": "(?<=[?&]position=)[^&]*",
  "query": { "type": "QueryXPath", "expression": "/feed/link[@rel=\"next\"]/@href" } }
```

#### QueryEval

Evaluates an expression (`EvalHandler`). When `query` is set, its string result is first bound to the variable named
`bindToVariable`. The `expression` is then evaluated as a single expression and the result converted to text.

```json
{ "type": "QueryEval",
  "query": { "type": "QueryJqPath", "expression": "._embedded | .oppdaterteEnheter[-1]? | .oppdateringsid" },
  "bindToVariable": "lastUpdateId",
  "expression": "${cast.toLong(lastUpdateId) + 1L}" }
```

#### QueryBody

Returns the body of the current response as UTF-8 text (`BodyHandler`). It ignores the entry it is given, so inside
`Parallel` it still returns the whole response. It is used to pass a whole response on, for example a token:
`"inputVariables": {"accessToken": {"type": "QueryBody"}}`.

#### QueryJsonToken and QueryXmlToken

Split a large response without loading it into memory. They work only as `splitQuery` of a `Sequence` and a `Parallel`
in the same `Get`: when `GetHandler` finds a `Sequence` with one of them, it streams the body to a temporary file, and
both nodes read it from there.

- `QueryJsonToken` (`JsonTokenParser`) reads a top-level JSON array and gives each object in it as an entry. Query the
  entries with `QueryJqPath`.
- `QueryXmlToken` (`XmlTokenParser`) gives no entries in 3.0: its token reader is empty.

## Expression language

### Syntax

An expression is written `${...}`. The text inside is an [Apache Commons JEXL](https://commons.apache.org/proper/commons-jexl/)
3.2.1 expression, evaluated by `io.descoped.dc.api.el.ExpressionLanguage`. The text inside cannot contain `}`.

Examples from the shipped specifications:

```text
${baseURL}/api/events?position=${fromPosition}&pageSize=10
${contentStream.lastOrInitialPosition(1)}
${cast.toLong(contentStream.lastOrInitialPosition(0)) + 1}
${fromStartPosition == 1L ? (cast.toLong(updateId)) : fromStartPosition}
${ENV.'maskinporten.clientId'}
${nextPage > totalPages}
```

There are two ways a field is evaluated:

- **template** (`evaluateExpressions`): every `${...}` is replaced by the text of its result. A `null` result becomes
  the text `null`.
- **single** (`evaluateExpression`): the first `${...}` is evaluated and its result is returned with its type; text
  outside it is dropped. When a field evaluated this way has no `${...}`, the whole text is evaluated as JEXL.

| Field | Evaluation |
|---|---|
| `SpecificationContext.variables` | single, when a `Paginate` starts; only values that contain `${...}` |
| `SpecificationContext.headers` | not evaluated |
| `Paginate.variables` | only `${name}`, as an alias of variable `name` |
| `Get`/`Post`/`Put`/`Delete` `url` | template |
| `requestHeaders` values | template |
| `AddContent.positionVariableExpression` | single |
| `AddContent.contentKey` | template |
| `AddContent.state` | keys: single, if they contain `${...}`; string values: template |
| `Publish.positionVariableExpression` | single |
| `ConditionWhenExpressionIsTrue.identifier` | single; must contain `${...}` |
| `QueryEval.expression` | single |
| `QueryXPath.expression`, `QueryJqPath.expression` | template |
| `QueryRegEx.expression` | not evaluated |
| `StringBodyPublisherProducer.data` | template |
| `JwtIdentityTokenBodyPublisherProducer.token` | template |
| `JwtIdentity` claims `issuer`, `timeToLiveInSeconds` | template |
| other `JwtIdentity` claims | not evaluated |

### Variables

An expression sees the variables of the current `ExecutionContext`; see [How a specification runs](#how-a-specification-runs).
The JEXL engine runs in strict mode: a variable that is not set fails the evaluation (`EvaluationException` or a JEXL
exception), and the error is logged together with all current variables.

### Functions

`ExpressionLanguage` registers four objects in every evaluation:

| Name | Class | Members |
|---|---|---|
| `ENV` | JEXL `MapContext` | The worker's configuration map. In the server this is the whole server configuration (configuration files, environment variables with the prefix `DC_`, keyed without it, and system properties). Keys with dots are quoted: `ENV.'maskinporten.clientId'`. |
| `cast` | `ELCast` | `toInteger(String)`, `toLong(String)`, `toFloat(String)`, `toDouble(String)`. A `null` argument gives `null`. |
| `convert` | `ELConvert` | `utcDateToEpoc(String)`. In 3.0 it ignores its argument and returns the epoch second of `2020-10-05T00:00:00Z`. |
| `contentStream` | `ELContentStream` | See below. |

`contentStream` reads the last published position of the run's topic (`global.topic`) from the content store, through
`EvaluateLastContentStreamPosition`. All positions are strings.

| Method | Returns |
|---|---|
| `hasLastPosition()` | `true` if the topic has a last position. |
| `lastPosition()` | The last position of the topic, or `null`. |
| `lastOrInitialPosition(value)` | The last position, or `value` (a string, `Integer` or `Long`) as text if there is none. |
| `hasLastPagePosition()` | `true` if the topic `<topic>-pages` has a last position. |
| `lastPagePosition()` | The last position of `<topic>-pages`, or `null`. |
| `lastOrInitialPagePosition(value)` | The last page position, or `value` as text. |

These functions fail with `Topic is null!` when no topic is set, and with `ContentStore is null or closed!` when there
is no open content store.

### Security of expressions

`ExpressionLanguage` builds the JEXL engine with `cache(512)`, `strict(true)` and `silent(false)`, and configures no
sandbox or permissions. An expression can call public methods on any object it reaches, and `ENV` exposes every
configuration value of the process, including secrets. The `Process` node loads the class named in `processorClass`.
A specification therefore runs with the rights of the dc process.

## Positions and the content stream

A position identifies one published item, for example an event id or a sequence number. dc writes content to the
content store under a position, and publishes positions to the topic named by `global.topic`. The content store
provider is chosen with `content.stream.connector` (`discarding`, the engine's default, or `rawdata`, which the
server's `application-defaults.properties` sets); see [architecture](architecture.md).

### The position chain

1. `Sequence.expectedQuery` lists the positions of a page, in document order.
2. `Parallel.variables` computes the position of each entry, usually into a variable named `position`, with the same
   query as `expectedQuery`.
3. `AddContent` buffers the entry, and the documents fetched for it, under `positionVariableExpression`
   (`${position}`). Several `AddContent` steps with different `contentKey` values add several documents to one position.
4. `Publish` with the same `positionVariableExpression` marks the position as completed.

### Publishing order

`PublishHandler` hands completed positions to `BufferedReordering`. A position is published only when every position
expected before it, on this page or an earlier one, is completed too, so positions are published in the order
`Sequence` listed them, while entries are processed concurrently. Positions that `Sequence` did not list are never
published, so the expected and the published position values must match exactly, and a listed position that is never
completed holds back every position after it.

### Page documents

With `Paginate.addPageContent`, each page response is published at once to `<topic>-pages`, with content key `page`, at
the position held by `Paginate.positionVariable`.

### Resuming

`contentStream.lastOrInitialPosition(initial)` in a `SpecificationContext` variable reads the last published position
when the `Paginate` starts. A specification resumes from where the last run stopped by computing its first request from
that value, for example `${cast.toLong(contentStream.lastOrInitialPosition(0)) + 1}` for sequence numbers, or by
passing it to an API that returns the entries after a position. `lastOrInitialPagePosition` does the same with the page
topic.

### Prefetch

`Paginate.threshold` sets how far ahead pagination runs, counted in positions (`PrefetchAlgorithm`):

- `Sequence` reports how many positions a page expects, and `Publish` reports how many are published.
- When the number of expected positions not yet published drops below `threshold`, and no page fetch is pending, the
  next page is fetched from the last page's output.

The next page is fetched only after a `Sequence` has reported the current page's positions. With `threshold` 0 no
further page is fetched.

## Configuration keys that affect a specification

These keys are read from the worker's configuration (the server configuration). See
[administration](administration.md) for all keys.

| Key | Default | Effect |
|---|---|---|
| `data.collector.http.request.timeout.seconds` | `15` | Time to wait for a response. |
| `data.collector.http.client.timeout.seconds` | `20` | HTTP connect timeout. |
| `data.collector.http.request.retryCount` | `3` | Attempts per request. |
| `data.collector.http.request.napOnRetryInMillis` | `150` | Pause between attempts. When the key is set, `OperationHandler` reads the value of `data.collector.http.request.retryCount` instead. |
| `data.collector.http.version` | client default | `HTTP_1_1` or `HTTP_2`. |
| `data.collector.http.followRedirects` | client default | `NEVER`, `ALWAYS` or `NORMAL`. |
| `data.collector.parallel.queueBuffer.threshold` | not set | See [Parallel](#parallel). |
| `data.collector.worker.threads` | number of processors + 2 | Size of the worker thread pool that runs pages and `Parallel` entries. |
| `content.stream.connector` | `discarding` (the server's `application-defaults.properties` sets `rawdata`) | Content store provider. |

## Worked example

This example pages through an Atom-like XML feed: each page lists events, each event's document is fetched, and each
event is published as one position with two documents, `entry` and `event-doc`.

### Java

From `GetTest.thatPaginateHandlePages` in `data-collector-core` (static imports from `io.descoped.dc.api.Builders`):

```java
SpecificationBuilder specificationBuilder = Specification.start("test", "getPage", "page-loop")
        .configure(context()
                .topic("topic")
                .header("accept", "application/xml")
                .variable("baseURL", testServer.testURL(""))
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
                .url("${baseURL}/api/events?position=${fromPosition}&pageSize=10")
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
                        .pipe(execute("event-doc")
                                .inputVariable("eventId", xpath("/entry/event-id"))
                        )
                        .pipe(publish("${position}"))
                )
                .returnVariables("nextPosition")
        )
        .function(get("event-doc")
                .url("${baseURL}/api/events/${eventId}?type=event")
                .pipe(addContent("${position}", "event-doc"))
        );
```

The test runs it with an in-memory rawdata content store, first with an iteration limit, then a second time on the
same content store without the limit:

```java
Map<String, String> configurationMap = Map.of("content.stream.connector", "rawdata", "rawdata.client.provider", "memory");
ContentStore contentStore = ProviderConfigurator.configure(ContentStoreInitializer.class, configurationMap.get("content.stream.connector"), configurationMap);

Worker.WorkerBuilder worker = Worker.newBuilder()
        .specification(specificationBuilder)
        .configuration(Map.of("content.stream.connector", "rawdata", "rawdata.client.provider", "memory"))
        .contentStore(contentStore)
        .keepContentStoreOpenOnWorkerCompletion(true);

ExecutionContext output = worker.stopAtNumberOfIterations(5).build().run();

Worker worker2 = worker.build();
worker2.resetMaxNumberOfIterations();
ExecutionContext output2 = worker2.run();
```

### JSON

[`data-collector-server/src/main/resources/worker.config/page-test.json`](../data-collector-server/src/main/resources/worker.config/page-test.json)
is the same specification written by `serialize()`, with id `TEST` and name `paging`. `baseURL` holds the placeholder
`PORT`, which `DockerServerTest` replaces before it sends the file to `PUT /tasks`. The range
`success(200, 299)` is written as the known codes 200 to 206, and `fail(300, 599)` as the known codes from 300 to 505.
The `event-doc` function has no `responseValidators`; when it is built, it gets `status().success(200)`.

```json
{
  "type" : "Specification",
  "name" : "paging",
  "startFunctionId" : "page-loop",
  "id" : "TEST",
  "configure" : [ {
    "type" : "SpecificationContext",
    "variables" : {
      "baseURL" : "http://0.0.0.0:PORT",
      "nextPosition" : "${contentStream.lastOrInitialPosition(1)}"
    },
    "globalState" : {
      "global.topic" : "topic"
    },
    "headers" : {
      "accept" : [ "application/xml" ]
    }
  } ],
  "functions" : {
    "page-loop" : {
      "type" : "Paginate",
      "id" : "page-loop",
      "variables" : {
        "fromPosition" : "${nextPosition}"
      },
      "threshold" : 5,
      "addPageContent" : true,
      "positionVariable" : "fromPosition",
      "iterate" : [ {
        "type" : "Execute",
        "executeId" : "page"
      } ],
      "until" : {
        "type" : "ConditionWhenVariableIsNull",
        "identifier" : "nextPosition"
      }
    },
    "page" : {
      "type" : "Get",
      "id" : "page",
      "url" : "${baseURL}/api/events?position=${fromPosition}&pageSize=10",
      "requestHeaders" : { },
      "returnVariables" : [ "nextPosition" ],
      "responseValidators" : [ {
        "type" : "HttpStatusValidation",
        "success" : {
          "200" : [ ],
          "201" : [ ],
          "202" : [ ],
          "203" : [ ],
          "204" : [ ],
          "205" : [ ],
          "206" : [ ]
        },
        "failed" : [ 300, 301, 302, 303, 304, 305, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 500, 501, 502, 503, 504, 505 ]
      } ],
      "pipes" : [ {
        "type" : "Sequence",
        "splitQuery" : {
          "type" : "QueryXPath",
          "expression" : "/feed/entry"
        },
        "expectedQuery" : {
          "type" : "QueryXPath",
          "expression" : "/entry/id"
        }
      }, {
        "type" : "NextPage",
        "outputs" : {
          "nextPosition" : {
            "type" : "QueryRegEx",
            "expression" : "(?<=[?&]position=)[^&]*",
            "query" : {
              "type" : "QueryXPath",
              "expression" : "/feed/link[@rel=\"next\"]/@href"
            }
          }
        }
      }, {
        "type" : "Parallel",
        "variables" : {
          "position" : {
            "type" : "QueryXPath",
            "expression" : "/entry/id"
          }
        },
        "splitQuery" : {
          "type" : "QueryXPath",
          "expression" : "/feed/entry"
        },
        "pipes" : [ {
          "type" : "AddContent",
          "positionVariableExpression" : "${position}",
          "contentKey" : "entry"
        }, {
          "type" : "Execute",
          "executeId" : "event-doc",
          "inputVariables" : {
            "eventId" : {
              "type" : "QueryXPath",
              "expression" : "/entry/event-id"
            }
          }
        }, {
          "type" : "Publish",
          "positionVariableExpression" : "${position}"
        } ]
      } ]
    },
    "event-doc" : {
      "type" : "Get",
      "id" : "event-doc",
      "url" : "${baseURL}/api/events/${eventId}?type=event",
      "requestHeaders" : { },
      "pipes" : [ {
        "type" : "AddContent",
        "positionVariableExpression" : "${position}",
        "contentKey" : "event-doc"
      } ]
    }
  }
}
```

### YAML

`Specification.deserialize` reads the same structure written as YAML. The `page-loop` function, for example:

```yaml
page-loop:
  type: "Paginate"
  id: "page-loop"
  variables:
    fromPosition: "${nextPosition}"
  threshold: 5
  addPageContent: true
  positionVariable: "fromPosition"
  iterate:
  - type: "Execute"
    executeId: "page"
  until:
    type: "ConditionWhenVariableIsNull"
    identifier: "nextPosition"
```

### What happens

1. The worker starts `page-loop`. `PaginateHandler` evaluates `nextPosition` to the last published position of topic
   `topic`, or `1` on the first run.
2. For each page, `fromPosition` is set to the value of `nextPosition`, and `Execute` runs `page`.
3. `page` requests `${baseURL}/api/events?position=${fromPosition}&pageSize=10` with header `accept: application/xml`.
   Because of `addPageContent`, the response is published to `topic-pages` at position `fromPosition`.
4. `Sequence` splits the feed at `/feed/entry` and lists each entry's `/entry/id` as the page's expected positions.
5. `NextPage` takes the `href` of `<link rel="next">` and extracts its `position` parameter into `nextPosition`. The
   test server ends the feed with a page that has no entries and no such link. On that page `Sequence` raises end of
   stream, which ends the loop; the XPath in `NextPage` matches nothing there and fails, but the end of stream came
   first and wins (`OperationHandler.handleResponse`), so the `until` condition is not what stops this example.
6. `Parallel` runs for each entry concurrently. It sets `position` to `/entry/id`, buffers the entry as `entry`, runs
   `event-doc` with `eventId` from `/entry/event-id`, which buffers the event document as `event-doc` under the same
   position, and then publishes the position.
7. Positions are published to `topic` in feed order. When fewer than 5 positions are waiting to be published, the
   next page is fetched.

To run the JSON with the server, send it as the body of `PUT /tasks`; see [REST API](rest-api.md).

## Further examples

Real-world specifications in [`examples/specs`](../examples/specs). Every one is read and built by
`ExampleSpecificationsTest`.

| File | Shows |
|---|---|
| [`enhetsregisteret-test-spec.json`](../examples/specs/enhetsregisteret-test-spec.json) | JSON API paged with `QueryJqPath`, `QueryEval` for the next position |
| [`enhetsregisteret-updates-spec.json`](../examples/specs/enhetsregisteret-updates-spec.json) | A `Get` start function that computes inputs with `QueryEval` and executes a `Paginate`; `Console` |
| [`enhetsregisteret-download-spec.json`](../examples/specs/enhetsregisteret-download-spec.json) | One large JSON array streamed with `QueryJsonToken` |
| [`altinn3-test-spec.json`](../examples/specs/altinn3-test-spec.json) | Maskinporten JWT grant with `Post`, `JwtIdentity`, `QueryBody`, `ForEach`, `AddContent.state`, `Authorization` header |
| [`moveit-test-spec.json`](../examples/specs/moveit-test-spec.json) | `Post` with a plain-text body built from `ENV` values, `ConditionWhenExpressionIsTrue` |
| [`ske-freg-konsument-test-spec.json`](../examples/specs/ske-freg-konsument-test-spec.json) | XML feed with a business certificate (`Security.bundleName`), several documents per position |
| [`ske-freg-bulk-uttrekk-spec.json`](../examples/specs/ske-freg-bulk-uttrekk-spec.json) | `HttpStatusRetryWhile` |
| [`ske-sirius-person-fastsatt-spec.json`](../examples/specs/ske-sirius-person-fastsatt-spec.json) | `HttpStatusValidation` with `HttpResponseBodyContains` |
| [`toll-tvinn-test-spec.json`](../examples/specs/toll-tvinn-test-spec.json) | Maskinporten JWT grant and an XML feed |
