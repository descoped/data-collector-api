#!/usr/bin/env bash

cd "$(dirname "$0")/../.."
WORKDIR=$PWD PROFILE=postgres docker compose -f docker-compose-postgres.yml exec -T dc-postgres-db psql -v ON_ERROR_STOP=1 --username postgres --dbname postgres < profile/postgres/init-db-and-user.sql
