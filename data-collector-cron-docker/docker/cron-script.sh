#!/usr/bin/env bash

#
# Check requires environement variables

if [ ! -n "$DC_HOST" ]
then
  echo "DC_HOST environement variable is NOT set!"
  exit 1
fi

if [ ! -n "$DC_PORT" ]
then
  echo "DC_PORT environement variable is NOT set!"
  exit 1
fi

echo "Data Collector Endpoint: $DC_HOST:$DC_PORT"

#
# Ping server
#

PING_HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" $DC_HOST:$DC_PORT/ping)

if [ ! "$PING_HTTP_STATUS" == "200" ]
then
  echo "'ping $DC_HOST:$DC_PORT' failed with http error: $PING_HTTP_STATUS"
  exit 1
fi

# Run data collector task

echo "Put task"

curl -X PUT $DC_HOST:$DC_PORT/tasks -H 'content-type: application/json' -d @/spec/spec.json

