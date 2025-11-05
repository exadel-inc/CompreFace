#  Galerie Photos - Guide d'Implémentation Complète

## ✅ Fonctionnalités Implémentées

### 1. Nouvel Onglet "Galerie Photos"
- ✅ Onglet séparé pour toutes les captures
- ✅ Filtres: Nom, Département, Sous-Département, Statut
- ✅ Pagination complète

### 2. Sauvegarde des Images
- ✅ **TOUTES** les images sauvegardées (autorisées + non autorisées)
- ✅ Boîtes VERTES pour personnel autorisé
- ✅ Boîtes ROUGES pour accès non autorisé
- ✅ Nom de la personne dans le filename: `authorized_John_Doe_20250127_143022.jpg`

### 3. Base de Données
- ✅ Nouveaux champs: `department`, `sub_department`
- ✅ Migration automatique (ajoute colonnes si manquantes)
- ✅ Index pour requêtes rapides

### 4. Pagination Table Alertes
- ✅ Pagination ajoutée pour table "Alertes de Sécurité"

---

##  Déploiement

```bash
# Sur votre VM Linux

# 1. Récupérer les changements
git pull origin claude/customize-compreface-org-011CULsWgj5qre3ZdcAZopAs

# 2. Reconstruire les services
docker-compose build --no-cache camera-service dashboard-service

# 3. Redémarrer (migration DB automatique)
docker-compose up -d

# 4. Vérifier les logs
docker-compose logs -f camera-service | grep -i "department\|authorized"
```

---

## 📝 Configuration CompreFace (Ajout Personnel)

### Option 1: Via UI CompreFace (Port 8000)

Quand vous ajoutez un employé dans CompreFace:

1. Allez sur http://[VM-IP]:8000
2. Services → Your Recognition Service → Manage Collection
3. Add Subject
4. **NOM format**: `Nom_Prenom`
5. **Pas encore de support département dans UI** (voir Option 2)

### Option 2: Via API CompreFace (Recommandé)

Pour ajouter un employé **avec département**:

```bash
# Variables
API_KEY="votre_cle_api"
VM_IP="192.168.x.x"

# Ajouter un employé avec métadonnées complètes
curl -X POST "http://$VM_IP:8080/api/v1/recognition/subjects" \
  -H "x-api-key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Mohamed_Alami",
    "metadata": {
      "department": "Operations",
      "sub_department": "Parachutistes_1ere_Compagnie",
      "rank": "Sergent",
      "id_number": "1BIP-001"
    }
  }'

# Puis ajouter les photos
curl -X POST "http://$VM_IP:8080/api/v1/recognition/faces?subject=Mohamed_Alami" \
  -H "x-api-key: $API_KEY" \
  -F "file=@photo1.jpg"
```

---

## 🎯 Utilisation Dashboard

### Onglet "Galerie Photos"

1. **Accès**: http://[VM-IP]:5000 → Onglet "📸 Galerie Photos"

2. **Filtres disponibles**:
   - 🔍 **Nom**: Recherche par nom de personne
   - 🏢 **Département**: Liste déroulante (auto-populée)
   - 📁 **Sous-Département**: Liste déroulante (auto-populée)
   - ✅ **Statut**: Tous / Autorisés / Non Autorisés

3. **Navigation**:
   - Pagination automatique (20 images/page)
   - Cliquer sur une image pour plein écran
   - Bouton téléchargement disponible

### Onglet "Alertes de Sécurité"

- **Pagination**: Table avec navigation Précédent/Suivant
- **Filtre horaire**: 1h, 6h, 24h, 1 semaine

---

## 📊 Structure des Fichiers Images

### Format des noms de fichier:

```
✅ Autorisé (boîte verte):
authorized_Mohamed_Alami_20250127_143022.jpg
authorized_Karim_Benjelloun_20250127_143045.jpg

❌ Non autorisé (boîte rouge):
unauthorized_Unknown_20250127_144010.jpg
unauthorized_Ahmed_Hassan_20250127_144032.jpg  (similiarité faible)
```

### Localisation:
```
camera-service/logs/debug_images/
├── authorized_*.jpg  (boîtes vertes)
└── unauthorized_*.jpg  (boîtes rouges)
```

---

## 🗄️ Schéma Base de Données

```sql
-- Table access_logs (mise à jour)
CREATE TABLE access_logs (
    id SERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    camera_name VARCHAR(255) NOT NULL,
    camera_location VARCHAR(255),
    subject_name VARCHAR(255),
    department VARCHAR(255),          -- NOUVEAU
    sub_department VARCHAR(255),      -- NOUVEAU
    is_authorized BOOLEAN NOT NULL,
    similarity FLOAT,
    face_box JSON,
    alert_sent BOOLEAN DEFAULT FALSE,
    image_path VARCHAR(500),          -- Contient le filename avec nom personne
    metadata JSON
);

-- Index pour performance
CREATE INDEX idx_access_logs_department ON access_logs(department);
CREATE INDEX idx_access_logs_subject ON access_logs(subject_name);
```

---

## 🔍 Exemples de Filtrage

### Filtrer par nom:
```
Recherche: "Mohamed"
Résultat: Toutes les images contenant "Mohamed" dans le nom
```

### Filtrer par département:
```
Département: "Operations"
Résultat: Toutes les images du département Opérations
```

### Filtrer combiné:
```
Nom: "Alami"
Département: "Operations"
Statut: "Autorisés"
Résultat: Images de personnes autorisées nommées Alami dans Opérations
```

---

## ⚙️ API Endpoints (Backend)

### GET /api/images/gallery
```bash
# Récupérer images avec filtres
curl "http://localhost:5000/api/images/gallery?page=1&per_page=20&name=Mohamed&department=Operations&status=authorized"

# Réponse JSON:
{
  "images": [
    {
      "filename": "authorized_Mohamed_Alami_20250127_143022.jpg",
      "timestamp": 1706361622.5,
      "url": "/api/images/authorized_Mohamed_Alami_20250127_143022.jpg",
      "subject_name": "Mohamed_Alami",
      "department": "Operations",
      "sub_department": "Parachutistes_1ere_Compagnie",
      "is_authorized": true,
      "similarity": 0.95
    }
  ],
  "total": 150,
  "page": 1,
  "per_page": 20,
  "total_pages": 8
}
```

### GET /api/departments
```bash
# Liste des départements disponibles
curl "http://localhost:5000/api/departments"

# Réponse:
{
  "departments": ["Operations", "Logistique", "Commandement"],
  "sub_departments": {
    "Operations": ["Parachutistes_1ere_Compagnie", "Parachutistes_2eme_Compagnie"],
    "Logistique": ["Materiel", "Transport"]
  }
}
```

---

## 🎨 Interface Utilisateur

### Galerie Photos:

```
┌──────────────────────────────────────────────────────────────┐
│ 📸 Galerie Photos - Captures de Reconnaissance Faciale       │
├──────────────────────────────────────────────────────────────┤
│ 🔍 Nom: [_________]  🏢 Dept: [▼]  📁 Sous: [▼]  ✅ [▼]  🔄    │
│                                                              │
│ 150 image(s) trouvée(s)                                      │
│                                                              │
│ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐                      │
│ │✅📷 │ │✅📷 │ │❌📷 │ │✅📷 │ │❌📷 │                         │
│ │Mohamed│ │Karim│ │Unknown│ │Ahmed│ │Hassan│                 │
│ │14:30 │ │14:31│ │14:32│ │14:33│ │14:34│                     │
│ └─────┘ └─────┘ └─────┘ └─────┘ └─────┘                      │
│                                                              │
│         ◀ Précédent    Page 1 sur 8    Suivant ▶             │
└──────────────────────────────────────────────────────────────┘
```

### Alertes de Sécurité (avec pagination):

```
┌──────────────────────────────────────────────────────────────┐
│ 🚨 Tentatives d'Accès Non Autorisées                         │
├──────────────────────────────────────────────────────────────┤
│ Plage: [24 Dernières Heures ▼]  🔄 Actualiser                │
│                                                              │
│ ⚠️ 45 accès non autorisés dans les dernières 24 heures       │
│                                                              │
│ │ Horodatage        │ Caméra    │ Personne │ Alerte │        │
│ │ 27/01 14:32:10   │ Gate Alpha│ Unknown  │  ✓      │        │
│ │ 27/01 14:34:22   │ Gate Alpha│ Hassan M.│  ✓      │        │
│                                                              │
│         ◀ Précédent    Page 1 sur 3    Suivant ▶             │
└──────────────────────────────────────────────────────────────┘
```

---

## ✅ Checklist de Vérification

Après déploiement, vérifiez:

- [ ] Onglet "Galerie Photos" visible
- [ ] Filtres fonctionnels (nom, département, statut)
- [ ] Images avec boîtes vertes pour autorisés
- [ ] Images avec boîtes rouges pour non autorisés
- [ ] Nom de personne dans filename
- [ ] Pagination table alertes fonctionne
- [ ] Champs department/sub_department en DB

---

Updates made by Mellal Badr
https://badr-mellal.com
