
## 📋 Vue d'Ensemble

Ce document décrit la structure organisationnelle telle qu'implémentée dans le système de reconnaissance faciale.



## 📁 Compagnies et Sections (Sous-Départements)


### Exemples de Sous-Départements:



**Pour VISITORS:**
- (Laisser vide ou indiquer l'organisation d'origine)

## 🔧 Implémentation Technique

### Backend (API)
**Fichier:** `dashboard-service/src/app.py`


### Frontend (HTML)
**Fichier:** `dashboard-service/src/templates/dashboard.html`

**Formulaire d'ajout de personnel:**

**Filtres de la galerie:**


### JavaScript
**Fichier:** `dashboard-service/src/static/js/dashboard.js`

- **Pas de cascade automatique** pour les sous-départements
- Départements hardcodés dans le HTML
- Sous-départements saisis manuellement

## 📊 Utilisation

### Ajouter un Nouveau Personnel



### Filtrer dans la Galerie Photos

1. **Accéder à l'onglet:** "📸 Galerie Photos"
2. **Utiliser les filtres:**
   - Nom: Recherche par nom
   - **/Unité:** Filtrer par 
   - **/Section:** Filtrer par 
   - Statut: Autorisé / Non Autorisé




## 🗄️ Stockage des Données

Les informations sont stockées dans:

1. **CompreFace** (Reconnaissance faciale)
   - Subject name = Nom du personnel
   - Metadata = JSON avec département, sous-département, grade

2. **PostgreSQL** (Logs d'accès)
   - Table: `access_logs`
   - Colonnes: `department`, `sub_department`, `subject_name`, etc.


## 🔄 Modification de la Structure

Si vous devez ajouter/modifier :

1. **Backend:** Éditer `dashboard-service/src/app.py` → fonction `get_department_config()`
2. **Frontend:** Éditer `dashboard-service/src/templates/dashboard.html` → section formulaire
3. **Rebuild:** `docker-compose up -d --build dashboard-service`

