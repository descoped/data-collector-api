# MOD Data Collection Consumers

## Pre-requisite

Business SSL bundles should be placed under `./data-collector/certs` directory.

```
certs
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


# Pull latest images 

```
docker-compose pull
```

# Start 

```
docker-compose up
```

# Stop 

```
docker-compose down

docker-compose down -v (remove anonymous db volume)
```

# Clean

```
docker-compose rm
```

# Run worker (Rawdata Producer)

## Skatteetaten

### Sirius Person Utkast (synthetic testdata)

```
curl -X PUT localhost:9090/task -H 'content-type: application/json' -d @specs/ske-sirius-person-utkast-spec.json
```

Doc: https://skatteetaten.github.io/datasamarbeid-api-dokumentasjon/reference_skattemelding

### Sirius Person Fastsatt (synthetic testdata)

```
curl -X PUT localhost:9090/task -H 'content-type: application/json' -d @specs/ske-sirius-person-fastsatt-spec.json
```

Doc: https://skatteetaten.github.io/datasamarbeid-api-dokumentasjon/reference_skattemelding

### Freg Nytt folkeregister (synthetic playground testdata)

```
curl -X PUT localhost:9090/task -H 'content-type: application/json' -d @specs/ske-freg-playground-spec.json
```

Doc: https://skatteetaten.github.io/folkeregisteret-api-dokumentasjon/oppslag/

## Tolletaten

### Tvinn Tolldeklarasjoner (synthetic testdata)

```
curl -X PUT localhost:9090/task -H 'content-type: application/json' -d @specs/toll-tvinn-test-spec.json
```

Doc: contact Olaf Hansen (SSB)

# Rawdata Consumer

Please refer to the Rawdata Client configuration in `conf/application.properties` for pub/sub details. Also refer to the [Rawdata Client project](https://github.com/statisticsnorway/rawdata-client-project) for technical information.
