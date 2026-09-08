# frontend/Dockerfile
FROM node:24-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install
COPY . .
# "production" swaps in environment.prod.ts (a real domain); "development" keeps
# environment.ts, which points at localhost — that is what docker-compose wants.
ARG NG_CONFIGURATION=development
# The production environment carries __APP_DOMAIN__ / __KEYCLOAK_DOMAIN__
# placeholders rather than hardcoded hosts, substituted here because Angular
# resolves its environment at build time — there is nothing to change on the
# server afterwards.
ARG APP_DOMAIN=shop.example.com
ARG KEYCLOAK_DOMAIN=auth.example.com
RUN sed -i "s/__APP_DOMAIN__/${APP_DOMAIN}/g; s/__KEYCLOAK_DOMAIN__/${KEYCLOAK_DOMAIN}/g" src/environment/environment.prod.ts
RUN npm run build -- --configuration=${NG_CONFIGURATION}

FROM nginx:alpine
WORKDIR /usr/share/nginx/html
# The application builder emits the browser bundle under dist/<project>/browser
COPY --from=builder /app/dist/portfolio-shop-frontend/browser/ .
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
