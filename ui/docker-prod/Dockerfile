FROM node:12.7-alpine AS build
WORKDIR /usr/src/app
LABEL intermidiate_frs=true
COPY . .
RUN npm install
RUN npm run build:prod
### STAGE 2: Run ###
FROM nginx:1.21.1
COPY --from=build /usr/src/app/dist/compreface /usr/share/nginx/html
RUN rm /etc/nginx/conf.d/default.conf
COPY --from=build /usr/src/app/nginx/ /etc/nginx/
