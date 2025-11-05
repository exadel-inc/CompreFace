# Camera Integration Service

Real-time face recognition and access control system for Hikvision cameras integrated with CompreFace.

## Features

✅ **Multi-Face Detection** - Detects and recognizes multiple faces simultaneously
✅ **Real-time Processing** - Processes video streams from Hikvision 8MP cameras
✅ **Unauthorized Access Alerts** - Instant alerts for unknown/unauthorized persons
✅ **Access Logging** - Complete audit trail of all access attempts
✅ **Configurable Thresholds** - Adjust similarity and detection confidence
✅ **Alert Cooldown** - Prevents alert spam with configurable cooldown periods
✅ **Database Integration** - PostgreSQL logging for attendance tracking
✅ **Debug Mode** - Save images of unauthorized access attempts
✅ **Multiple Camera Support** - Run multiple instances for different locations

---

## Architecture

```
Hikvision Camera (RTSP Stream)
    ↓
Camera Service (Python + OpenCV)
    ↓
Frame Capture & Processing
    ↓
CompreFace API (Face Recognition)
    ↓
Authorization Check (Known vs Unknown)
    ↓
├─ Authorized → Log to Database
└─ Unauthorized → Alert + Log to Database
    ↓
Alert Manager → Webhook/Email Notifications but we will stick to an internal alert system.
```

---

## Prerequisites

1. **Hikvision Camera** configured and accessible on network
2. **CompreFace** running with Recognition Service created
3. **PostgreSQL** database for access logs
4. **Docker & Docker Compose** installed

---

## Quick Start

### 1. Configure Camera Settings

Edit `config/camera_config.env`:

```bash
# Camera RTSP URL
CAMERA_RTSP_URL=rtsp://admin:password@192.168.1.100:554/Streaming/Channels/101

# Camera identification
CAMERA_NAME=Main Entrance Gate
CAMERA_LOCATION=Building A - Main Gate

# CompreFace API Key (get from CompreFace UI)
COMPREFACE_API_KEY=your-api-key-here
```

### 2. Get CompreFace API Key

1. Open CompreFace UI: `http://localhost:8000`
2. Go to **Applications** → Your Application
3. Find your **Recognition Service**
4. Copy the **API Key**
5. Paste it in `camera_config.env`

### 3. Add Authorized Users to CompreFace

Before running the camera service, add authorized users:

1. Go to CompreFace UI
2. Select your **Recognition Service**
3. Click **Manage Collection**
4. Add subjects (employees) with their photos
   - Subject Name: Employee name/ID
   - Upload multiple photos per person (recommended: 3-5)

### 4. Build and Run

Using Docker Compose (recommended):

```bash
# Build the camera service
docker-compose build camera-service

# Start the service
docker-compose up -d camera-service

# View logs
docker-compose logs -f camera-service
```

Or run standalone:

```bash
# Build the image
docker build -t 1bip-camera-service ./camera-service

# Run the container
docker run -d \
  --name camera-service-main-gate \
  --env-file ./camera-service/config/camera_config.env \
  --network compreface_default \
  -v $(pwd)/camera-service/logs:/app/logs \
  1bip-camera-service
```

---

## Configuration Options

### Camera Settings

| Variable | Description | Default |
|----------|-------------|---------|
| `CAMERA_RTSP_URL` | RTSP stream URL | `rtsp://admin:password@192.168.1.100:554/Streaming/Channels/101` |
| `CAMERA_NAME` | Camera identifier | `Main Entrance Gate` |
| `CAMERA_LOCATION` | Physical location | `Building A - Main Gate` |
| `FRAME_SKIP` | Process every Nth frame | `5` |
| `FRAME_WIDTH` | Video resolution width | `1920` |
| `FRAME_HEIGHT` | Video resolution height | `1080` |

### Recognition Settings

| Variable | Description | Default |
|----------|-------------|---------|
| `COMPREFACE_API_KEY` | Recognition service API key | *Required* |
| `SIMILARITY_THRESHOLD` | Minimum similarity for authorization (0.0-1.0) | `0.85` (85%) |
| `DET_PROB_THRESHOLD` | Face detection confidence (0.0-1.0) | `0.8` (80%) |
| `MAX_FACES_PER_FRAME` | Maximum faces to detect per frame | `10` |

### Alert Settings

| Variable | Description | Default |
|----------|-------------|---------|
| `ENABLE_ALERTS` | Enable unauthorized access alerts | `true` |
| `ALERT_WEBHOOK_URL` | Webhook URL for notifications | *(empty)* |
| `ALERT_EMAIL` | Email for notifications | *(empty)* |
| `ALERT_COOLDOWN_SECONDS` | Time between duplicate alerts | `60` |

### Debug Settings

| Variable | Description | Default |
|----------|-------------|---------|
| `SAVE_DEBUG_IMAGES` | Save images of unauthorized access | `true` |
| `DEBUG_IMAGE_PATH` | Path for debug images | `/app/logs/debug_images` |

---

## Hikvision Camera RTSP URL Format

```
rtsp://[username]:[password]@[camera_ip]:[port]/Streaming/Channels/[channel]
```

**Common Channels:**
- `101` - Main Stream (High Quality) - Recommended for 8MP cameras
- `102` - Sub Stream (Lower Quality) - For bandwidth-constrained networks

**Examples:**

```bash
# Standard format
rtsp://admin:Admin123@192.168.1.100:554/Streaming/Channels/101

# With special characters in password (URL encode them)
rtsp://admin:P%40ssw0rd%21@192.168.1.100:554/Streaming/Channels/101
```

---

## Database Schema

The service automatically creates the `access_logs` table:

```sql
CREATE TABLE access_logs (
    id SERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    camera_name VARCHAR(255) NOT NULL,
    camera_location VARCHAR(255),
    subject_name VARCHAR(255),
    is_authorized BOOLEAN NOT NULL,
    similarity FLOAT,
    face_box JSON,
    alert_sent BOOLEAN DEFAULT FALSE,
    image_path VARCHAR(500),
    metadata JSON
);
```

### Query Examples

**Recent unauthorized access:**
```sql
SELECT * FROM access_logs
WHERE is_authorized = FALSE
ORDER BY timestamp DESC
LIMIT 10;
```

**Daily attendance report:**
```sql
SELECT subject_name, MIN(timestamp) as first_entry, MAX(timestamp) as last_entry
FROM access_logs
WHERE is_authorized = TRUE
  AND timestamp >= CURRENT_DATE
GROUP BY subject_name
ORDER BY first_entry;
```

**Unauthorized access attempts today:**
```sql
SELECT COUNT(*) as attempts, camera_name
FROM access_logs
WHERE is_authorized = FALSE
  AND timestamp >= CURRENT_DATE
GROUP BY camera_name;
```

---

## Alert Webhooks

The service can send alerts to webhook endpoints (Slack, Discord, Teams, custom):

### Example: Slack Webhook

1. Create a Slack Incoming Webhook
2. Set `ALERT_WEBHOOK_URL` in config:
   ```bash
   ALERT_WEBHOOK_URL=https://hooks.slack.com/services/YOUR/WEBHOOK/URL
   ```

### Alert Payload Format

```json
{
  "alert_type": "UNAUTHORIZED_ACCESS",
  "timestamp": "2025-10-21T14:30:00",
  "camera_name": "Main Entrance Gate",
  "camera_location": "Building A - Main Gate",
  "subject_name": "Unknown Person",
  "similarity": null,
  "face_count": 1,
  "severity": "HIGH"
}
```

---

## Multiple Camera Support

To monitor multiple cameras, create separate config files and run multiple instances:

### Camera 1: Main Entrance
```bash
# config/camera_main_gate.env
CAMERA_RTSP_URL=rtsp://admin:pass@192.168.1.100:554/Streaming/Channels/101
CAMERA_NAME=Main Entrance Gate
CAMERA_LOCATION=Building A
```

### Camera 2: Back Entrance
```bash
# config/camera_back_gate.env
CAMERA_RTSP_URL=rtsp://admin:pass@192.168.1.101:554/Streaming/Channels/101
CAMERA_NAME=Back Entrance Gate
CAMERA_LOCATION=Building B
```

### Run both instances:
```bash
docker run -d --name camera-main-gate \
  --env-file config/camera_main_gate.env \
  1bip-camera-service

docker run -d --name camera-back-gate \
  --env-file config/camera_back_gate.env \
  1bip-camera-service
```

---

## Monitoring & Logs

### View Live Logs
```bash
docker-compose logs -f camera-service
```

### Check Service Status
```bash
docker-compose ps camera-service
```

### Access Log Files
```bash
# Service logs
tail -f camera-service/logs/camera_service.log

# Debug images (if enabled)
ls -lh camera-service/logs/debug_images/
```

---

## Troubleshooting

### Issue: Cannot connect to camera

**Solutions:**
1. Verify camera IP and RTSP port (default: 554)
2. Check camera username/password
3. Ensure camera RTSP is enabled in camera settings
4. Test RTSP URL with VLC Media Player: `Media → Open Network Stream`

### Issue: No faces detected

**Solutions:**
1. Check camera angle and positioning
2. Ensure adequate lighting
3. Lower `DET_PROB_THRESHOLD` (try 0.6)
4. Check `FRAME_WIDTH` and `FRAME_HEIGHT` settings

### Issue: Too many false positives

**Solutions:**
1. Increase `SIMILARITY_THRESHOLD` (try 0.90)
2. Add more photos per person in CompreFace (3-5 recommended)
3. Use photos with different angles and lighting

### Issue: High CPU usage

**Solutions:**
1. Increase `FRAME_SKIP` (process fewer frames)
2. Reduce `FRAME_WIDTH` and `FRAME_HEIGHT` (use 1280x720 instead of 1920x1080)
3. Reduce `MAX_FACES_PER_FRAME`

### Issue: Alerts spamming

**Solutions:**
1. Increase `ALERT_COOLDOWN_SECONDS` (try 120)
2. Check for camera motion/vibration causing repeated detections

---

## Performance Optimization

### For High-Traffic Entrances

```bash
FRAME_SKIP=3              # Process more frames
MAX_FACES_PER_FRAME=15    # Detect more faces
SIMILARITY_THRESHOLD=0.80  # More lenient matching
```

### For Low-Power Devices

```bash
FRAME_SKIP=10             # Process fewer frames
FRAME_WIDTH=1280          # Lower resolution
FRAME_HEIGHT=720
MAX_FACES_PER_FRAME=5     # Fewer faces per frame
```

### For Maximum Security

```bash
SIMILARITY_THRESHOLD=0.90  # Strict matching
SAVE_DEBUG_IMAGES=true     # Save all unauthorized attempts
ALERT_COOLDOWN_SECONDS=30  # More frequent alerts
```

---

## Development

### Run Locally (without Docker)

1. Install dependencies:
   ```bash
   cd camera-service
   pip install -r requirements.txt
   ```

2. Set environment variables:
   ```bash
   export CAMERA_RTSP_URL="rtsp://..."
   export COMPREFACE_API_KEY="your-key"
   # ... other variables
   ```

3. Run the service:
   ```bash
   python src/camera_service.py
   ```

---

## Security Recommendations

1. **Change default camera passwords** - Never use default Hikvision credentials
2. **Use network segmentation** - Put cameras on isolated VLAN
3. **Enable HTTPS** - Use SSL/TLS for CompreFace API
4. **Secure database** - Use strong PostgreSQL password
5. **Limit network access** - Firewall rules for camera RTSP ports
6. **Regular updates** - Keep camera firmware updated

---

## API Integration

The access logs can be queried via custom API endpoints. Example using Python:

```python
import psycopg2

conn = psycopg2.connect(
    host="localhost",
    database="frs_1bip",
    user="postgres",
    password="your-password"
)

# Get today's attendance
cursor = conn.cursor()
cursor.execute("""
    SELECT DISTINCT subject_name, MIN(timestamp) as entry_time
    FROM access_logs
    WHERE is_authorized = TRUE
      AND timestamp::date = CURRENT_DATE
    GROUP BY subject_name
    ORDER BY entry_time;
""")

for row in cursor.fetchall():
    print(f"{row[0]} entered at {row[1]}")
```

---

## Future Enhancements

- [ ] Web dashboard for real-time monitoring
- [ ] Email alerts with attached images
- [ ] SMS alerts for critical security events
- [ ] Integration with access control systems (door locks)
- [ ] Attendance report generation
- [ ] Mobile app for alert notifications
- [ ] AI-based anomaly detection
- [ ] License plate recognition integration

---

## Support

For issues and questions:
- Check logs: `camera-service/logs/camera_service.log`
- Review CompreFace logs: `docker-compose logs compreface-api`
- Verify database connection: `docker-compose ps`

---

## License

Part of my Face Recognition System
Based on CompreFace (Apache 2.0 License)

---

**Last Updated:** 2025-10-21
**Version:** 1.0.0
