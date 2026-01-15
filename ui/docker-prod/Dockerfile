FROM node:22-alpine AS build
WORKDIR /usr/src/app
LABEL intermidiate_frs=true
COPY . .
RUN npm install
RUN npm run build:prod
### STAGE 2: Run ###
FROM nginx:1.27-alpine
COPY --from=build /usr/src/app/dist/compreface/browser /usr/share/nginx/html
RUN rm /etc/nginx/conf.d/default.conf
COPY --from=build /usr/src/app/nginx/ /etc/nginx/
