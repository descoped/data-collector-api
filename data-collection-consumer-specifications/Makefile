SHELL=/bin/bash -x

.PHONY: default
default: | help

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

#
# docker-compose postgres
#

.PHONY: pull-postgres
pull-postgres: ## Pull postgres images
	@WORKDIR=$(PWD) PROFILE=content-stream-postgres DC_IMAGE=statisticsnorway/data-collector:latest docker-compose -f docker-compose-postgres.yml pull

.PHONY: start-postgres
start-postgres: ## Start postgres
	@WORKDIR=$(PWD) PROFILE=content-stream-postgres DC_IMAGE=statisticsnorway/data-collector:latest docker-compose -f docker-compose-postgres.yml up &

.PHONY: stop-postgres
stop-postgres: ## Stop postgres
	@WORKDIR=$(PWD) PROFILE=content-stream-postgres DC_IMAGE=statisticsnorway/data-collector:latest docker-compose -f docker-compose-postgres.yml down

.PHONY: stop-postgres-clean
stop-postgres-clean: ## Stop postgres and remove anonymous volumes
	@WORKDIR=$(PWD) PROFILE=content-stream-postgres DC_IMAGE=statisticsnorway/data-collector:latest docker-compose -f docker-compose-postgres.yml down -v

.PHONY: remove-postgres
remove-postgres: ## Remove postgres
	@WORKDIR=$(PWD) PROFILE=content-stream-postgres DC_IMAGE=statisticsnorway/data-collector:latest docker-compose -f docker-compose-postgres.yml rm

#
# docker-compose kafka
#

.PHONY: pull-kafka
pull-kafka: ## Pull kafka images
	@WORKDIR=$(PWD) PROFILE=content-stream-kafka DC_IMAGE=statisticsnorway/data-collector:latest docker-compose -f docker-compose-kafka.yml pull

.PHONY: start-kafka
start-kafka: ## Start kafka
	@WORKDIR=$(PWD) PROFILE=content-stream-kafka DC_IMAGE=statisticsnorway/data-collector:latest docker-compose -f docker-compose-kafka.yml up &

.PHONY: stop-kafka
stop-kafka: ## Stop kafka
	@WORKDIR=$(PWD) PROFILE=content-stream-kafka DC_IMAGE=statisticsnorway/data-collector:latest docker-compose -f docker-compose-kafka.yml down

.PHONY: stop-kafka-clean
stop-kafka-clean: ## Stop kafka and remove anonymous volumes
	@WORKDIR=$(PWD) PROFILE=content-stream-kafka DC_IMAGE=statisticsnorway/data-collector:latest docker-compose -f docker-compose-kafka.yml down -v

.PHONY: remove-kafka
remove-kafka: ## Remove kafka
	@WORKDIR=$(PWD) PROFILE=content-stream-kafka DC_IMAGE=statisticsnorway/data-collector:latest docker-compose -f docker-compose-kafka.yml rm

#
# build data collector dev image
#

.PHONY: build-data-collector-dev-image
build-data-collector-dev-image: ## Build data collector dev image
	@cd .. && mvn clean install -DskipTests && cd data-collector-docker && mvn clean verify dependency:copy-dependencies -DskipTests && docker build -t data-collector:dev -f Dockerfile-dev .

#
# docker-compose postgres-dev
#

.PHONY: start-postgres-dev
start-postgres-dev: ## Start postgres-dev
	@WORKDIR=$(PWD) PROFILE=content-stream-postgres DC_IMAGE=data-collector:dev docker-compose -f docker-compose-postgres.yml up &

.PHONY: stop-postgres-dev
stop-postgres-dev: ## Stop postgres-dev
	@WORKDIR=$(PWD) PROFILE=content-stream-postgres DC_IMAGE=data-collector:dev docker-compose -f docker-compose-postgres.yml down

.PHONY: stop-postgres-dev-clean
stop-postgres-dev-clean: ## Stop postgres-dev and remove anonymous volumes
	@WORKDIR=$(PWD) PROFILE=content-stream-postgres DC_IMAGE=data-collector:dev docker-compose -f docker-compose-postgres.yml down -v

.PHONY: remove-postgres-dev
remove-postgres-dev: ## Remove postgres-dev
	@WORKDIR=$(PWD) PROFILE=content-stream-postgres DC_IMAGE=data-collector:dev docker-compose -f docker-compose-postgres.yml rm

#
# docker-compose kafka-dev
#

.PHONY: start-kafka-dev
start-kafka-dev: ## Start kafka-dev
	@WORKDIR=$(PWD) PROFILE=content-stream-kafka DC_IMAGE=data-collector:dev docker-compose -f docker-compose-kafka.yml up &

.PHONY: stop-kafka-dev
stop-kafka-dev: ## Stop kafka-dev
	@WORKDIR=$(PWD) PROFILE=content-stream-kafka DC_IMAGE=data-collector:dev docker-compose -f docker-compose-kafka.yml down

.PHONY: stop-kafka-dev-clean
stop-kafka-dev-clean: ## Stop kafka-dev and remove anonymous volumes
	@WORKDIR=$(PWD) PROFILE=content-stream-kafka DC_IMAGE=data-collector:dev docker-compose -f docker-compose-kafka.yml down -v

.PHONY: remove-kafka-dev
remove-kafka-dev: ## Remove kafka-dev
	@WORKDIR=$(PWD) PROFILE=content-stream-kafka DC_IMAGE=data-collector:dev docker-compose -f docker-compose-kafka.yml rm

#
# execute consumer specifications
#

.PHONY: collect-freg-playground
collect-freg-playground: ## Collect freg playground
	@curl -X PUT localhost:9090/task -H 'content-type: application/json' -d @specs/ske-freg-playground-spec.json

.PHONY: collect-sirius-person-utkast
collect-sirius-person-utkast: ## Collect sirius person utkast
	@curl -X PUT localhost:9090/task -H 'content-type: application/json' -d @specs/ske-sirius-person-utkast-spec.json

.PHONY: collect-sirius-person-fastsatt
collect-sirius-person-fastsatt: ## Collect sirius person fastsatt
	@curl -X PUT localhost:9090/task -H 'content-type: application/json' -d @specs/ske-sirius-person-fastsatt-spec.json

.PHONY: collect-tvinn
collect-tvinn: ## Collect tvinn
	@curl -X PUT localhost:9090/task -H 'content-type: application/json' -d @specs/toll-tvinn-test-spec.json

