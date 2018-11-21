#!/bin/bash

set -e
set -x

TAR_FILE=helm-v2.10.0-linux-amd64.tar.gz

wget https://storage.googleapis.com/kubernetes-helm/$TAR_FILE
tar -zxf $TAR_FILE
sudo cp linux-amd64/helm /usr/local/bin/helm
