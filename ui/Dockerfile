FROM node:12.7-alpine AS build
WORKDIR /usr/src/app
LABEL intermidiate_frs=true
COPY . .
RUN npm install
RUN npm run build:prod
### STAGE 2: Run ###
FROM nginx:1.19.0
COPY --from=build /usr/src/app/dist/frs-client-fe /usr/share/nginx/html
COPY --from=build /usr/src/app/config/nginx.conf /etc/nginx/nginx.conf
