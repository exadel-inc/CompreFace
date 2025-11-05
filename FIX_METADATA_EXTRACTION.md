#  Fix: Extraction des Métadonnées CompreFace

## Problème Identifié

**Symptôme:** Quand on ajoute une nouvelle personne via le dashboard (port 5000), la personne est bien ajoutée à CompreFace, mais quand la caméra la reconnaît, les informations de département et sous-département n'apparaissent pas dans le frontend.



### Flux Avant le Fix:

```
1. Utilisateur ajoute personnel via dashboard (port 5000)
   ↓
2. Dashboard envoie à CompreFace:
   - Nom: " Ahmed Bennani"
   - Photos: 3 images
   - Métadonnées: {"department": "13BsIsPssfr", "sub_department": "Comp 1", "rank": "Cap"}
   ↓
3. CompreFace stocke TOUT (photos + métadonnées) ✅
   ↓
4. Caméra détecte le visage
   ↓
5. CompreFace répond avec:
   {
     "subjects": [{
       "subject": " Ahmed Bennani",
       "similarity": 0.95,
       "metadata": {"department": "13BsIsPkcloo", "sub_department": "Comp 1", ...}
     }]
   }
   ↓
6. ❌ BUG: Service caméra IGNORAIT le champ "metadata"
   ↓
7. Enregistrement dans base de données:
   - Nom: ✅
   - Département: ❌ NULL
   - Sous-département: ❌ NULL
   ↓
8. Frontend: Ne peut pas afficher département car NULL dans la BD
```




## Comment Appliquer le Fix

### 1. Reconstruire le Service Caméra (UNE SEULE FOIS)

```bash
# Arrêter le service caméra
docker-compose stop camera-service

# Reconstruire avec le fix
docker-compose up -d --build camera-service

# Vérifier que ça fonctionne
docker-compose logs -f camera-service
```

### 2. Vérification dans les Logs

Après reconstruction, quand une personne est reconnue, vous devriez voir:

**Avant:**
```
✓ Authorized: Ahmed Bennani (95.23%)
```

**Après:**
```
✓ Authorized: Ahmed Bennani (95.23%) - 13BIP
```

Le département apparaît maintenant dans les logs!


## Questions Fréquentes

### Q1: Faut-il redémarrer docker-compose à chaque ajout de personne?

**Réponse: NON!** 

Après avoir appliqué ce fix (reconstruction **une seule fois**), vous pouvez ajouter autant de personnes que vous voulez sans redémarrer.

### Q2: Que se passe-t-il avec les personnes ajoutées AVANT le fix?

**Réponse:** Les personnes ajoutées avant le fix ont leurs métadonnées **stockées dans CompreFace**, mais **pas dans la base de données PostgreSQL** (table access_logs).

**Solution:**
1. La prochaine fois que la caméra les détecte, les métadonnées seront extraites et enregistrées correctement
2. Ou vous pouvez les supprimer et les ré-ajouter via le dashboard



