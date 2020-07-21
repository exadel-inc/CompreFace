docker-compose -f docker-compose.yml -f docker-compose.dev.ui.yml up --build &
( cd ui && npm run start )