# docker build -t mattes/hello-world-nginx .
# docker run -p 8090:80 mattes/hello-world-nginx

FROM ubuntu:14.04
RUN apt-get update
RUN apt-get install -y nginx
ADD nginx.conf /etc/nginx/nginx.conf

ADD ./www-data /www-data

EXPOSE 80
CMD ["nginx"]
