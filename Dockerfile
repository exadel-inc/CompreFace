FROM nginx:1.17.4
ADD nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx"]
