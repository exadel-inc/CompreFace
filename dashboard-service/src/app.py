#!/usr/bin/env python3
"""
 Dashboard Service
Real-time monitoring interface for face recognition and attendance system
Runs completely offline on local network
"""

from flask import Flask, render_template, jsonify, request, send_from_directory
from flask_cors import CORS
import psycopg2
from psycopg2.extras import RealDictCursor
import os
import logging
from datetime import datetime, timedelta
from typing import List, Dict, Any, Optional
import json
import requests
import base64
from urllib.parse import quote
import re

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Flask app initialization
app = Flask(__name__)
CORS(app)  # Enable CORS for API access
app.config['SECRET_KEY'] = os.getenv('SECRET_KEY', '1bip-dashboard-secret-key-change-in-production')

# Database configuration
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'compreface-postgres-db'),
    'port': int(os.getenv('DB_PORT', '5432')),
    'database': os.getenv('DB_NAME', 'morocco_1bip_frs'),
    'user': os.getenv('DB_USER', 'postgres'),
    'password': os.getenv('DB_PASSWORD', 'admin')
}

# CompreFace API configuration
COMPREFACE_API_URL = os.getenv('COMPREFACE_API_URL', 'http://compreface-api:8080')
COMPREFACE_API_KEY = os.getenv('COMPREFACE_API_KEY', '00000000-0000-0000-0000-000000000002')


class DatabaseConnection:
    """Database connection manager"""

    def __init__(self):
        self.conn = None

    def __enter__(self):
        self.conn = psycopg2.connect(**DB_CONFIG)
        return self.conn

    def __exit__(self, exc_type, exc_val, exc_tb):
        if self.conn:
            self.conn.close()


# ============================================
# API ENDPOINTS
# ============================================

@app.route('/')
def index():
    """Main dashboard page"""
    return render_template('dashboard.html')


@app.route('/api/stats/summary')
def get_summary_stats():
    """Get summary statistics for dashboard"""
    try:
        with DatabaseConnection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                # Total access attempts today
                cursor.execute("""
                    SELECT COUNT(*) as total
                    FROM access_logs
                    WHERE timestamp >= CURRENT_DATE
                """)
                total_today = cursor.fetchone()['total']

                # Authorized access today
                cursor.execute("""
                    SELECT COUNT(*) as authorized
                    FROM access_logs
                    WHERE timestamp >= CURRENT_DATE
                    AND is_authorized = TRUE
                """)
                authorized_today = cursor.fetchone()['authorized']

                # Unauthorized access today
                cursor.execute("""
                    SELECT COUNT(*) as unauthorized
                    FROM access_logs
                    WHERE timestamp >= CURRENT_DATE
                    AND is_authorized = FALSE
                """)
                unauthorized_today = cursor.fetchone()['unauthorized']

                # Unique employees today
                cursor.execute("""
                    SELECT COUNT(DISTINCT subject_name) as unique_employees
                    FROM access_logs
                    WHERE timestamp >= CURRENT_DATE
                    AND is_authorized = TRUE
                    AND subject_name IS NOT NULL
                """)
                unique_employees = cursor.fetchone()['unique_employees']

                # Active cameras (cameras that reported in last 5 minutes)
                cursor.execute("""
                    SELECT COUNT(DISTINCT camera_name) as active_cameras
                    FROM access_logs
                    WHERE timestamp >= NOW() - INTERVAL '5 minutes'
                """)
                active_cameras = cursor.fetchone()['active_cameras']

                return jsonify({
                    'total_today': total_today,
                    'authorized_today': authorized_today,
                    'unauthorized_today': unauthorized_today,
                    'unique_employees': unique_employees,
                    'active_cameras': active_cameras,
                    'timestamp': datetime.now().isoformat()
                })

    except Exception as e:
        logger.error(f"Error getting summary stats: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/api/access/recent')
def get_recent_access():
    """Get recent access attempts"""
    limit = request.args.get('limit', 50, type=int)

    try:
        with DatabaseConnection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute("""
                    SELECT
                        id,
                        timestamp,
                        camera_name,
                        camera_location,
                        subject_name,
                        is_authorized,
                        similarity,
                        alert_sent
                    FROM access_logs
                    ORDER BY timestamp DESC
                    LIMIT %s
                """, (limit,))

                records = cursor.fetchall()

                # Convert to JSON-serializable format
                for record in records:
                    record['timestamp'] = record['timestamp'].isoformat()
                    if record['similarity']:
                        record['similarity'] = float(record['similarity'])

                return jsonify(records)

    except Exception as e:
        logger.error(f"Error getting recent access: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/api/access/unauthorized')
def get_unauthorized_access():
    """Get unauthorized access attempts"""
    hours = request.args.get('hours', 24, type=int)

    try:
        with DatabaseConnection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute("""
                    SELECT
                        id,
                        timestamp,
                        camera_name,
                        camera_location,
                        subject_name,
                        similarity,
                        alert_sent,
                        image_path
                    FROM access_logs
                    WHERE is_authorized = FALSE
                    AND timestamp >= NOW() - INTERVAL '%s hours'
                    ORDER BY timestamp DESC
                """, (hours,))

                records = cursor.fetchall()

                for record in records:
                    record['timestamp'] = record['timestamp'].isoformat()
                    if record['similarity']:
                        record['similarity'] = float(record['similarity'])

                return jsonify(records)

    except Exception as e:
        logger.error(f"Error getting unauthorized access: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/api/attendance/today')
def get_attendance_today():
    """Get today's attendance (first entry per employee)"""
    try:
        with DatabaseConnection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute("""
                    SELECT
                        subject_name,
                        MIN(timestamp) as first_entry,
                        MAX(timestamp) as last_entry,
                        COUNT(*) as total_entries,
                        camera_name,
                        AVG(similarity) as avg_similarity
                    FROM access_logs
                    WHERE is_authorized = TRUE
                    AND subject_name IS NOT NULL
                    AND timestamp >= CURRENT_DATE
                    GROUP BY subject_name, camera_name
                    ORDER BY first_entry
                """)

                records = cursor.fetchall()

                for record in records:
                    record['first_entry'] = record['first_entry'].isoformat()
                    record['last_entry'] = record['last_entry'].isoformat()
                    if record['avg_similarity']:
                        record['avg_similarity'] = float(record['avg_similarity'])

                return jsonify(records)

    except Exception as e:
        logger.error(f"Error getting attendance: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/api/attendance/report')
def get_attendance_report():
    """Get attendance report for date range with advanced filters"""
    start_date = request.args.get('start_date', datetime.now().date().isoformat())
    end_date = request.args.get('end_date', datetime.now().date().isoformat())
    name_filter = request.args.get('name', '').strip()
    department_filter = request.args.get('department', '').strip()
    sub_department_filter = request.args.get('sub_department', '').strip()
    status_filter = request.args.get('status', '').strip()  # 'authorized', 'unauthorized', or ''

    try:
        with DatabaseConnection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                # Build dynamic query
                query = """
                    SELECT
                        DATE(timestamp) as date,
                        subject_name,
                        department,
                        sub_department,
                        MIN(timestamp) as first_entry,
                        MAX(timestamp) as last_entry,
                        COUNT(*) as entries_count,
                        AVG(similarity) as avg_similarity,
                        is_authorized
                    FROM access_logs
                    WHERE subject_name IS NOT NULL
                    AND timestamp::date BETWEEN %s AND %s
                """
                params = [start_date, end_date]

                # Apply filters
                if name_filter:
                    query += " AND LOWER(subject_name) LIKE LOWER(%s)"
                    params.append(f'%{name_filter}%')

                if department_filter:
                    query += " AND department = %s"
                    params.append(department_filter)

                if sub_department_filter:
                    query += " AND LOWER(sub_department) LIKE LOWER(%s)"
                    params.append(f'%{sub_department_filter}%')

                if status_filter == 'authorized':
                    query += " AND is_authorized = TRUE"
                elif status_filter == 'unauthorized':
                    query += " AND is_authorized = FALSE"

                query += """
                    GROUP BY DATE(timestamp), subject_name, department, sub_department, is_authorized
                    ORDER BY date DESC, first_entry
                """

                cursor.execute(query, params)
                records = cursor.fetchall()

                for record in records:
                    record['date'] = record['date'].isoformat()
                    record['first_entry'] = record['first_entry'].isoformat()
                    record['last_entry'] = record['last_entry'].isoformat()

                return jsonify(records)

    except Exception as e:
        logger.error(f"Error getting attendance report: {e}")
        return jsonify({'error': str(e)}), 500


def extract_camera_ip_from_rtsp(rtsp_url: str) -> Optional[str]:
    """
    Extract IP address from RTSP URL
    Example: rtsp://admin:password@192.168.1.73:554/... -> 192.168.1.73
    """
    try:
        # Pattern to match IP address in RTSP URL
        pattern = r'@?([\d]{1,3}\.[\d]{1,3}\.[\d]{1,3}\.[\d]{1,3})'
        match = re.search(pattern, rtsp_url)
        if match:
            return match.group(1)
        return None
    except Exception as e:
        logger.error(f"Error extracting IP from RTSP URL: {e}")
        return None


@app.route('/api/camera/status')
def get_camera_status():
    """
    Get status of all cameras based on recent database activity
    Simple and reliable: If camera service is writing to DB, camera is working
    """
    try:
        # Get camera configuration
        camera_rtsp_url = os.getenv('CAMERA_RTSP_URL', '')
        camera_ip = extract_camera_ip_from_rtsp(camera_rtsp_url) if camera_rtsp_url else None

        with DatabaseConnection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                # Check for recent activity (last hour)
                cursor.execute("""
                    SELECT
                        camera_name,
                        camera_location,
                        MAX(timestamp) as last_activity,
                        COUNT(*) as detections_last_hour,
                        COUNT(CASE WHEN is_authorized = FALSE THEN 1 END) as unauthorized_last_hour
                    FROM access_logs
                    WHERE timestamp >= NOW() - INTERVAL '1 hour'
                    GROUP BY camera_name, camera_location
                    ORDER BY last_activity DESC
                """)

                cameras = cursor.fetchall()

                # If no cameras in DB but we have RTSP URL configured, add placeholder
                if not cameras and camera_rtsp_url:
                    camera_name = os.getenv('CAMERA_NAME', '1BIP Portail Principal')
                    camera_location = os.getenv('CAMERA_LOCATION', '1BIP')
                    cameras = [{
                        'camera_name': camera_name,
                        'camera_location': camera_location,
                        'last_activity': None,
                        'detections_last_hour': 0,
                        'unauthorized_last_hour': 0
                    }]

                for camera in cameras:
                    # Format last activity if exists
                    if camera['last_activity']:
                        camera['last_activity'] = camera['last_activity'].isoformat()
                        last_activity = datetime.fromisoformat(camera['last_activity'])
                        time_diff = datetime.now() - last_activity.replace(tzinfo=None)
                        seconds_since_activity = time_diff.total_seconds()
                    else:
                        seconds_since_activity = float('inf')

                    # Simple, reliable status determination based on database activity
                    # If camera service is writing to DB, camera + service are working
                    if seconds_since_activity < 180:  # Activity within 3 minutes
                        camera['status'] = 'online'
                        camera['status_reason'] = f'Active - Last detection {int(seconds_since_activity)} seconds ago'
                    elif seconds_since_activity < 600:  # Activity within 10 minutes
                        camera['status'] = 'warning'
                        camera['status_reason'] = f'No recent detections for {int(seconds_since_activity / 60)} minutes'
                    else:
                        # No recent activity - check if camera service might be starting up
                        # or if there simply haven't been any faces to detect
                        camera['status'] = 'offline'
                        if seconds_since_activity == float('inf'):
                            camera['status_reason'] = 'Camera service starting up or no activity recorded yet'
                        else:
                            minutes_ago = int(seconds_since_activity / 60)
                            camera['status_reason'] = f'No activity for {minutes_ago} minutes - Camera or service may be offline'

                    # Add camera IP for reference
                    camera['camera_ip'] = camera_ip

                return jsonify(cameras)

    except Exception as e:
        logger.error(f"Error getting camera status: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/api/stats/hourly')
def get_hourly_stats():
    """Get hourly statistics for charts"""
    hours = request.args.get('hours', 24, type=int)

    try:
        with DatabaseConnection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute("""
                    SELECT
                        DATE_TRUNC('hour', timestamp) as hour,
                        COUNT(*) as total,
                        COUNT(CASE WHEN is_authorized = TRUE THEN 1 END) as authorized,
                        COUNT(CASE WHEN is_authorized = FALSE THEN 1 END) as unauthorized
                    FROM access_logs
                    WHERE timestamp >= NOW() - INTERVAL '%s hours'
                    GROUP BY DATE_TRUNC('hour', timestamp)
                    ORDER BY hour
                """, (hours,))

                records = cursor.fetchall()

                for record in records:
                    record['hour'] = record['hour'].isoformat()

                return jsonify(records)

    except Exception as e:
        logger.error(f"Error getting hourly stats: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/api/employees/list')
def get_employees_list():
    """Get list of all recognized employees"""
    try:
        with DatabaseConnection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute("""
                    SELECT
                        subject_name,
                        COUNT(*) as total_accesses,
                        MAX(timestamp) as last_seen,
                        MIN(timestamp) as first_seen
                    FROM access_logs
                    WHERE is_authorized = TRUE
                    AND subject_name IS NOT NULL
                    GROUP BY subject_name
                    ORDER BY last_seen DESC
                """)

                employees = cursor.fetchall()

                for emp in employees:
                    emp['last_seen'] = emp['last_seen'].isoformat()
                    emp['first_seen'] = emp['first_seen'].isoformat()

                return jsonify(employees)

    except Exception as e:
        logger.error(f"Error getting employees list: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/api/search')
def search_access_logs():
    """Search access logs"""
    subject_name = request.args.get('subject_name', '')
    camera_name = request.args.get('camera_name', '')
    start_date = request.args.get('start_date', '')
    end_date = request.args.get('end_date', '')
    is_authorized = request.args.get('is_authorized', '')

    query = "SELECT * FROM access_logs WHERE 1=1"
    params = []

    if subject_name:
        query += " AND subject_name ILIKE %s"
        params.append(f"%{subject_name}%")

    if camera_name:
        query += " AND camera_name ILIKE %s"
        params.append(f"%{camera_name}%")

    if start_date:
        query += " AND timestamp >= %s"
        params.append(start_date)

    if end_date:
        query += " AND timestamp <= %s"
        params.append(end_date)

    if is_authorized:
        query += " AND is_authorized = %s"
        params.append(is_authorized.lower() == 'true')

    query += " ORDER BY timestamp DESC LIMIT 100"

    try:
        with DatabaseConnection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute(query, params)
                records = cursor.fetchall()

                for record in records:
                    record['timestamp'] = record['timestamp'].isoformat()
                    if record['similarity']:
                        record['similarity'] = float(record['similarity'])

                return jsonify(records)

    except Exception as e:
        logger.error(f"Error searching access logs: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/health')
def health_check():
    """Health check endpoint"""
    try:
        with DatabaseConnection() as conn:
            with conn.cursor() as cursor:
                cursor.execute("SELECT 1")

        return jsonify({'status': 'healthy', 'timestamp': datetime.now().isoformat()})
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        return jsonify({'status': 'unhealthy', 'error': str(e)}), 500


# ============================================
# STATIC FILES (for offline operation)
# ============================================

@app.route('/static/<path:filename>')
def serve_static(filename):
    """Serve static files"""
    return send_from_directory('static', filename)


# ============================================
# CAPTURED IMAGES (from camera service)
# ============================================

@app.route('/api/images/<path:filename>')
def serve_captured_image(filename):
    """Serve captured images from camera service"""
    try:
        camera_logs_path = os.getenv('CAMERA_LOGS_PATH', '/app/camera_logs')
        image_path = os.path.join(camera_logs_path, 'debug_images', filename)

        if os.path.exists(image_path):
            return send_from_directory(
                os.path.join(camera_logs_path, 'debug_images'),
                filename,
                mimetype='image/jpeg'
            )
        else:
            logger.warning(f"Image not found: {image_path}")
            # Return placeholder image
            return jsonify({'error': 'Image not found'}), 404
    except Exception as e:
        logger.error(f"Error serving image {filename}: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/api/images/latest')
def get_latest_images():
    """Get list of latest captured images with pagination (unauthorized access only)"""
    try:
        # Get pagination parameters
        page = request.args.get('page', 1, type=int)
        per_page = request.args.get('per_page', 20, type=int)

        # Limit per_page to prevent abuse
        per_page = min(per_page, 100)

        camera_logs_path = os.getenv('CAMERA_LOGS_PATH', '/app/camera_logs')
        debug_images_path = os.path.join(camera_logs_path, 'debug_images')

        if not os.path.exists(debug_images_path):
            return jsonify({
                'images': [],
                'total': 0,
                'page': page,
                'per_page': per_page,
                'total_pages': 0
            })

        # Get all UNAUTHORIZED images only (filter by filename prefix)
        images = []
        for filename in os.listdir(debug_images_path):
            # Only include images starting with "unauthorized_"
            if filename.startswith('unauthorized_') and filename.endswith(('.jpg', '.jpeg', '.png')):
                filepath = os.path.join(debug_images_path, filename)
                mtime = os.path.getmtime(filepath)
                images.append({
                    'filename': filename,
                    'timestamp': mtime,
                    'url': f'/api/images/{filename}'
                })

        # Sort by timestamp (newest first)
        images.sort(key=lambda x: x['timestamp'], reverse=True)

        # Calculate pagination
        total = len(images)
        total_pages = (total + per_page - 1) // per_page  # Ceiling division
        start_idx = (page - 1) * per_page
        end_idx = start_idx + per_page

        # Get page slice
        page_images = images[start_idx:end_idx]

        return jsonify({
            'images': page_images,
            'total': total,
            'page': page,
            'per_page': per_page,
            'total_pages': total_pages
        })
    except Exception as e:
        logger.error(f"Error getting latest images: {e}")
        return jsonify({
            'error': str(e),
            'images': [],
            'total': 0,
            'page': 1,
            'per_page': per_page,
            'total_pages': 0
        }), 500


@app.route('/api/images/gallery')
def get_gallery_images():
    """Get gallery images with advanced filters and pagination"""
    try:
        # Get filter parameters
        page = request.args.get('page', 1, type=int)
        per_page = request.args.get('per_page', 20, type=int)
        name_filter = request.args.get('name', '').strip()
        department_filter = request.args.get('department', '').strip()
        sub_department_filter = request.args.get('sub_department', '').strip()
        status_filter = request.args.get('status', '').strip()  # 'authorized', 'unauthorized', or ''

        # Limit per_page
        per_page = min(per_page, 100)

        camera_logs_path = os.getenv('CAMERA_LOGS_PATH', '/app/camera_logs')
        debug_images_path = os.path.join(camera_logs_path, 'debug_images')

        if not os.path.exists(debug_images_path):
            return jsonify({
                'images': [],
                'total': 0,
                'page': page,
                'per_page': per_page,
                'total_pages': 0
            })

        # Query database for metadata
        with DatabaseConnection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                # Build query with filters
                query = """
                    SELECT
                        image_path,
                        subject_name,
                        department,
                        sub_department,
                        is_authorized,
                        similarity,
                        timestamp,
                        camera_name
                    FROM access_logs
                    WHERE image_path IS NOT NULL
                """
                params = []

                # Apply filters
                if name_filter:
                    query += " AND LOWER(subject_name) LIKE LOWER(%s)"
                    params.append(f'%{name_filter}%')

                if department_filter:
                    query += " AND department = %s"
                    params.append(department_filter)

                if sub_department_filter:
                    query += " AND sub_department = %s"
                    params.append(sub_department_filter)

                if status_filter == 'authorized':
                    query += " AND is_authorized = TRUE"
                elif status_filter == 'unauthorized':
                    query += " AND is_authorized = FALSE"

                # Count total
                count_query = f"SELECT COUNT(*) FROM ({query}) AS filtered"
                cursor.execute(count_query, params)
                total = cursor.fetchone()['count']

                # Get paginated results
                query += " ORDER BY timestamp DESC LIMIT %s OFFSET %s"
                params.extend([per_page, (page - 1) * per_page])

                cursor.execute(query, params)
                records = cursor.fetchall()

        # Build response
        images = []
        for record in records:
            filename = record['image_path']
            filepath = os.path.join(debug_images_path, filename)

            # Check if file exists
            if os.path.exists(filepath):
                images.append({
                    'filename': filename,
                    'url': f'/api/images/{filename}',
                    'subject_name': record['subject_name'] or 'Unknown',
                    'department': record['department'],
                    'sub_department': record['sub_department'],
                    'is_authorized': record['is_authorized'],
                    'similarity': float(record['similarity']) if record['similarity'] else None,
                    'timestamp': record['timestamp'].isoformat(),
                    'camera_name': record['camera_name']
                })

        total_pages = (total + per_page - 1) // per_page

        return jsonify({
            'images': images,
            'total': total,
            'page': page,
            'per_page': per_page,
            'total_pages': total_pages
        })

    except Exception as e:
        logger.error(f"Error getting gallery images: {e}")
        return jsonify({
            'error': str(e),
            'images': [],
            'total': 0,
            'page': 1,
            'per_page': per_page,
            'total_pages': 0
        }), 500


@app.route('/api/departments')
def get_departments():
    """Get list of departments and sub-departments for filters"""
    try:
        with DatabaseConnection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                # Get unique departments
                cursor.execute("""
                    SELECT DISTINCT department
                    FROM access_logs
                    WHERE department IS NOT NULL
                    ORDER BY department
                """)
                departments = [row['department'] for row in cursor.fetchall()]

                # Get sub-departments grouped by department
                cursor.execute("""
                    SELECT DISTINCT department, sub_department
                    FROM access_logs
                    WHERE department IS NOT NULL AND sub_department IS NOT NULL
                    ORDER BY department, sub_department
                """)
                rows = cursor.fetchall()

                sub_departments = {}
                for row in rows:
                    dept = row['department']
                    if dept not in sub_departments:
                        sub_departments[dept] = []
                    if row['sub_department'] not in sub_departments[dept]:
                        sub_departments[dept].append(row['sub_department'])

        return jsonify({
            'departments': departments,
            'sub_departments': sub_departments
        })

    except Exception as e:
        logger.error(f"Error getting departments: {e}")
        return jsonify({
            'departments': [],
            'sub_departments': {}
        }), 500


@app.route('/api/access/unauthorized_paginated')
def get_unauthorized_paginated():
    """Get unauthorized access attempts with pagination"""
    try:
        hours = request.args.get('hours', 24, type=int)
        page = request.args.get('page', 1, type=int)
        per_page = request.args.get('per_page', 20, type=int)

        # Limit per_page
        per_page = min(per_page, 100)

        with DatabaseConnection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                # Count total
                cursor.execute("""
                    SELECT COUNT(*) as count
                    FROM access_logs
                    WHERE is_authorized = FALSE
                    AND timestamp >= NOW() - INTERVAL '%s hours'
                """, (hours,))
                total = cursor.fetchone()['count']

                # Get paginated results
                cursor.execute("""
                    SELECT *
                    FROM access_logs
                    WHERE is_authorized = FALSE
                    AND timestamp >= NOW() - INTERVAL '%s hours'
                    ORDER BY timestamp DESC
                    LIMIT %s OFFSET %s
                """, (hours, per_page, (page - 1) * per_page))

                records = cursor.fetchall()

                # Convert to JSON-serializable format
                for record in records:
                    record['timestamp'] = record['timestamp'].isoformat()

        total_pages = (total + per_page - 1) // per_page

        return jsonify({
            'records': records,
            'total': total,
            'page': page,
            'per_page': per_page,
            'total_pages': total_pages
        })

    except Exception as e:
        logger.error(f"Error getting unauthorized access: {e}")
        return jsonify({
            'error': str(e),
            'records': [],
            'total': 0,
            'page': 1,
            'per_page': per_page,
            'total_pages': 0
        }), 500


# ============================================
# PERSONNEL MANAGEMENT (CompreFace Integration)
# ============================================

@app.route('/api/personnel', methods=['GET'])
def get_personnel_list():
    """Get list of all personnel from CompreFace + our metadata DB"""
    try:
        # Step 1: Get all subjects from CompreFace
        url = f"{COMPREFACE_API_URL}/api/v1/recognition/subjects"
        headers = {'x-api-key': COMPREFACE_API_KEY}

        response = requests.get(url, headers=headers)
        response.raise_for_status()

        subjects = response.json().get('subjects', [])
        logger.info(f"Fetched {len(subjects)} subjects from CompreFace: {subjects}")

        # Step 2: Get metadata from OUR database
        personnel_list = []

        with DatabaseConnection() as conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                for subject in subjects:
                    # Fetch metadata from our personnel_metadata table
                    cursor.execute("""
                        SELECT subject_name, department, sub_department, rank, created_date
                        FROM personnel_metadata
                        WHERE subject_name = %s
                    """, (subject,))

                    metadata_row = cursor.fetchone()

                    if metadata_row:
                        # Personnel with metadata
                        personnel_list.append({
                            'subject': subject,
                            'name': subject,
                            'department': metadata_row['department'] or '',
                            'sub_department': metadata_row['sub_department'] or '',
                            'rank': metadata_row['rank'] or '',
                            'created_date': metadata_row['created_date'].isoformat() if metadata_row['created_date'] else ''
                        })
                    else:
                        # Personnel without metadata (added via port 8000 directly)
                        logger.warning(f"Subject '{subject}' has no metadata in DB (added externally?)")
                        personnel_list.append({
                            'subject': subject,
                            'name': subject,
                            'department': '',
                            'sub_department': '',
                            'rank': '',
                            'created_date': ''
                        })

        logger.info(f"Returning {len(personnel_list)} personnel records")
        return jsonify({'personnel': personnel_list})

    except Exception as e:
        logger.error(f"Error getting personnel list: {e}")
        return jsonify({'error': str(e), 'personnel': []}), 500


@app.route('/api/personnel', methods=['POST'])
def add_personnel():
    """Add new personnel to CompreFace with photos"""
    try:
        # Parse form data
        name = request.form.get('name', '').strip()
        department = request.form.get('department', '').strip()
        sub_department = request.form.get('sub_department', '').strip()
        rank = request.form.get('rank', '').strip()

        if not name:
            return jsonify({'error': 'Nom requis'}), 400

        if not department:
            return jsonify({'error': 'Bataillon / Unité requis'}), 400

        # Get uploaded photos
        photos = request.files.getlist('photos')

        if len(photos) < 3:
            return jsonify({'error': 'Minimum 3 photos requises'}), 400

        # Step 0: Check if subject already exists
        headers = {'x-api-key': COMPREFACE_API_KEY}
        check_url = f"{COMPREFACE_API_URL}/api/v1/recognition/subjects/{name}"

        check_response = requests.get(check_url, headers=headers)

        if check_response.status_code == 200:
            # Subject exists
            logger.warning(f"Attempt to add existing subject: {name}")
            return jsonify({
                'error': f'Le personnel "{name}" existe déjà dans le système.',
                'exists': True,
                'hint': 'Veuillez utiliser un nom différent ou supprimer l\'entrée existante depuis la liste ci-dessous.'
            }), 409  # 409 Conflict

        # Create metadata JSON
        metadata = {
            'department': department,
            'sub_department': sub_department,
            'rank': rank,
            'created_date': datetime.now().isoformat()
        }

        # Step 1: Add subject to CompreFace with metadata
        add_subject_url = f"{COMPREFACE_API_URL}/api/v1/recognition/subjects"

        subject_data = {
            'subject': name,
            'metadata': json.dumps(metadata)
        }

        response = requests.post(add_subject_url, headers=headers, json=subject_data)

        if response.status_code not in [200, 201]:
            # Parse error message
            try:
                error_data = response.json()
                error_msg = error_data.get('message', response.text)

                # Check for "already exists" error (code 43)
                if error_data.get('code') == 43 or 'already exists' in error_msg.lower():
                    return jsonify({
                        'error': f'Le personnel "{name}" existe déjà.',
                        'exists': True
                    }), 409
            except:
                pass

            logger.error(f"Failed to add subject: {response.text}")
            return jsonify({'error': f'Échec de l\'ajout du personnel: {response.text}'}), 500

        # Step 2: Upload photos
        upload_url = f"{COMPREFACE_API_URL}/api/v1/recognition/faces"
        upload_headers = {
            'x-api-key': COMPREFACE_API_KEY
        }

        uploaded_count = 0
        errors = []

        for i, photo in enumerate(photos):
            try:
                # Reset file pointer
                photo.seek(0)

                # Prepare multipart form data
                files = {'file': (photo.filename, photo, photo.content_type)}
                data = {'subject': name}

                upload_response = requests.post(
                    upload_url,
                    headers=upload_headers,
                    files=files,
                    data=data
                )

                if upload_response.status_code in [200, 201]:
                    uploaded_count += 1
                    logger.info(f"Uploaded photo {i+1}/{len(photos)} for {name}")
                else:
                    error_msg = f"Photo {i+1}: {upload_response.text}"
                    errors.append(error_msg)
                    logger.error(error_msg)

            except Exception as e:
                error_msg = f"Photo {i+1}: {str(e)}"
                errors.append(error_msg)
                logger.error(f"Error uploading photo {i+1}: {e}")

        if uploaded_count == 0:
            # Delete the subject if no photos were uploaded
            delete_url = f"{COMPREFACE_API_URL}/api/v1/recognition/subjects/{name}"
            requests.delete(delete_url, headers=headers)
            return jsonify({
                'error': 'Failed to upload any photos',
                'details': errors
            }), 500

        # Step 3: Save metadata to OUR database
        try:
            with DatabaseConnection() as conn:
                with conn.cursor() as cursor:
                    cursor.execute("""
                        INSERT INTO personnel_metadata (subject_name, department, sub_department, rank, created_date)
                        VALUES (%s, %s, %s, %s, CURRENT_TIMESTAMP)
                        ON CONFLICT (subject_name)
                        DO UPDATE SET
                            department = EXCLUDED.department,
                            sub_department = EXCLUDED.sub_department,
                            rank = EXCLUDED.rank,
                            updated_date = CURRENT_TIMESTAMP
                    """, (name, department, sub_department, rank))
                    conn.commit()
                    logger.info(f"Saved metadata for {name} to database")
        except Exception as db_error:
            logger.error(f"Failed to save metadata to database: {db_error}")
            # Don't fail the entire operation, metadata can be added later

        return jsonify({
            'success': True,
            'message': f'Personnel "{name}" added successfully',
            'uploaded_photos': uploaded_count,
            'total_photos': len(photos),
            'errors': errors if errors else None
        }), 201

    except Exception as e:
        logger.error(f"Error adding personnel: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/api/personnel/<subject>', methods=['PUT'])
def update_personnel(subject):
    """Update personnel metadata (department, sub_department, rank)"""
    try:
        data = request.get_json()

        department = data.get('department', '').strip()
        sub_department = data.get('sub_department', '').strip()
        rank = data.get('rank', '').strip()

        if not department:
            return jsonify({'error': 'Bataillon / Unité requis'}), 400

        # Verify subject exists in CompreFace
        headers = {'x-api-key': COMPREFACE_API_KEY}
        check_url = f"{COMPREFACE_API_URL}/api/v1/recognition/subjects/{subject}"
        check_response = requests.get(check_url, headers=headers)

        if check_response.status_code != 200:
            return jsonify({
                'error': f'Personnel "{subject}" not found in CompreFace'
            }), 404

        # Update metadata in our database
        with DatabaseConnection() as conn:
            with conn.cursor() as cursor:
                cursor.execute("""
                    UPDATE personnel_metadata
                    SET department = %s,
                        sub_department = %s,
                        rank = %s,
                        updated_date = CURRENT_TIMESTAMP
                    WHERE subject_name = %s
                """, (department, sub_department, rank, subject))

                if cursor.rowcount == 0:
                    # Subject exists in CompreFace but not in our DB, insert it
                    cursor.execute("""
                        INSERT INTO personnel_metadata (subject_name, department, sub_department, rank, created_date)
                        VALUES (%s, %s, %s, %s, CURRENT_TIMESTAMP)
                    """, (subject, department, sub_department, rank))

                conn.commit()
                logger.info(f"Updated metadata for {subject}: dept={department}, sub_dept={sub_department}, rank={rank}")

        return jsonify({
            'success': True,
            'message': f'Personnel "{subject}" updated successfully'
        }), 200

    except Exception as e:
        logger.error(f"Error updating personnel: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/api/personnel/<subject>', methods=['DELETE'])
def delete_personnel(subject):
    """Delete personnel from CompreFace and our metadata DB"""
    try:
        # Step 1: Delete from CompreFace
        headers = {'x-api-key': COMPREFACE_API_KEY}
        delete_url = f"{COMPREFACE_API_URL}/api/v1/recognition/subjects/{subject}"

        response = requests.delete(delete_url, headers=headers)

        if response.status_code in [200, 204]:
            logger.info(f"Deleted personnel from CompreFace: {subject}")

            # Step 2: Delete metadata from our database
            try:
                with DatabaseConnection() as conn:
                    with conn.cursor() as cursor:
                        cursor.execute("""
                            DELETE FROM personnel_metadata
                            WHERE subject_name = %s
                        """, (subject,))
                        conn.commit()
                        logger.info(f"Deleted metadata from database: {subject}")
            except Exception as db_error:
                logger.error(f"Failed to delete metadata from database: {db_error}")
                # Continue anyway, CompreFace deletion succeeded

            return jsonify({
                'success': True,
                'message': f'Personnel "{subject}" deleted successfully'
            })
        else:
            logger.error(f"Failed to delete personnel: {response.text}")
            return jsonify({
                'error': f'Failed to delete personnel: {response.text}'
            }), response.status_code

    except Exception as e:
        logger.error(f"Error deleting personnel: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/api/personnel/departments/config', methods=['GET'])
def get_department_config():
    """Get department/sub-department configuration for forms"""
    #  Structure organisationnelle 
    departments = [
        '1BCAS',    
        '10BPAG',   
        '11BPAG',   
        '12BPAG',   
        '13BIP',    
        '14BIP',    
        '15BIP',    
        'CITAP',    
        'VISITORS'  # Visiteurs
    ]

    # Sous-départements: Saisie manuelle (pas de cascade automatique)
    # Les sous-départements seront saisis manuellement selon l'organisation de chaque bataillon
    # Exemples: Compagnie 1, Compagnie 2, Section Commandement, etc.

    return jsonify({
        'departments': departments,
        'sub_departments': {}  # Vide: saisie manuelle
    })


# ============================================
# ERROR HANDLERS
# ============================================

@app.errorhandler(404)
def not_found(error):
    return jsonify({'error': 'Not found'}), 404


@app.errorhandler(500)
def internal_error(error):
    logger.error(f"Internal server error: {error}")
    return jsonify({'error': 'Internal server error'}), 500


# ============================================
# DATABASE MIGRATIONS
# ============================================

def run_migrations():
    """Run database migrations on startup"""
    try:
        migration_file = '/app/migrations/001_create_personnel_metadata.sql'

        # Check if file exists
        if not os.path.exists(migration_file):
            logger.warning(f"Migration file not found: {migration_file}")
            return

        with DatabaseConnection() as conn:
            with conn.cursor() as cursor:
                # Read and execute migration
                with open(migration_file, 'r') as f:
                    migration_sql = f.read()
                    cursor.execute(migration_sql)
                    conn.commit()
                    logger.info("Database migrations completed successfully")
    except Exception as e:
        logger.error(f"Migration failed: {e}")
        # Don't crash the app, just log the error


# ============================================
# APPLICATION STARTUP
# ============================================

if __name__ == '__main__':
    port = int(os.getenv('DASHBOARD_PORT', 5000))
    debug = os.getenv('FLASK_DEBUG', 'false').lower() == 'true'

    logger.info(f"Starting 1BIP Dashboard Service on port {port}")
    logger.info(f"Database: {DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}")

    # Run migrations
    logger.info("Running database migrations...")
    run_migrations()

    logger.info("Dashboard will be available at http://localhost:5000")

    app.run(
        host='0.0.0.0',
        port=port,
        debug=debug,
        threaded=True
    )
