#!/bin/bash

docker run -p 80:80 -v $(pwd)/config:/etc/nginx/conf.d --name dev_proxy --detach nginx
