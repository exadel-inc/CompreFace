# MELLAL BADR Dashboard Service

Real-time monitoring and analytics dashboard for the our Face Recognition & Attendance System.

## Features

✅ **Real-time Monitoring** - Live access log viewing with 10-second auto-refresh
✅ **Attendance Tracking** - Daily attendance reports with entry/exit times
✅ **Unauthorized Access Alerts** - Visual alerts for security incidents
✅ **Camera Status Monitoring** - Health check for all cameras
✅ **Analytics & Reports** - Hourly activity charts and date range reports
✅ **CSV Export** - Export attendance and reports to CSV files
✅ **Completely Offline** - No external dependencies, works on local network only
✅ **Responsive Design** - Works on desktop, tablet, and mobile devices

---

## Quick Start

### Access the Dashboard

Once the system is running:

```
http://localhost:5000
```

Or from another computer on the network:

```
http://[server-ip]:5000
```

---

## Dashboard Tabs

### 1. 🔴 Live Monitor

Real-time access log showing:
- Timestamp of each access attempt
- Camera that detected the person
- Person name (if authorized) or "Unknown"
- Authorization status (Authorized/Unauthorized)
- Recognition confidence percentage
- Alert status

**Features:**
- Auto-refresh every 10 seconds (configurable)
- Shows last 50 access attempts
- Color-coded status badges

### 2. 📋 Attendance

Today's attendance report showing:
- Employee name
- First entry time (arrival)
- Last entry time (departure)
- Total number of entries
- Camera used
- Average recognition confidence

**Features:**
- Export to CSV button
- Automatic refresh
- Sortable columns

### 3. ⚠️ Unauthorized Access

Security incidents log showing:
- All unauthorized access attempts
- Time and camera location
- Alert status
- Filter by time range (1 hour, 6 hours, 24 hours, 1 week)

**Features:**
- Highlighted alerts
- Time range filtering
- Count of total unauthorized attempts

### 4. 📹 Camera Status

Health monitoring for all cameras showing:
- Camera name and location
- Online/Warning/Offline status
- Last activity timestamp
- Detections in last hour
- Unauthorized attempts in last hour

**Status Indicators:**
- **Online (Green)** - Activity within last 5 minutes
- **Warning (Yellow)** - Activity within last 10 minutes
- **Offline (Red)** - No activity for 10+ minutes

### 5. 📊 Reports

Advanced reporting and analytics:

**Attendance Reports:**
- Select date range
- Generate detailed reports
- Export to CSV
- Shows attendance history

**Hourly Activity Chart:**
- Visual chart of access patterns
- Green bars = Authorized access
- Red bars = Unauthorized access
- 24-hour view

---

## API Endpoints

The dashboard provides REST API endpoints for integration:

### Summary Statistics

```
GET /api/stats/summary
```

Returns today's summary:
- Total access attempts
- Authorized count
- Unauthorized count
- Unique employees
- Active cameras

### Recent Access

```
GET /api/access/recent?limit=50
```

Returns recent access logs (default 50, max 100).

### Unauthorized Access

```
GET /api/access/unauthorized?hours=24
```

Returns unauthorized access attempts for specified hours.

### Today's Attendance

```
GET /api/attendance/today
```

Returns today's attendance with first/last entry times.

### Attendance Report

```
GET /api/attendance/report?start_date=2025-01-01&end_date=2025-01-31
```

Returns attendance report for date range.

### Camera Status

```
GET /api/camera/status
```

Returns status of all cameras.

### Hourly Statistics

```
GET /api/stats/hourly?hours=24
```

Returns hourly statistics for charts.

### Search Logs

```
GET /api/search?subject_name=John&is_authorized=true
```

Search access logs with filters.

### Health Check

```
GET /health
```

Returns service health status.

---

## Configuration

The dashboard is configured via environment variables in `docker-compose.yml`:

```yaml
environment:
  - DB_HOST=compreface-postgres-db     # PostgreSQL host
  - DB_PORT=5432                        # PostgreSQL port
  - DB_NAME=frs_1bip                   # Database name
  - DB_USER=postgres                    # Database user
  - DB_PASSWORD=your_password          # Database password
  - DASHBOARD_PORT=5000                 # Dashboard port
  - FLASK_DEBUG=false                   # Debug mode (false for production)
```

---

## Customization

### Change Refresh Interval

Edit `/dashboard-service/src/static/js/dashboard.js`:

```javascript
const CONFIG = {
    API_BASE_URL: '',
    REFRESH_INTERVAL: 10000,  // Change to desired milliseconds (e.g., 5000 = 5 seconds)
    COUNTDOWN_INTERVAL: 1000,
};
```

### Change Port

Edit `docker-compose.yml`:

```yaml
dashboard-service:
  ports:
    - "8080:5000"  # Access on port 8080 instead of 5000
```

### Customize Branding

Edit `/dashboard-service/src/templates/dashboard.html`:

```html
<h1>🏢 Your Organization Name - Face Recognition System</h1>
```

Edit `/dashboard-service/src/static/css/dashboard.css` for colors:

```css
:root {
    --primary-color: #2563eb;  /* Change to your brand color */
    --success-color: #10b981;
    --danger-color: #ef4444;
    /* ... */
}
```

---

## Offline Operation

The dashboard is designed to work **completely offline**:

✅ **No External CDNs** - All CSS/JS files are self-hosted
✅ **No Internet Required** - Works on local network only
✅ **Pure Vanilla JavaScript** - No jQuery or external libraries
✅ **Self-contained** - All resources included in Docker image

### Verifying Offline Operation

1. **Disconnect from Internet**
2. **Access Dashboard**: `http://localhost:5000`
3. **Check Browser Console** - No external requests
4. **Test All Features** - Everything should work

---

## Troubleshooting

### Dashboard not loading

**Check service status:**
```bash
docker-compose ps dashboard-service
```

**Check logs:**
```bash
docker-compose logs dashboard-service
```

**Expected output:**
```
Starting Dashboard Service on port 5000
Database: compreface-postgres-db:5432/frs_1bip
Dashboard will be available at http://localhost:5000
```

### Database connection errors

**Verify database is running:**
```bash
docker-compose ps compreface-postgres-db
```

**Test database connection:**
```bash
docker-compose exec dashboard-service python -c "import psycopg2; print('OK')"
```

**Check database password:**
Ensure `DB_PASSWORD` in `docker-compose.yml` matches `.env` file's `postgres_password`.

### No data showing

**Check camera service is running:**
```bash
docker-compose ps camera-service
```

**Verify access logs exist:**
```bash
docker-compose exec compreface-postgres-db psql -U postgres -d frs_1bip -c "SELECT COUNT(*) FROM access_logs;"
```

### Auto-refresh not working

**Check browser console** for JavaScript errors.

**Disable/Enable auto-refresh** checkbox.

**Manually refresh** using the 🔄 Refresh button.

---

## Security

### Production Deployment

1. **Change Secret Key** (in `app.py`):
   ```python
   app.config['SECRET_KEY'] = 'your-very-secure-random-key-here'
   ```

2. **Enable HTTPS** - Use reverse proxy (Nginx/Traefik)

3. **Add Authentication** - Implement login system

4. **Firewall Rules** - Restrict access to dashboard port:
   ```bash
   sudo ufw allow from 192.168.1.0/24 to any port 5000
   ```

5. **Disable Debug Mode** - Ensure `FLASK_DEBUG=false` in production

---

## CSV Export Format

### Attendance Export

```csv
subject_name,first_entry,last_entry,total_entries,camera_name
John_Doe,2025-10-21T08:15:00,2025-10-21T17:30:00,3,Main Entrance Gate
Jane_Smith,2025-10-21T08:20:00,2025-10-21T17:25:00,2,Main Entrance Gate
```

### Report Export

```csv
date,subject_name,first_entry,last_entry,entries_count
2025-10-21,John_Doe,2025-10-21T08:15:00,2025-10-21T17:30:00,3
2025-10-21,Jane_Smith,2025-10-21T08:20:00,2025-10-21T17:25:00,2
```

---

## Integration Examples

### Python

```python
import requests

# Get today's attendance
response = requests.get('http://localhost:5000/api/attendance/today')
attendance = response.json()

for record in attendance:
    print(f"{record['subject_name']} arrived at {record['first_entry']}")
```

### JavaScript

```javascript
fetch('http://localhost:5000/api/stats/summary')
    .then(response => response.json())
    .then(data => {
        console.log(`Total access today: ${data.total_today}`);
        console.log(`Unauthorized: ${data.unauthorized_today}`);
    });
```

### Curl

```bash
# Get summary stats
curl http://localhost:5000/api/stats/summary

# Get unauthorized access
curl "http://localhost:5000/api/access/unauthorized?hours=24"

# Search for specific person
curl "http://localhost:5000/api/search?subject_name=John"
```

---

## Development

### Run Locally (without Docker)

```bash
cd dashboard-service/src

# Install dependencies
pip install -r ../requirements.txt

# Set environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=frs_1bip
export DB_USER=postgres
export DB_PASSWORD=your_password

# Run application
python app.py
```

### Hot Reload for Development

Edit `docker-compose.yml`:

```yaml
dashboard-service:
  environment:
    - FLASK_DEBUG=true  # Enable debug mode
  volumes:
    - ./dashboard-service/src:/app  # Mount source code for hot reload
```

---

## Performance

### Expected Performance

- **API Response Time**: < 100ms for most endpoints
- **Dashboard Load Time**: < 2 seconds
- **Concurrent Users**: 50+ simultaneous users
- **Data Refresh**: Every 10 seconds without lag

### Optimization Tips

1. **Database Indexes** - Already created automatically
2. **Limit API Results** - Use `limit` parameter
3. **Cache Static Files** - Handled by Flask
4. **Use Production WSGI Server** - For high load, use Gunicorn:

```dockerfile
CMD ["gunicorn", "-w", "4", "-b", "0.0.0.0:5000", "app:app"]
```

---

## Monitoring

### Check Dashboard Health

```bash
curl http://localhost:5000/health
```

**Healthy response:**
```json
{
  "status": "healthy",
  "timestamp": "2025-10-21T14:30:00"
}
```


---

## Support

For issues:
1. Check logs: `docker-compose logs dashboard-service`
2. Verify database: `docker-compose ps compreface-postgres-db`
3. Test API: `curl http://localhost:5000/health`

---

**Version:** 1.0.0
**Last Updated:** 2025-10-21

