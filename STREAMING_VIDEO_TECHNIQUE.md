# 📹 Streaming Vidéo - Informations Techniques

### Configuration Actuelle
Le système utilise **MJPEG (Motion JPEG)** pour le streaming vidéo en direct vers le dashboard.

**Caractéristiques:**
- **Protocole:** HTTP/MJPEG (multipart/x-mixed-replace)
- **Port:** 5001
- **Résolution stream:** 1280x720 (720p)
- **Qualité JPEG:** 60%
- **FPS:** 25 images/seconde
- **Bande passante:** ~4-6 Mbps

### Indépendance du Stream
**IMPORTANT:** Le stream vidéo est **complètement indépendant** de l'intervalle de rafraîchissement du dashboard (30 secondes).

-  **Stream vidéo:** Temps réel continu (25 FPS)
-  **Rafraîchissement dashboard:** Toutes les 30 secondes (statistiques, tableaux)
-  **Reconnaissance faciale:** Full HD (résolution complète)

Le changement de 10s → 30s affecte **uniquement** les requêtes API pour les statistiques, **PAS** le stream vidéo!


### Pipeline Séparé

```
Caméra Hikvision (8MP)
      ↓
[RTSP Stream]
      ↓
Service Caméra (Python/OpenCV)
      ├─→ [Pipeline 1] Reconnaissance Faciale (Full HD 1920x1080)
      │                    ↓
      │              CompreFace API
      │                    ↓
      │              Base de données
      │
      └─→ [Pipeline 2] Streaming Web (720p optimisé)
                           ↓
                    Serveur MJPEG (port 5001)
                           ↓
                    Dashboard (navigateur)
```

### Avantages de l'Architecture Actuelle

1. **Séparation des préoccupations:**
   - La reconnaissance faciale ne ralentit pas le stream
   - Le stream ne ralentit pas la reconnaissance

2. **Optimisation indépendante:**
   - Reconnaissance: Full HD pour précision maximale
   - Stream: 720p pour fluidité maximale

3. **Simplicité:**
   - Pas de dépendances complexes
   - Compatible tous navigateurs
   - Facile à déboguer

## Options d'Amélioration Future

### Option 1: WebSocket Streaming (Recommandé)
**Avantages:**
- Latence réduite (~20-50ms vs 50-100ms MJPEG)
- Bande passante optimisée
- Bidirectionnel (contrôles PTZ possibles)

**Inconvénients:**
- Implémentation plus complexe
- Nécessite JavaScript côté client pour décoder
- Support navigateur à vérifier


### Option 2: WebRTC (Avancé)
**Avantages:**
- Latence ultra-faible (<50ms)
- P2P possible
- Standard moderne

**Inconvénients:**
- Très complexe à implémenter
- Nécessite serveur STUN/TURN
- Overhead important pour un seul stream


##  Monitoring du Stream

### Vérifier les Performances

**1. Vérifier les logs:**
```bash
docker-compose logs -f camera-service | grep "Stream"
```

Vous devriez voir:
```
Stream configured: 1280x720 @ 25fps, quality=60%
New client connected to MJPEG stream (1280x720 @ 25fps)
```

**2. Vérifier le health check:**
```bash
curl http://localhost:5001/stream/health
```

Réponse:
```json
{
  "status": "ok",
  "streaming": true,
  "frame_count": 12345
}
```

**3. Monitorer la bande passante:**
```bash
# Trafic réseau sur le port 5001
sudo iftop -i eth0 -f "port 5001"
```
