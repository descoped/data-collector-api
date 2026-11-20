#!/usr/bin/env bash

docker compose exec -T postgresdb psql -v ON_ERROR_STOP=1 --username postgres --dbname postgres < init-db-and-user.sql
