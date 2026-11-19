#!/usr/bin/env bash

(cd .. && ./mvnw -pl data-collector-server -am clean verify dependency:copy-dependencies -DskipTests) &&\
docker build -t data-collector:dev -f Dockerfile-dev .
