docker rm -f angularui.local
docker run -d -p 4200:443 --name angularui.local angular-nginx-app
