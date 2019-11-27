FROM nginx:1.17.4
RUN apt update -y && apt install openssl -y
RUN openssl req -newkey rsa:2048 -nodes -keyout /etc/nginx/msqv500.exadel.by.key -x509 -days 365 -out /etc/nginx/msqv500.exadel.by.crt -subj "/C=UA/ST=Kharkiv/L=Kharkiv/O=Exadel Network/OU=DevOps Department/CN=msqv500.exadel.by"
ADD nginx.conf /etc/nginx/nginx.conf
EXPOSE 5000 443
CMD ["nginx", "-g", "daemon off;"]
