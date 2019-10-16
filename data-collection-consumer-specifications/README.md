# MOD Data Collection Consumer Specifications

# Intro

This project provides a convenient way to control the lifecycle for different stream provider configurations and makes it easy to execute data collector specifications.

## Pre-requisite

Business SSL bundles are required for service authentication. The bundles must be placed under `certs` directory.

```
/certs
├── ske-test-certs (bundle)
│   ├── secret.properties
│   ├── testauth-key.pem
│   └── testauth-public.pem
├── toll-test-certs  (bundle)
│   ├── secret.properties
│   ├── testauthcert-key.pem
│   └── testauthcert-public.pem
```

> :key: **Please note**: Cert-bundles can be provided by Team Innsamling.

# Use

## Getting available targets

```
make
```

## Run target

```
make TARGET
```

> :bulb: **Advise**: Tail the running container-log in a separate terminal window.

# Docker Compose Lifecycle

> :warning: **Limitation**: only one _target provider_ can be run at a given time!

## Postgres Stream Provider

> :bulb: **Important notice**: Postgres is a recommended provider for test purposes.

|Target                         |Description                                     |
|:------------------------------|:-----------------------------------------------|
|pull-postgres                  |Pull images                                     |
|start-postgres                 |Start postgres                                  |
|tail-postgres                  |Tail docker log                                 |
|stop-postgres                  |Stop postgres                                   |
|stop-postgres-clean            |Stop postgres and remove anonymous volumes      |
|remove-postgres                |Remove postgres                                 |
|open-postgres-adminer          |Open a web based DB admin tool in your browser  |

## Kafka Stream Provider

> :warning: **Important notice**: The Kafka provider IS NOT a deployment target for MOD Sirius.

|Target                         |Description                                     |
|:------------------------------|:-----------------------------------------------|
|pull-kafka                     |Pull images                                     |
|start-kafka                    |Start kafka                                     |
|tail-kafka                     |Tail docker log                                 |
|stop-kafka                     |Stop kafka                                      |
|stop-kafka-clean               |Stop kafka and remove anonymous volumes         |
|remove-kafka                   |Remove kafka                                    |

## Build Data Collector Dev Image

Dev images requires full checkout of the [data-collector-project](https://github.com/statisticsnorway/data-collector-project) repo.

|Target                         |Description                                     |
|:------------------------------|:-----------------------------------------------|
|build-data-collector-dev-image |Build data collector dev image                  |

## Postgres Dev Stream Provider

|Target                         |Description                                     |
|:------------------------------|:-----------------------------------------------|
|start-postgres-dev             |Start postgres-dev                              |
|tail-postgres-dev              |Tail docker log                                 |
|stop-postgres-dev              |Stop postgres-dev                               |
|stop-postgres-dev-clean        |Stop postgres-dev and remove anonymous volumes  |
|remove-postgres-dev            |Remove postgres-dev                             |

## Kafka Dev Stream Provider

|Target                         |Description                                     |
|:------------------------------|:-----------------------------------------------|
|start-kafka-dev                |Start kafka-dev                                 |
|tail-kafka-dev                 |Tail docker log                                 |
|stop-kafka-dev                 |Stop kafka-dev                                  |
|stop-kafka-dev-clean           |Stop kafka-dev and remove anonymous volumes     |
|remove-kafka-dev               |Remove kafka-dev                                |

# Rawdata Producer (execute task)

## Execute consumer specification on running data collector container

|Target                         |Supplier     |Description                                     |
|:------------------------------|:-----------:|:-----------------------------------------------|
|collect-freg-playground        |Skatteetaten |Collect freg playground                         |
|collect-sirius-person-utkast   |Skatteetaten |Collect sirius person utkast                    |
|collect-sirius-person-fastsatt |Skatteetaten |Collect sirius person fastsatt                  |
|collect-tvinn                  |Tolletaten   |Collect tvinn                                   |


# Rawdata Consumer

Please refer to the Rawdata Client configuration in `conf/application.properties` for pub/sub details. Also refer to the [Rawdata Client project](https://github.com/statisticsnorway/rawdata-client-project) for technical information.

# References

* FREG: https://skatteetaten.github.io/folkeregisteret-api-dokumentasjon/oppslag/
* SIRIUS: https://skatteetaten.github.io/datasamarbeid-api-dokumentasjon/reference_skattemelding