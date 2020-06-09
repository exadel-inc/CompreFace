git checkout origin/develop
git pull
cd embedding-calculator
dos2unix * ml/* e2e/* ml/tools/*
cd ..
docker-compose -f docker-compose-dev-db.yml up --build
