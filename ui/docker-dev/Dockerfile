FROM node:22-alpine AS build
WORKDIR /usr/src/app
LABEL intermidiate_frs=true
COPY . .
#nothing is needed here so far
RUN echo "Hello from dev docker"
#RUN rm /etc/nginx/conf.d/default.conf
FROM nginx:1.27-alpine
RUN rm /etc/nginx/conf.d/default.conf
COPY --from=build /usr/src/app/nginx/ /etc/nginx/
