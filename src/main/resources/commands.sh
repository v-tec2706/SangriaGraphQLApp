#docker rm $(docker ps | grep postgres | awk '{ print $1 }')
docker run -p 5432:5432 --name sangria-app-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=mydb -d postgres