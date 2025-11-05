# Face Recognition System - Deployment Guide

Complete guide for deploying my custom Face Recognition & Attendance System with Hikvision camera integration.

---

## Table of Contents

1. [System Requirements](#system-requirements)
2. [Pre-Deployment Checklist](#pre-deployment-checklist)
3. [Step-by-Step Deployment](#step-by-step-deployment)
4. [Camera Setup](#camera-setup)
5. [Adding Employees](#adding-employees)
6. [Testing the System](#testing-the-system)
7. [Production Deployment](#production-deployment)
8. [Monitoring & Maintenance](#monitoring--maintenance)

---

## System Requirements

### Hardware Requirements

**Minimum (Testing/Development):**
- CPU: 4 cores
- RAM: 8 GB
- Storage: 50 GB SSD
- Network: 100 Mbps

**Recommended (Production):**
- CPU: 8+ cores (or GPU for better performance)
- RAM: 16+ GB
- Storage: 200+ GB SSD
- Network: 1 Gbps

### Software Requirements

- **Operating System**: Linux (Ubuntu 20.04/22.04 recommended) or Windows with Docker
- **Docker**: Version 20.10+
- **Docker Compose**: Version 1.29+
- **Hikvision Camera**: 8MP model with RTSP support
- **Network**: Cameras and server on same network or VPN

---

## Pre-Deployment Checklist

### ✅ Before Starting

- [ ] Docker and Docker Compose installed
- [ ] Hikvision cameras configured and accessible
- [ ] Camera RTSP credentials available
- [ ] Network connectivity verified (ping camera IP)
- [ ] Sufficient disk space (check with `df -h`)
- [ ] Ports 8000 (UI) and 554 (RTSP) available

### ✅ Security Checklist

- [ ] Changed default camera passwords
- [ ] Updated database password in `.env`
- [ ] Cameras on isolated VLAN (recommended)
- [ ] Firewall rules configured
- [ ] SSL/TLS certificates prepared (for production)

---

## Step-by-Step Deployment

### Step 1: Clone/Download Repository

```bash
# If using git
git clone https://github.com/badrmellal/CompreFaceModeling.git
cd CompreFaceModeling

# Or download and extract the archive
```

### Step 2: Configure Environment

Edit `.env` file:

```bash
nano .env
```

**Key settings to update:**

```bash
# Change database password (IMPORTANT!)
postgres_password=YOUR_SECURE_PASSWORD_HERE

# Database name
postgres_db=frs_1bip

# Email configuration (optional but recommended)
enable_email_server=true
email_host=smtp.gmail.com
email_username=your-email@1bip.com
email_password=your-app-password
```

### Step 3: Configure Camera Service

Edit camera configuration:

```bash
nano camera-service/config/camera_config.env
```

**Essential settings:**

```bash
# Camera RTSP URL - UPDATE THIS!
CAMERA_RTSP_URL=rtsp://admin:YOUR_CAMERA_PASSWORD@192.168.1.100:554/Streaming/Channels/101

# Camera identification
CAMERA_NAME=Main Entrance Gate
CAMERA_LOCATION=Building A - Main Gate

# You'll get this API key after first setup (see Step 6)
COMPREFACE_API_KEY=

# Database password (must match .env file)
DB_PASSWORD=YOUR_SECURE_PASSWORD_HERE

# Alert settings
ENABLE_ALERTS=true
ALERT_EMAIL=security@1bip.com
```

### Step 4: Start CompreFace Services

```bash
# Pull images and start services
docker-compose up -d compreface-postgres-db compreface-admin compreface-api compreface-core compreface-fe

# Wait for services to start (30-60 seconds)
echo "Waiting for services to start..."
sleep 60

# Check status
docker-compose ps
```

**Expected output:**
```
NAME                      STATUS
compreface-admin          Up
compreface-api            Up
compreface-core           Up (healthy)
compreface-postgres-db    Up
compreface-ui             Up
```

### Step 5: Initial CompreFace Setup

1. **Open CompreFace UI:**
   ```
   http://localhost:8000
   ```

2. **Create First User (Owner):**
   - Click "Sign Up"
   - First Name: Your name
   - Last Name: Your surname
   - Email: admin@1bip.com
   - Password: Strong password
   - Click "Sign Up"

3. **Login:**
   - Use credentials you just created
   - You'll see an empty Applications page

### Step 6: Create Recognition Application

1. **Create Application:**
   - Click "Create Application"
   - Name: `1BIP Main System`
   - Click "Create"

2. **Create Recognition Service:**
   - Click on your application
   - Click "Add Service"
   - Service Name: `Main Entrance Recognition`
   - Service Type: **Recognition**
   - Click "Create"

3. **Copy API Key:**
   - Find your Recognition Service in the list
   - Click "Copy API Key" (clipboard icon)
   - **Save this key** - you'll need it for camera configuration

4. **Update Camera Config:**
   ```bash
   nano camera-service/config/camera_config.env
   ```

   Paste the API key:
   ```bash
   COMPREFACE_API_KEY=your-copied-api-key-here
   ```

### Step 7: Add Employees to System

1. **Access Face Collection:**
   - In CompreFace UI, click your Recognition Service
   - Click "Manage Collection"

2. **Add First Employee:**
   - Click "Add Subject"
   - Subject Name: Employee name or ID (e.g., "John_Doe" or "EMP001")
   - Click "Add"

3. **Upload Employee Photos:**
   - Click on the employee name
   - Click "Upload" or drag-drop photos
   - **Best practices:**
     - Upload 3-5 photos per person
     - Different angles (front, slight left, slight right)
     - Different lighting conditions
     - Clear face visibility
     - No sunglasses or masks

4. **Repeat for all employees:**
   - Add all authorized personnel
   - Organize by department if needed (use subject names like "HR_John_Doe")

---

## Camera Setup

### Hikvision Camera Configuration

1. **Access Camera Web Interface:**
   ```
   http://[camera_ip]
   ```

2. **Enable RTSP:**
   - Configuration → Network → Advanced Settings → RTSP
   - Enable RTSP
   - RTSP Port: 554 (default)
   - Authentication: Basic

3. **Configure Video Stream:**
   - Configuration → Video/Audio → Video
   - Main Stream Settings:
     - Resolution: 1920×1080 (recommended) or higher
     - Frame Rate: 25 fps
     - Bitrate: Variable
     - Video Quality: Higher

4. **Test RTSP Stream:**

   Using VLC Media Player:
   - Open VLC
   - Media → Open Network Stream
   - Enter: `rtsp://admin:password@[camera_ip]:554/Streaming/Channels/101`
   - Click Play
   - If you see video, RTSP is working!

5. **Position Camera:**
   - Mount at entrance gate
   - Height: 1.8-2.2 meters
   - Angle: Slightly downward (15-30 degrees)
   - Ensure good lighting
   - Test with actual face detection

---

## Starting Camera Service

### Step 8: Start Camera Service

```bash
# Build and start camera service
docker-compose build camera-service
docker-compose up -d camera-service

# Check logs
docker-compose logs -f camera-service
```

**Expected logs:**
```
camera-service | Starting 1BIP Camera Service
camera-service | Camera: Main Entrance Gate
camera-service | Location: Building A - Main Gate
camera-service | Connecting to camera...
camera-service | ✓ Camera connected successfully
camera-service | Database connection established
camera-service | Processing frame #5
camera-service | Detected 1 face(s) in frame
camera-service | ✓ Authorized: John_Doe (92.3%)
```

### Step 9: Verify Camera Service

Check service status:

```bash
# Check if container is running
docker ps | grep camera

# View recent logs
docker-compose logs --tail=50 camera-service

# Check database logs
docker-compose exec compreface-postgres-db psql -U postgres -d frs_1bip -c "SELECT * FROM access_logs ORDER BY timestamp DESC LIMIT 5;"
```

---

## Testing the System

### Test 1: Authorized Access

1. Stand in front of the camera
2. Wait 2-3 seconds
3. Check logs:
   ```bash
   docker-compose logs --tail=20 camera-service
   ```
4. You should see: `✓ Authorized: [Your_Name] (XX.X%)`

### Test 2: Unauthorized Access

1. Have someone not in the system stand in front of camera
2. Check logs:
   ```bash
   docker-compose logs --tail=20 camera-service
   ```
3. You should see: `✗ Unknown person detected`
4. An alert should be logged

### Test 3: Multi-Face Detection

1. Multiple people stand in front of camera
2. System should detect all faces
3. Check logs for multiple face entries

### Test 4: Database Logging

```bash
# Access database
docker-compose exec compreface-postgres-db psql -U postgres -d frs_1bip

# Query access logs
SELECT subject_name, is_authorized, similarity, timestamp
FROM access_logs
ORDER BY timestamp DESC
LIMIT 10;

# Exit
\q
```

---

## Production Deployment

### Security Hardening

1. **Change All Passwords:**
   ```bash
   # Edit .env
   postgres_password=VERY_STRONG_PASSWORD_HERE_987654

   # Edit camera_config.env
   DB_PASSWORD=VERY_STRONG_PASSWORD_HERE_987654
   ```

2. **Enable HTTPS:**
   - Set up reverse proxy (Nginx/Traefik)
   - Install SSL certificates (Let's Encrypt)
   - Update ports to 443

3. **Firewall Rules:**
   ```bash
   # Ubuntu/Debian
   sudo ufw allow 8000/tcp  # CompreFace UI
   sudo ufw allow 443/tcp   # HTTPS (production)
   sudo ufw enable
   ```

4. **Network Segmentation:**
   - Put cameras on separate VLAN
   - Restrict camera network access
   - Use VPN for remote access

### Performance Optimization

For high-traffic entrances:

```bash
# Edit camera_config.env
FRAME_SKIP=3              # Process more frames
MAX_FACES_PER_FRAME=15    # Detect more faces
UWSGI_PROCESSES=4         # More workers

# Edit .env
compreface_api_java_options=-Xmx8g  # More memory
uwsgi_processes=4                    # More workers
```

### Backup Strategy

```bash
# Create backup script
cat > backup.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups"

# Backup database
docker-compose exec -T compreface-postgres-db pg_dump -U postgres frs_1bip > $BACKUP_DIR/db_backup_$DATE.sql

# Backup configuration
tar -czf $BACKUP_DIR/config_backup_$DATE.tar.gz .env camera-service/config/

echo "Backup completed: $DATE"
EOF

chmod +x backup.sh

# Add to crontab (daily at 2 AM)
(crontab -l 2>/dev/null; echo "0 2 * * * /path/to/backup.sh") | crontab -
```

---

## Monitoring & Maintenance

### Daily Monitoring

```bash
# Check service health
docker-compose ps

# Check recent logs
docker-compose logs --tail=100 --follow camera-service

# Check unauthorized access attempts today
docker-compose exec compreface-postgres-db psql -U postgres -d frs_1bip -c "
SELECT COUNT(*) as unauthorized_attempts
FROM access_logs
WHERE is_authorized = FALSE
AND timestamp >= CURRENT_DATE;"
```

### Weekly Maintenance

```bash
# Clean old logs (keep last 7 days)
find camera-service/logs/ -name "*.log" -mtime +7 -delete

# Check disk space
df -h

# Update Docker images (if new versions available)
docker-compose pull
docker-compose up -d
```

### Troubleshooting Commands

```bash
# Restart specific service
docker-compose restart camera-service

# Rebuild after code changes
docker-compose build camera-service
docker-compose up -d camera-service

# View all logs
docker-compose logs

# Access database shell
docker-compose exec compreface-postgres-db psql -U postgres -d frs_1bip

# Check network connectivity
docker-compose exec camera-service ping compreface-api
```

---

## Multiple Camera Deployment

To add more cameras:

1. **Copy config file:**
   ```bash
   cp camera-service/config/camera_config.env camera-service/config/camera_back_gate.env
   ```

2. **Edit new config:**
   ```bash
   nano camera-service/config/camera_back_gate.env
   ```

   Update:
   ```bash
   CAMERA_RTSP_URL=rtsp://admin:pass@192.168.1.101:554/Streaming/Channels/101
   CAMERA_NAME=Back Entrance Gate
   CAMERA_LOCATION=Building B
   ```

3. **Add to docker-compose.yml:**
   ```yaml
   camera-service-back-gate:
     build:
       context: ./camera-service
       dockerfile: Dockerfile
     container_name: "1bip-camera-back-gate"
     restart: unless-stopped
     depends_on:
       - compreface-api
       - compreface-postgres-db
     env_file:
       - ./camera-service/config/camera_back_gate.env
     volumes:
       - ./camera-service/logs:/app/logs
   ```

4. **Start new camera service:**
   ```bash
   docker-compose up -d camera-service-back-gate
   ```

---

**Deployment Guide Version:** 1.0.0
**Last Updated:** 2025-10-21
