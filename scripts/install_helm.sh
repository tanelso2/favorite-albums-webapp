#!/bin/bash

set -e
set -x

wget https://storage.googleapis.com/kubernetes-helm/helm-v2.11.0-linux-amd64.tar.gz
tar -zxf helm-v2.11.0-linux-amd64.tar.gz
sudo cp linux-amd64/helm /usr/local/bin/helm
