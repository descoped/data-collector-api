#!/usr/bin/env bash

gcloud auth activate-service-account --key-file=$PWD/certs/gcs-secret/gcs_sa_test.json

echo "gsutil ls -l -r gs://dev-datalager-store/freg-playground"
