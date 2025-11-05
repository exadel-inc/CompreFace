#!/bin/bash


echo " ARRÊT DE TOUS LES SERVICES..."
docker-compose down

echo ""
echo "🗑️  SUPPRESSION DES VOLUMES ANCIENS (base de données)..."
docker volume rm comprefacemodeling_postgres-data 2>/dev/null || true
docker volume rm comprefacemodeling_embedding-data 2>/dev/null || true

echo ""
echo " NETTOYAGE DES CONTENEURS ARRÊTÉS..."
docker container prune -f

echo ""
echo " Réinitialisation terminée!"
echo ""
echo " PROCHAINES ÉTAPES:"
echo "1. Démarrer les services: docker-compose up -d"
echo "2. Attendre 3 minutes pour l'initialisation"
echo "3. Accéder à http://localhost:8000"
echo ""
