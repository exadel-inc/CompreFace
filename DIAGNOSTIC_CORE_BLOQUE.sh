#!/bin/bash

# Script de diagnostic complet pour compreface-core bloqué

echo "🔍 DIAGNOSTIC COMPREFACE-CORE BLOQUÉ"
echo "===================================="
echo ""

echo "Test 1: Processus dans le conteneur"
echo "-----------------------------------"
docker-compose exec -T compreface-core ps aux 2>/dev/null || echo "❌ Impossible d'exécuter ps"
echo ""

echo "Test 2: Test connexion port 3000"
echo "--------------------------------"
timeout 5 docker-compose exec -T compreface-core curl -s http://localhost:3000/healthcheck 2>/dev/null && echo "✅ Port 3000 répond" || echo "❌ Port 3000 ne répond pas"
echo ""

echo "Test 3: Fichiers de log"
echo "-----------------------"
docker-compose exec -T compreface-core ls -la /var/log/ 2>/dev/null || echo "Pas de logs système"
echo ""

echo "Test 4: Erreurs dans les logs Docker"
echo "------------------------------------"
docker-compose logs compreface-core 2>&1 | grep -i "error\|exception\|failed\|killed\|segfault" | tail -10
echo ""

echo "Test 5: Utilisation CPU/RAM"
echo "---------------------------"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" | grep compreface-core
echo ""

echo "Test 6: Vérifier si Python est chargé"
echo "-------------------------------------"
docker-compose exec -T compreface-core pgrep -l python 2>/dev/null && echo "✅ Processus Python trouvés" || echo "❌ Aucun processus Python"
echo ""

echo "Test 7: Strace du processus principal (10 secondes)"
echo "---------------------------------------------------"
MAIN_PID=$(docker-compose exec -T compreface-core pgrep -o uwsgi 2>/dev/null)
if [ ! -z "$MAIN_PID" ]; then
    echo "PID principal: $MAIN_PID"
    timeout 10 docker-compose exec -T compreface-core strace -p $MAIN_PID 2>&1 | head -20
else
    echo "❌ Impossible de trouver le PID principal"
fi
echo ""

echo "✅ DIAGNOSTIC TERMINÉ"
echo "===================="
echo ""
echo "ANALYSE:"
echo "--------"
echo "- Si aucun processus Python: Crash au démarrage de l'app"
echo "- Si processus Python mais port 3000 ne répond pas: Deadlock dans le chargement des modèles"
echo "- Si CPU à 100%+: Chargement en cours (attendre encore)"
echo "- Si CPU à 0%: Processus bloqué/gelé (problème émulation ARM64)"
echo ""
echo "SOLUTION RECOMMANDÉE:"
echo "--------------------"
echo "1. Activer Rosetta 2 dans Docker Desktop"
echo "2. Settings → Features → 'Use Rosetta for x86/amd64 emulation'"
echo "3. Redémarrer: docker-compose down && docker-compose up -d"
