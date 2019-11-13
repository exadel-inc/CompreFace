FROM nginx:1.17.4
ADD nginx.conf /etc/nginx/nginx.conf
RUN apt update && apt install sleep
EXPOSE 5000
CMD ["sleep 100000"]
