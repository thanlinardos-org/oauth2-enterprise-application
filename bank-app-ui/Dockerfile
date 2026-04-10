# Stage 1: Build the Angular application
FROM node:alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install
COPY package.json package-lock.json ./
COPY angular.json tsconfig*.json ./
COPY src/ ./src/
COPY nginx/ ./nginx
COPY .env local_angular_ui_client.crt local_angular_ui_client.key ./
RUN npm run build

# Stage 2: Serve the application with Nginx
FROM nginx:alpine

# Ensure SSL is available and create SSL directory
# Set proper ownership and permissions
RUN mkdir -p /etc/nginx/ssl && \
    mkdir -p /var/run/nginx && \
    chown -R nginx:nginx /var/run/nginx && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /etc/nginx/conf.d && \
    chmod -R 755 /usr/share/nginx/html

# Copy SSL certificates
COPY local_angular_ui_client.crt /etc/nginx/ssl/local_angular_ui_client.crt
COPY local_angular_ui_client.key /etc/nginx/ssl/local_angular_ui_client.key

RUN chown -R nginx:nginx /etc/nginx/ssl && \
    chown nginx:nginx /etc/nginx/ssl/local_angular_ui_client.crt && \
    chown nginx:nginx /etc/nginx/ssl/local_angular_ui_client.key && \
    chmod 600 /etc/nginx/ssl/local_angular_ui_client.crt && \
    chmod 600 /etc/nginx/ssl/local_angular_ui_client.key

COPY --from=build /app/dist/bank-app-ui /usr/share/nginx/html
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

RUN sed -i '/^[[:space:]]*pid[[:space:]]\+/d' /etc/nginx/nginx.conf && \
    echo "pid /var/run/nginx/nginx.pid;" >> /etc/nginx/nginx.conf && \
    sed -i 's/^user  nginx/#user  nginx/' /etc/nginx/nginx.conf

USER nginx

EXPOSE 443
CMD ["nginx", "-g", "daemon off;"]
