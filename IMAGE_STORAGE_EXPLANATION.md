# Image Storage Explanation - 1BIP System

## 📊 Where Are Images Stored?

### 1. **Face Training Images** (Personnel Photos)
**Location**: PostgreSQL Database (`img` table)
**Setting**: `save_images_to_db=true` in `.env`

```
When you add personnel via port 5000:
├── Photos uploaded → CompreFace API
├── CompreFace saves to PostgreSQL `img` table
└── Images stored as BLOB data in database

Why RAM usage but not disk files?
├── Docker volume `postgres-data` stores DB
├── PostgreSQL manages its own files
└── You see RAM usage (DB cache), not individual .jpg files
```

**Verify**:
```sql
SELECT s.subject_name, COUNT(i.id) as image_count,
       SUM(i.img_size) as total_size_bytes
FROM subject s
JOIN img i ON s.id = i.subject_id
GROUP BY s.subject_name;
```

---

### 2. **Unauthorized Access Screenshots**
**Location**: Local Disk (`./camera-service/logs/debug_images/`)
**Format**: `unauthorized_Unknown_YYYYMMDD_HHMMSS.jpg`

```
Docker volume mapping:
├── Container: /app/logs/debug_images/
└── Host: ./camera-service/logs/debug_images/

Retention: 5 days (auto-cleanup every 6 hours)
```

**Verify**:
```bash
ls -lh camera-service/logs/debug_images/
du -sh camera-service/logs/debug_images/
```

---

### 3. **Docker Volumes**
```bash
# Check Docker volumes
docker volume ls
docker volume inspect comprefacemodeling_postgres-data

# PostgreSQL data location
/var/lib/docker/volumes/comprefacemodeling_postgres-data/_data
```

---

## Summary

| Image Type | Storage Location | Visible as Files | Disk Space |
|------------|------------------|------------------|------------|
| Training photos | PostgreSQL DB | ❌ No (BLOB) | Docker volume |
| Unauthorized screenshots | ./camera-service/logs/ | ✅ Yes (.jpg) | Host disk |
| Database files | Docker volume | ✅ Yes (internal) | Docker volume |
