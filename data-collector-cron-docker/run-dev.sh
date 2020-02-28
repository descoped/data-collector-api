#!/usr/bin/env bash

DATA_COLLECTOR_HOST=data-collector-postgres
DATA_COLLECTOR_PORT=9990
DATA_COLLECTOR_NETWORK=data-collection-consumer-specifications_default
DATA_COLLECTOR_CRON_VOLUME=data-collector-cron-volume

docker run -it -e DC_HOST=$DATA_COLLECTOR_HOST -e DC_PORT=9990 -e CRON_EXPRESSION="* * * * *" --net $DATA_COLLECTOR_NETWORK -v $DATA_COLLECTOR_CRON_VOLUME/spec data-collector-cron-job:dev
