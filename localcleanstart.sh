git checkout develop
git pull
cd embedding-calculator
dos2unix * ml/* e2e/* ml/tools/*
cd ..
docker-compose down
docker-compose up --build
