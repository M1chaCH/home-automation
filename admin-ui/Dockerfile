FROM node:lts-alpine3.14 as admin-ui-build

ARG ngConfig="development"

COPY . /var/local/admin-ui/
WORKDIR /var/local/admin-ui/

RUN npm install
RUN npm run build -- --configuration=$ngConfig

FROM nginx:stable as admin-ui

COPY --from=admin-ui-build /var/local/admin-ui/dist/admin-ui /usr/share/nginx/html

EXPOSE 80
EXPOSE 443