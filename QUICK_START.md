# MELLAL BADR Face Recognition System - Quick Start Guide

Complete offline face recognition and attendance system for our organization.

---

## 🚀 What You Have Now

A complete, production-ready face recognition system that:

✅ **Works 100% Offline** - No internet required
✅ **Multi-Face Detection** - Detects multiple people simultaneously
✅ **Real-Time Monitoring** - Web dashboard with auto-refresh
✅ **Unauthorized Alerts** - Instant security notifications
✅ **Attendance Tracking** - Automatic employee time tracking
✅ **Hikvision Integration** - Ready for 8MP cameras
✅ **Department Support** - Scalable for 300-500 users per department
✅ **Complete Privacy** - All data stays on your server

---

## 📦 System Components

```
 Face Recognition System
│
├── CompreFace (Face Recognition Engine)
│   ├── Admin Service (User management)
│   ├── API Service (Recognition API)
│   ├── Core Service (ML processing)
│   └── UI Service (CompreFace web interface)
│
├── Camera Service (Hikvision Integration)
│   ├── Multi-face detection
│   ├── Unauthorized access alerts
│   ├── Access logging
│   └── Debug image capture
│
├── Dashboard Service (Monitoring Interface)
│   ├── Real-time access monitoring
│   ├── Attendance tracking
│   ├── Unauthorized access alerts
│   ├── Camera health monitoring
│   └── Reports & analytics
│
└── PostgreSQL (Database)
    ├── User data
    ├── Face embeddings
    └── Access logs
```

---

## ⚡ Quick Start (3 Steps)

### Step 1: Configure Your Camera

Edit `camera-service/config/camera_config.env`:

```bash
# Your Hikvision camera RTSP URL
CAMERA_RTSP_URL=rtsp://admin:YOUR_PASSWORD@192.168.1.100:554/Streaming/Channels/101

# Camera identification
CAMERA_NAME=Main Entrance Gate
CAMERA_LOCATION=Building A - Main Gate
```

### Step 2: Start All Services

```bash
# Start everything
docker-compose up -d

# Wait 60 seconds for services to initialize
sleep 60

# Check status
docker-compose ps
```

**Expected Output:**
```
NAME                      STATUS
compreface-admin          Up
compreface-api            Up
compreface-core           Up (healthy)
compreface-postgres-db    Up
compreface-ui             Up
1bip-camera-service       Up
1bip-dashboard            Up
```

### Step 3: Access Web Interfaces

**CompreFace UI** (Add employees & configure):
```
http://localhost:8000
```

** Dashboard** (Monitor & track):
```
http://localhost:5000
```

---

## 📝 Initial Setup (First Time)

### 1. Create CompreFace Account

1. Open http://localhost:8000
2. Click "Sign Up"
3. Enter your details:
   - First Name: Your name
   - Last Name: Your surname
   - Email: admin@1bip.com
   - Password: (strong password)
4. Click "Sign Up"

### 2. Create Recognition Application

1. Log in to CompreFace
2. Click "Create Application"
3. Name: `Main System`
4. Click "Create"

### 3. Create Recognition Service

1. Click on your application
2. Click "Add Service"
3. Service Name: `Main Entrance Recognition`
4. Service Type: **Recognition**
5. Click "Create"

### 4. Get API Key

1. Find your Recognition Service
2. Click the copy icon to copy API Key
3. Open `camera-service/config/camera_config.env`
4. Paste: `COMPREFACE_API_KEY=your-copied-key-here`
5. Restart camera service:
   ```bash
   docker-compose restart camera-service
   ```

### 5. Add Employees

1. In CompreFace, click your Recognition Service
2. Click "Manage Collection"
3. Click "Add Subject"
4. Subject Name: `Employee_Name` (e.g., "John_Doe")
5. Upload 3-5 photos of the employee
   - Different angles
   - Different lighting
   - Clear face visibility

### 6. Test the System

1. Stand in front of the camera
2. Open dashboard: http://localhost:5000
3. Click "Live Monitor" tab
4. You should see your access logged in real-time!

---

## 🖥️ Web Interfaces

### CompreFace UI (http://localhost:8000)

**Purpose:** Manage employees and face recognition

**Features:**
- Add/remove employees
- Upload face photos
- Configure recognition services
- Manage user access
- Test face recognition

###  Dashboard (http://localhost:5000)

**Purpose:** Monitor and track attendance

**Features:**
- 🔴 **Live Monitor** - Real-time access log
- 📋 **Attendance** - Daily attendance report
- ⚠️ **Unauthorized** - Security alerts
- 📹 **Camera Status** - Camera health
- 📊 **Reports** - Analytics & exports

---

## 📊 Dashboard Features

### Summary Cards (Top of Dashboard)

- **Total Access Today** - All access attempts
- **Authorized** - Successful recognitions
- **Unauthorized Attempts** - Security incidents
- **Unique Employees** - Different people detected
- **Active Cameras** - Cameras currently online

### Live Monitor Tab

- Shows last 50 access attempts
- Auto-refreshes every 10 seconds
- Color-coded status badges
- Recognition confidence percentages

### Attendance Tab

- Today's attendance report
- First entry (arrival time)
- Last entry (departure time)
- Export to CSV

### Unauthorized Tab

- Security incidents log
- Filter by time range
- Alert status
- Camera location

### Camera Status Tab

- All cameras with health status
- Online/Warning/Offline indicators
- Last activity timestamp
- Detections count

### Reports Tab

- Custom date range reports
- Hourly activity charts
- Export to CSV
- Visual analytics

---

## 🎥 Camera Setup

### Hikvision Camera RTSP URL Format

```
rtsp://[username]:[password]@[camera_ip]:[port]/Streaming/Channels/[channel]
```

**Examples:**

```bash
# Main stream (high quality) - Recommended
rtsp://admin:Admin123@192.168.1.100:554/Streaming/Channels/101

# Sub stream (lower quality) - For bandwidth constraints
rtsp://admin:Admin123@192.168.1.100:554/Streaming/Channels/102
```

### Multiple Cameras

To add more cameras:

1. Copy config file:
   ```bash
   cp camera-service/config/camera_config.env \
      camera-service/config/camera_back_gate.env
   ```

2. Edit new config with different camera URL

3. Add to `docker-compose.yml`:
   ```yaml
   camera-service-back-gate:
     build:
       context: ./camera-service
     env_file:
       - ./camera-service/config/camera_back_gate.env
   ```

4. Start new camera service:
   ```bash
   docker-compose up -d camera-service-back-gate
   ```

---

## 🔍 Monitoring & Logs

### Check Service Status

```bash
# All services
docker-compose ps

# Specific service
docker-compose ps camera-service
```

### View Logs

```bash
# Live logs (all services)
docker-compose logs -f

# Camera service logs
docker-compose logs -f camera-service

# Dashboard logs
docker-compose logs -f dashboard-service

# Last 50 lines
docker-compose logs --tail=50 camera-service
```

### Database Access

```bash
# Access PostgreSQL
docker-compose exec compreface-postgres-db psql -U postgres -d frs_1bip

# Query today's attendance
SELECT subject_name, MIN(timestamp) as arrival
FROM access_logs
WHERE is_authorized = TRUE
AND timestamp >= CURRENT_DATE
GROUP BY subject_name;

# Exit
\q
```

---

## 📥 Export Data

### Export Attendance (CSV)

1. Open dashboard: http://localhost:5000
2. Go to "Attendance" tab
3. Click "Export CSV"
4. File downloads automatically

### Export Reports (CSV)

1. Go to "Reports" tab
2. Select date range
3. Click "Generate Report"
4. Click "Export CSV"

### Database Backup

```bash
# Backup database
docker-compose exec -T compreface-postgres-db \
  pg_dump -U postgres frs_1bip > backup_$(date +%Y%m%d).sql

# Restore database
docker-compose exec -T compreface-postgres-db \
  psql -U postgres frs_1bip < backup_20251021.sql
```

---

## 🛠️ Configuration

### Main Configuration Files

1. **`.env`** - Main system configuration
   - Database password
   - Email settings
   - Performance tuning

2. **`camera-service/config/camera_config.env`** - Camera settings
   - RTSP URL
   - Recognition thresholds
   - Alert configuration

### Important Settings

**Recognition Threshold** (camera_config.env):
```bash
SIMILARITY_THRESHOLD=0.85  # 85% similarity required
# Higher = more strict
# Lower = more lenient
# Recommended: 0.80 - 0.90
```

**Frame Processing** (camera_config.env):
```bash
FRAME_SKIP=5  # Process every 5th frame
# Lower = more frequent checks, higher CPU
# Higher = less frequent checks, lower CPU
```

**Auto-Refresh** (dashboard):
- Default: 10 seconds
- Edit: `dashboard-service/src/static/js/dashboard.js`
- Change `REFRESH_INTERVAL`

---

## ⚙️ Common Tasks

### Add New Employee

1. CompreFace UI → Manage Collection
2. Add Subject → Enter name
3. Upload 3-5 photos
4. Done! System will recognize them immediately

### Remove Employee

1. CompreFace UI → Manage Collection
2. Select employee
3. Delete
4. All their face data is removed

### View Unauthorized Attempts

1. Dashboard → Unauthorized tab
2. Select time range
3. Review attempts
4. Check alert status

### Check Camera Health

1. Dashboard → Camera Status tab
2. View all cameras
3. Check online/offline status
4. Monitor detection counts

---

## 🚨 Troubleshooting

### Camera not connecting

```bash
# Check camera is accessible
ping 192.168.1.100

# Test RTSP with VLC Media Player
# Open VLC → Media → Open Network Stream
# Enter: rtsp://admin:password@192.168.1.100:554/Streaming/Channels/101
```

### No faces detected

- Check camera angle and positioning
- Ensure adequate lighting
- Lower `DET_PROB_THRESHOLD` in config
- Check camera resolution

### Dashboard not loading

```bash
# Check service status
docker-compose ps dashboard-service

# Check logs
docker-compose logs dashboard-service

# Restart service
docker-compose restart dashboard-service
```

### Database connection error

```bash
# Check database is running
docker-compose ps compreface-postgres-db

# Verify password in .env matches docker-compose.yml
```

---

## 🔒 Security Checklist

Before production deployment:

- [ ] Change database password in `.env`
- [ ] Change default camera passwords
- [ ] Update dashboard secret key
- [ ] Enable HTTPS (use reverse proxy)
- [ ] Configure firewall rules
- [ ] Set up regular backups
- [ ] Review and limit network access
- [ ] Enable email alerts (optional)

---

## 📚 Documentation

- **DEPLOYMENT_GUIDE.md** - Complete deployment instructions
- **OFFLINE_OPERATION_GUIDE.md** - Offline operation details
- **BRANDING_CUSTOMIZATION.md** - Logo and branding changes
- **camera-service/README.md** - Camera service documentation
- **dashboard-service/README.md** - Dashboard documentation

---

## 🆘 Getting Help

### Check Logs First

```bash
docker-compose logs --tail=100
```

### Verify Service Health

```bash
# Dashboard health
curl http://localhost:5000/health

# CompreFace API health
docker-compose logs compreface-api | grep -i "started"
```

### Common Commands

```bash
# Restart all services
docker-compose restart

# Stop all services
docker-compose down

# Start all services
docker-compose up -d

# Rebuild after changes
docker-compose build
docker-compose up -d
```

---

## 📊 System Requirements

**Minimum:**
- CPU: 4 cores
- RAM: 8 GB
- Storage: 50 GB SSD
- Network: 100 Mbps (local)

**Recommended:**
- CPU: 8+ cores
- RAM: 16+ GB
- Storage: 200+ GB SSD
- Network: 1 Gbps (local)

---

## ✅ Offline Operation

This system works **100% offline**:

- ✅ No internet required
- ✅ All processing local
- ✅ All data stays on your server
- ✅ Complete privacy
- ✅ Works in air-gapped environments

See **OFFLINE_OPERATION_GUIDE.md** for details.

---

## 🎯 Next Steps

1. ✅ System is running
2. ✅ Add all employees to CompreFace
3. ✅ Configure all cameras
4. ✅ Monitor dashboard
5. ⏭️ Set up email alerts (optional)
6. ⏭️ Configure HTTPS for production
7. ⏭️ Set up automated backups
8. ⏭️ Create user accounts for HR/Security team