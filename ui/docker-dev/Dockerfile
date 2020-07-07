FROM node:12.7-alpine AS build
WORKDIR /usr/src/app
LABEL intermidiate_frs=true
COPY . .
#nothing is needed here so far
RUN echo "Hello from dev docker"
#RUN rm /etc/nginx/conf.d/default.conf
FROM nginx:1.19.0
RUN rm /etc/nginx/conf.d/default.conf
COPY --from=build /usr/src/app/nginx/ /etc/nginx/
