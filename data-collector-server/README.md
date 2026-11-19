# Data Collector Server

![Renovate](https://img.shields.io/badge/renovate-enabled-brightgreen.svg)

For more information about Data Collector, please refer to
the [Data Collector documentation](https://github.com/descoped/data-collector-project).


## Build

```
cd data-collector-project
mvn clean install
```

### Build and release docker-dev image

> :bulb: To determine next version manually, check latest version
> at [Docker Hub](https://cloud.docker.com/u/descoped/repository/docker/descoped/data-collector/tags)

> :warning: Versioning must follow: https://semver.org

```
cd data-collector-server

mvn -B clean install -DskipTests && mvn -B dependency:copy-dependencies

docker login -u USERNAME

docker build --no-cache -t descoped/data-collector:MAJOR.MINOR.PATCH -f ./Dockerfile-dev .
docker push descoped/data-collector:MAJOR.MINOR.PATCH
docker tag descoped/data-collector:MAJOR.MINOR.PATCH descoped/data-collector:latest
docker push descoped/data-collector:latest
```

### Pull and run Data Collector

```
docker pull descoped/data-collector:latest
docker run -it -p 9990:9990 -v $PWD/conf:/conf -v $PWD/certs:/certs -v /tmp/rawdata:/rawdata descoped/data-collector:latest
```

## Build dev

```
./build-dev.sh

docker run -it -p 9990:9990 data-collector:dev
```

## Build dev and Visual VM Profiling

Visual VM can be connected to `io.descoped.dc.server.Server` or using `data-collector:dev` image.

Follow these instructions for setting up profiling.

### Run `Server` from IntelliJ

1. Configure `Server` (see enum inside)
1. Consult readme in `data-collection-consumer-specifications`-repo regarding GCS configuration
1. Run `Server` in IntelliJ using Visual VM plugin
1. `make collect-freg-playground`

### Docker-dev (`data-collection-consumer-specifications`)

> Consult readme in `data-collection-consumer-specifications`-repo regarding GCS configuration.

1. `make build-data-collector-dev-image`
1. `make start-gcs-dev`
1. `make tail-gcs-dev`
1. [Download and open Visual VM](https://visualvm.github.io/) (standard version, not Graal download)
1. File --> Add Remote Host ("0.0.0.0")
1. In left pane, right click "0.0.0.0" and Choose "Add JMX Connection"
1. Set port to 9992
1. `make collect-freg-playground`

## Configuration

The server reads, in this order, the later one winning: `application-defaults.properties` in its jar,
`/conf/application-defaults.properties`, `/conf/application.properties` and `/conf/application-override.properties`,
then `DC_` environment variables and system properties.

**Environment variables and system properties only override keys a properties file declares**; they do not add keys.
`DC_RAWDATA_ENCRYPTION_KEY` (or `DC_rawdata.encryption.key`) overrides `rawdata.encryption.key`. The jar's defaults
declare every key an operator sets, most of them empty, and an empty value reads as unset. A `DC_RAWDATA_ENCRYPTION_*`
or `DC_DATA_COLLECTOR_SSLBUNDLE_*` variable that matches no declared key stops the server, so encryption and
certificates are never switched off by a misspelt or undeclared variable; other unmatched `DC_` variables are logged and
ignored. To set a key the defaults do not declare, put it in `/conf/application.properties`.

### Google Secret Manager SSL properties

```
data.collector.sslBundle.provider=google-secret-manager
data.collector.sslBundle.gcp.projectId=SECRET_MANAGER_PROJECT_ID
data.collector.sslBundle.gcp.serviceAccountKeyPath=(optional)
data.collector.sslBundle.type=(pem | p12)
data.collector.sslBundle.name=ssb-prod-certs
data.collector.sslBundle.publicCertificate=secretName
data.collector.sslBundle.privateCertificate=secretName
data.collector.sslBundle.archiveCertificate=secretName
data.collector.sslBundle.passphrase=secretNam
```

### Rawdata Encryption Credentials

```
rawdata.encryption.provider = (dynamic-configuration | google-secret-manager), optional
rawdata.encryption.key  = the password, or with google-secret-manager the name of the secret that holds it
rawdata.encryption.salt = the salt, at least 16 bytes, or with google-secret-manager the name of its secret
rawdata.encryption.gcp.projectId = google-secret-manager: the project (required)
rawdata.encryption.gcp.serviceAccountKeyPath = google-secret-manager: a service-account key (Application Default
                                               Credentials when absent); dynamic-configuration: a properties file
                                               holding rawdata.encryption.key and rawdata.encryption.salt
```

Without a provider, the key and salt are read from the configuration. Encryption fails closed: a key without a salt, a
salt without a key, an empty secret or an unknown provider stops the server instead of writing plaintext.

Content is encrypted with AES-256-GCM (encryption-util 3, ciphertext format 2). The key is derived from the password
and salt with PBKDF2-HMAC-SHA256 and 600 000 iterations. Readers of an encrypted topic need encryption-util 3 and the
same derivation.

### Logstash

Set environment variable `LOGBACK_CONFIGURATION_FILE=/opt/dc/log4j2-logstash.xml` to enable structured logging using
Logstash.

.
