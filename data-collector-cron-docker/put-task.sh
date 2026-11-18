#!/bin/sh

docker run -it -e DC_HOST=data-collector-postgres -e DC_PORT=9990 --net data-collection-consumer-specifications_default -v data-collector-cron-volume:/spec data-collector-cron:dev
