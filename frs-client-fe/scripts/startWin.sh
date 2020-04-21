#!/bin/bash

# for Win OS
MSYS_NO_PATHCONV=1 docker run -p 80:80 -v $(pwd)/config:/etc/nginx/conf.d --name dev_proxy --detach nginx
