#!/bin/bash

set -e
set -x

export GOOGLE_APPLICATION_CREDENTIALS="./service-account.json"



gcloud auth configure-docker --quiet
docker pull gcr.io/kubernetes-221218/favorite-albums:latest
