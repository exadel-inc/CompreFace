#!/bin/bash

echo "🔍 DIAGNOSTIC COMPLET 1b COMPREFACE"
echo "======================================"
echo ""

echo "1️⃣ VÉRIFICATION DES CONTENEURS"
echo "------------------------------"
docker-compose ps
echo ""

echo "2️⃣ LOGS COMPLETS compreface-core (dernières 100 lignes)"
echo "--------------------------------------------------------"
docker-compose logs --tail=100 compreface-core
echo ""

echo "3️⃣ TEST MANUEL HEALTHCHECK compreface-core"
echo "------------------------------------------"
docker-compose exec -T compreface-core curl -s http://localhost:3000/healthcheck || echo "❌ Healthcheck FAILED"
echo ""

echo "4️⃣ VÉRIFICATION PROCESSUS DANS LE CONTENEUR"
echo "-------------------------------------------"
docker-compose exec -T compreface-core ps aux | head -20
echo ""

echo "5️⃣ UTILISATION MÉMOIRE/CPU"
echo "-------------------------"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" | grep compreface
echo ""

echo "6️⃣ LOGS DASHBOARD (dernières 50 lignes)"
echo "---------------------------------------"
docker-compose logs --tail=50 dashboard-service
echo ""

echo "7️⃣ TEST MANUEL HEALTHCHECK dashboard"
echo "------------------------------------"
curl -s http://localhost:5000/health || echo "❌ Dashboard health FAILED"
echo ""

echo "8️⃣ VÉRIFICATION BASE DE DONNÉES"
echo "-------------------------------"
docker-compose exec -T compreface-postgres-db psql -U postgres -d morocco_1bip_frs -c "SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema='public';" 2>/dev/null || echo "❌ DB check FAILED"
echo ""

echo "9️⃣ VÉRIFICATION CONNEXION API → CORE"
echo "------------------------------------"
docker-compose exec -T compreface-api curl -s http://compreface-core:3000/status 2>/dev/null || echo "❌ API → CORE connection FAILED"
echo ""

echo "🔟 ESPACE DISQUE DOCKER"
echo "----------------------"
docker system df
echo ""

echo "✅ DIAGNOSTIC TERMINÉ"
echo "===================="
