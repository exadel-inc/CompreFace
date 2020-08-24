git checkout develop
git pull
docker-compose -f docker-compose.yml up --build --scale frs-postgres-db=0
