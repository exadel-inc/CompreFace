#sed -i "s|%COMMIT_HASH%|$(git rev-parse --short HEAD)|g" ui/src/app/features/footer/footer.component.html
docker-compose -f docker-compose.yml -f docker-compose.dev.ui.yml up --build &
( cd ../ui && npm run start )
