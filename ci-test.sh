#!/bin/bash -xe
command -v docker-compose || curl -L "https://github.com/docker/compose/releases/download/1.24.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/bin/docker-compose && chmod +x /usr/bin/docker-compose
make test
