# MOD Data Collection Consumer Specifications

## Pre-requisite

Business SSL bundles should be placed under `certs` directory.

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

> Contact Rune Lind or Kenneth Schulstad for getting Cert-bundles.

# Intro

This project uses Makefile to control the docker-compose lifecycle and executing data collector specifications.

# Use

## Getting available targets

```
make
```

## Run target

```
make TARGET
```

# Docker Compose Lifecycle

## Postgres Stream Provider

|Target                         |Description                                     |
|:------------------------------|:-----------------------------------------------|
|pull-postgres                  |Pull postgres images                            |
|start-postgres                 |Start postgres                                  |
|stop-postgres                  |Stop postgres                                   |
|stop-postgres-clean            |Stop postgres and remove anonymous volumes      |
|remove-postgres                |Remove postgres                                 |

## Kafka Stream Provider

|Target                         |Description                                     |
|:------------------------------|:-----------------------------------------------|
|pull-kafka                     |Pull kafka images                               |
|start-kafka                    |Start kafka                                     |
|stop-kafka                     |Stop kafka                                      |
|stop-kafka-clean               |Stop kafka and remove anonymous volumes         |
|remove-kafka                   |Remove kafka                                    |

## Build Data Collector Dev Image

|Target                         |Description                                     |
|:------------------------------|:-----------------------------------------------|
|build-data-collector-dev-image |Build data collector dev image                  |

## Postgres Dev Stream Provider

|Target                         |Description                                     |
|:------------------------------|:-----------------------------------------------|
|start-postgres-dev             |Start postgres-dev                              |
|stop-postgres-dev              |Stop postgres-dev                               |
|stop-postgres-dev-clean        |Stop postgres-dev and remove anonymous volumes  |
|remove-postgres-dev            |Remove postgres-dev                             |

## Kafka Dev Stream Provider

|Target                         |Description                                     |
|:------------------------------|:-----------------------------------------------|
|start-kafka-dev                |Start kafka-dev                                 |
|stop-kafka-dev                 |Stop kafka-dev                                  |
|stop-kafka-dev-clean           |Stop kafka-dev and remove anonymous volumes     |
|remove-kafka-dev               |Remove kafka-dev                                |

# Run worker (Rawdata Producer)

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