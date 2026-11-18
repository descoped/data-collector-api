#!/usr/bin/env bash

PASSPHRASE=$(cat `pwd`/certs/ske-test-certs/secret.properties | grep "secret.passphrase=" | cut -d'=' -f2) && curl --cert `pwd`/certs/ske-test-certs/testauth-public.pem:$PASSPHRASE --key `pwd`/certs/ske-test-certs/testauth-key.pem -kvi -o /dev/null https://folkeregisteret-api-konsument-playground.sits.no/folkeregisteret/offentlig-med-hjemmel/api/v1/hendelser/feed/?seq=1

