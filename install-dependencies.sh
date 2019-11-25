#!/bin/bash -xe

if [ -z "$1" ]; then
  printf "Incorrect usage."
  exit 1
fi
IS_DEV_ENV=$1

if [ "$IS_DEV_ENV" = 'false' ]; then
  apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 9DA31620334BD75D9DCB49F368818C72E52529D4
  echo "deb [ arch=amd64 ] https://repo.mongodb.org/apt/ubuntu bionic/mongodb-org/4.0 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-4.0.list
  apt-get update
  apt-get install -y mongodb-org-tools mongodb-org-shell
else
  apt-get update
  apt-get install -y mongo-tools mongodb-clients
fi
