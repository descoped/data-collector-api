#!/bin/bash

#
# Set default environement variables
#

if [ ! -n "$API_CONSUMER_SPECIFICATION_FILE" ]
then
  API_CONSUMER_SPECIFICATION_FILE=./ske-sirius-person-utkast-spec.json
fi

CONTAINER_NAME=data-collector-cron-fs
CONTAINER_VOLUME_NAME=data-collector-cron-volume

#
# Create volume if npt exists
#

VOLUME_EXISTS=$(docker volume ls -q | grep $CONTAINER_VOLUME_NAME)

if [ "$VOLUME_EXISTS" == "" ]
then
	docker volume create $CONTAINER_VOLUME_NAME
fi

#
# Run docker container and mount volume
#

CONTAINER_CID=$(docker ps | awk '/'$CONTAINER_NAME'/{print $1}')

if [ ! -n "$CONTAINER_CID" ]
then
  docker run -d --rm --name $CONTAINER_NAME -v $CONTAINER_VOLUME_NAME:/spec alpine tail -f /dev/null
  sleep 2
fi

#
# Copy api consumer specification to volume
#

docker cp $API_CONSUMER_SPECIFICATION_FILE $CONTAINER_NAME:/spec/spec.json
docker exec $CONTAINER_NAME ls -l /spec/spec.json

#
# Stop docker container with mounted volume
#

CONTAINER_CID=$(docker ps | awk '/'$CONTAINER_NAME'/{print $1}')
docker stop $CONTAINER_CID

VOLUME_EXISTS=$(docker volume ls -q | grep $CONTAINER_VOLUME_NAME)

if [ -n "$VOLUME_EXISTS" ]
then
	echo "Docker volume $CONTAINER_VOLUME_NAME is prepared."
fi
