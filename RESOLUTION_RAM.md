

### Cause: **Mémoire Insuffisante**

Le service `compreface-core` essaie de charger les modèles d'IA en mémoire mais **manque de RAM** et se bloque/crashe silencieusement.

## 💾 Vérifier la RAM Docker

```bash
# Voir la RAM allouée à Docker
docker info | grep "Total Memory"
```

**Résultat typique:**
```
Total Memory: 3.842GiB    # ❌ INSUFFISANT - Minimum 4GB requis
Total Memory: 7.684GiB    # ✅ OK - Suffisant pour config minimale
Total Memory: 15.37GiB    # ✅ PARFAIT - Peut utiliser config standard
```

## ✅ SOLUTION: Réduire l'Utilisation RAM

### Étape 1: Récupérer la Nouvelle Configuration

```bash
cd ~/Desktop/Projects/CompreFaceModeling

# Récupérer les changements (config RAM réduite)
git pull origin claude/customize-compreface-org-011CULsWgj5qre3ZdcAZopAs
```

**Vérifier que les changements sont appliqués:**
```bash
cat .env | grep -A2 "uwsgi_processes"
```

**Devrait afficher:**
```
uwsgi_processes=1        # 1 worker (économie RAM, suffisant pour tests)
uwsgi_threads=2          # 2 threads par worker
```

### Étape 2: Augmenter RAM Docker Desktop (Recommandé)

**Pour M3 Max, donnez plus de ressources à Docker:**

1. **Ouvrir Docker Desktop**
2. **⚙️ Settings** → **Resources**
3. **Configurer:**
   ```
   Memory: 8 GB (minimum) ou 12 GB (recommandé)
   CPUs: 8 cores (minimum)
   Swap: 2 GB
   Disk image size: 64 GB
   ```
4. **Apply & Restart**

### Étape 3: Redémarrer CompreFace

```bash
# Arrêter complètement
docker-compose down

# Vider le cache
docker system prune -f

# Redémarrer
docker-compose up -d

# Suivre les logs en temps réel
docker-compose logs -f compreface-core
```

**Vous devriez maintenant voir:**
```
*** Operational MODE: preforking+threaded ***
Loading embedding model...
Loading face detection model...
✅ Models loaded successfully
Spawned uWSGI worker 1 (pid: 25)
WSGI app 0 ready in 45 seconds
```

Appuyez sur `Ctrl+C` pour arrêter de suivre.

### Étape 4: Vérifier l'État

**Après 5 minutes:**
```bash
docker-compose ps
```

**Résultat attendu:**
```
NAME                     STATUS
compreface-core          Up 5 minutes (healthy)      ✅
compreface-postgres-db   Up 5 minutes                ✅
compreface-admin         Up 5 minutes                ✅
compreface-api           Up 5 minutes                ✅
compreface-ui            Up 5 minutes                ✅
1bip-dashboard           Up 5 minutes (healthy)      ✅
1bip-camera-service      Up 5 minutes (healthy)      ✅
```

## 📊 Configurations RAM Disponibles

### Configuration MINIMALE (Actuelle)
**RAM Docker:** 4-8 GB
**Fichier:** `.env` (configuration par défaut maintenant)

```bash
compreface_api_java_options=-Xmx2g
compreface_admin_java_options=-Xmx512m
uwsgi_processes=1
uwsgi_threads=2
```

**Performance:** Suffisant pour 50-100 utilisateurs, tests, développement

### Configuration STANDARD
**RAM Docker:** 8-16 GB
**Fichier:** Modifier `.env`

```bash
# Décommenter dans .env:
compreface_api_java_options=-Xmx4g
compreface_admin_java_options=-Xmx1g
uwsgi_processes=2
uwsgi_threads=2
```

**Performance:** Bon pour 100-300 utilisateurs, production légère

### Configuration HIGH PERFORMANCE
**RAM Docker:** 16+ GB
**Fichier:** Modifier `.env`

```bash
# Décommenter dans .env:
compreface_api_java_options=-Xmx8g
compreface_admin_java_options=-Xmx2g
uwsgi_processes=4
uwsgi_threads=2
```

**Performance:** Excellente pour 300-500 utilisateurs, production intensive

## 🧪 Scripts de Test

### Test Rapide
```bash
./TEST_RAPIDE.sh
```

Vérifie rapidement:
- ✅ Ports 8000 et 5000 accessibles
- ✅ Healthchecks fonctionnent
- ✅ Processus Python actifs
- ✅ Mémoire disponible

### Diagnostic Complet
```bash
./DIAGNOSTIC_COMPLET.sh > diagnostic.txt
cat diagnostic.txt
```

Génère un rapport détaillé avec:
- Logs complets
- Tests de connexion
- Utilisation ressources
- État base de données

## 🔧 Dépannage Avancé

### Problème 1: Toujours "unhealthy" après 10 minutes

```bash
# Voir les logs complets
docker-compose logs compreface-core | tail -200

# Vérifier la mémoire utilisée
docker stats --no-stream | grep compreface-core
```

**Si vous voyez:**
```
compreface-core   0.00%   2.5GB / 4GB   62.5%
```

→ Le service tente d'utiliser 2.5GB mais Docker n'a que 4GB total (insuffisant)

**Solution:** Augmenter RAM Docker à 8GB

### Problème 2: Service redémarre en boucle

```bash
# Voir les erreurs
docker-compose logs compreface-core | grep -i "error\|exception\|killed"
```

**Si vous voyez "Killed" ou "OOMKilled":**
→ Manque de mémoire, le système tue le processus

**Solution:** Augmenter RAM Docker OU réduire davantage la config

### Problème 3: Chargement très lent (>10 minutes)

C'est **normal sur M3 Max** avec émulation AMD64:
- Chargement modèles: 3-5 minutes
- Initialisation complète: 5-10 minutes
- Performance: 60-70% du natif

**Optimisation:**
1. Docker Desktop → Settings → Features in development
2. ✅ Activer "Use Rosetta for x86/amd64 emulation on Apple Silicon"
3. Apply & Restart

Améliore les performances d'émulation de ~30%.

## ✅ Checklist de Résolution

- [ ] Vérifier RAM Docker: `docker info | grep Memory`
- [ ] Augmenter RAM Docker à 8GB minimum
- [ ] Récupérer nouvelle config: `git pull`
- [ ] Vérifier `.env` a `uwsgi_processes=1`
- [ ] Arrêter services: `docker-compose down`
- [ ] Nettoyer: `docker system prune -f`
- [ ] Redémarrer: `docker-compose up -d`
- [ ] Attendre 5 minutes
- [ ] Vérifier: `docker-compose ps`
- [ ] Tester UI: http://localhost:8000
- [ ] Tester Dashboard: http://localhost:5000


