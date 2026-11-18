# data-collector-cron-docker

Crontab time based scheduler for Data Collector.

## Configuration

* `DC_HOST` = data collector host
* `DC_PORT` = data collector port
* `CRON_EXPRESSION` = cron scheduler expression

## Test dev image

1. Go to `./data-collection-consumer-specifications` and run `make start-postgres-dev` (scripts below are using the network: `data-collection-consumer-specifications_default`)
1. Run `./build-dev.sh` (build dev image)
1. Run `./create-and-copy-spec.sh` (prepare volume with spec)
1. Run `./run-dev.sh` ()

> Edit the script environment variable if you want to test with other image, volume or network.
