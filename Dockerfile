FROM nginx:1.17.4
ADD nginx.conf /etc/nginx/nginx.conf
EXPOSE 5000
CMD ["sleep 100000"]
