#!/bin/bash -xe

IS_DEV_ENV=$1
if [ -z "$IS_DEV_ENV" ]; then
  printf "Incorrect usage."
  exit 1
fi

if [ "$IS_DEV_ENV" = 'false' ]; then
  ln -sf /usr/bin/python3.7 /usr/bin/python
  apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 9DA31620334BD75D9DCB49F368818C72E52529D4
fi
apt-get update
