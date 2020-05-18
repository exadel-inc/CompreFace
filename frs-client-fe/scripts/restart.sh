#!/bin/bash

docker kill dev_proxy
docker rm dev_proxy
docker run -p 8000:80 -v $(pwd)/config:/etc/nginx/conf.d --name dev_proxy --detach nginx
