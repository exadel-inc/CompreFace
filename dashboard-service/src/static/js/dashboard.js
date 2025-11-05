// 1BIP Dashboard JavaScript - Completely Offline (No External Dependencies)
// Pure Vanilla JavaScript - No jQuery, No External Libraries

// ==================== CONFIGURATION ====================
const CONFIG = {
    API_BASE_URL: '',  // Same origin
    REFRESH_INTERVAL: 30000,  // 30 seconds (auto-refresh interval)
    COUNTDOWN_INTERVAL: 1000, // 1 second
    VIDEO_STREAM_URL: '',  // Will be set dynamically based on current host
    IMAGES_PER_PAGE: 20,
};

// ==================== STATE ====================
let refreshTimer = null;
let countdownTimer = null;
let countdownSeconds = 10;
let currentTab = 'live';
let currentImagePage = 1;
let totalImagePages = 1;
let currentGalleryPage = 1;
let totalGalleryPages = 1;
let currentUnauthorizedPage = 1;
let totalUnauthorizedPages = 1;
let galleryFilters = {
    name: '',
    department: '',
    sub_department: '',
    status: ''
};

// ==================== INITIALIZATION ====================
document.addEventListener('DOMContentLoaded', function() {
    console.log('1BIP Dashboard initializing...');

    // Set video stream URL based on current host
    const currentHost = window.location.hostname;
    CONFIG.VIDEO_STREAM_URL = `http://${currentHost}:5001/stream/video.mjpeg`;

    // Initialize tabs
    initializeTabs();

    // Initialize date inputs with today's date
    initializeDateInputs();

    // Initialize video stream
    initializeVideoStream();

    // Start clock
    updateClock();
    setInterval(updateClock, 1000);

    // Load departments for filters
    loadDepartments();

    // Load initial data
    loadAllData();

    // Start auto-refresh if enabled
    startAutoRefresh();
});

// ==================== CLOCK ====================
function updateClock() {
    const now = new Date();
    const timeString = now.toLocaleTimeString('en-US', { hour12: false });
    document.getElementById('currentTime').textContent = timeString;
    document.getElementById('lastUpdate').textContent = now.toLocaleString();
}

// ==================== TAB MANAGEMENT ====================
function initializeTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const tabName = this.getAttribute('data-tab');
            switchTab(tabName);
        });
    });
}

function switchTab(tabName) {
    // Update active button
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');

    // Update active content
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    document.getElementById(`tab-${tabName}`).classList.add('active');

    currentTab = tabName;

    // Load tab-specific data
    loadTabData(tabName);
}

function loadTabData(tabName) {
    switch(tabName) {
        case 'live':
            refreshLiveMonitor();
            break;
        case 'attendance':
            refreshAttendance();
            break;
        case 'personnel':
            initPersonnelManagement();
            break;
        case 'unauthorized':
            refreshUnauthorized();
            break;
        case 'gallery':
            loadGalleryDepartments(); // Load department options for filters
            refreshGallery();
            break;
        case 'cameras':
            refreshCameraStatus();
            break;
        case 'reports':
            loadHourlyChart();
            loadReportDepartments();
            break;
    }
}

// ==================== DATA LOADING ====================
function loadAllData() {
    loadSummaryStats();
    loadTabData(currentTab);
}

async function loadSummaryStats() {
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/stats/summary`);
        if (!response.ok) throw new Error('Failed to fetch summary stats');

        const data = await response.json();

        document.getElementById('totalToday').textContent = data.total_today || 0;
        document.getElementById('authorizedToday').textContent = data.authorized_today || 0;
        document.getElementById('unauthorizedToday').textContent = data.unauthorized_today || 0;
        document.getElementById('uniqueEmployees').textContent = data.unique_employees || 0;
        document.getElementById('activeCameras').textContent = data.active_cameras || 0;

    } catch (error) {
        console.error('Error loading summary stats:', error);
        showError('Failed to load summary statistics');
    }
}

// ==================== LIVE MONITOR ====================
async function refreshLiveMonitor() {
    const tableBody = document.getElementById('liveAccessTable');
    tableBody.innerHTML = '<tr><td colspan="6" class="loading">Loading...</td></tr>';

    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/access/recent?limit=50`);
        if (!response.ok) throw new Error('Failed to fetch live access');

        const data = await response.json();

        if (data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" class="empty">No access records found</td></tr>';
            return;
        }

        tableBody.innerHTML = data.map(record => `
            <tr class="${record.is_authorized ? '' : 'unauthorized-row'}">
                <td>${formatTime(record.timestamp)}</td>
                <td>${escapeHtml(record.camera_name)}</td>
                <td>${escapeHtml(record.subject_name || 'Unknown')}</td>
                <td><span class="badge ${record.is_authorized ? 'authorized' : 'unauthorized'}">
                    ${record.is_authorized ? 'Authorized' : 'Unauthorized'}
                </span></td>
                <td>${record.similarity ? (record.similarity * 100).toFixed(1) + '%' : 'N/A'}</td>
                <td><span class="badge ${record.alert_sent ? 'alert-sent' : 'no-alert'}">
                    ${record.alert_sent ? 'Alert Sent' : 'No Alert'}
                </span></td>
            </tr>
        `).join('');

    } catch (error) {
        console.error('Error loading live monitor:', error);
        tableBody.innerHTML = '<tr><td colspan="6" class="empty">Error loading data</td></tr>';
    }
}

// ==================== ATTENDANCE ====================
async function refreshAttendance() {
    const tableBody = document.getElementById('attendanceTable');
    tableBody.innerHTML = '<tr><td colspan="6" class="loading">Loading...</td></tr>';

    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/attendance/today`);
        if (!response.ok) throw new Error('Failed to fetch attendance');

        const data = await response.json();

        if (data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" class="empty">No attendance records for today</td></tr>';
            return;
        }

        tableBody.innerHTML = data.map(record => `
            <tr>
                <td><strong>${escapeHtml(record.subject_name)}</strong></td>
                <td>${formatTime(record.first_entry)}</td>
                <td>${formatTime(record.last_entry)}</td>
                <td>${record.total_entries}</td>
                <td>${escapeHtml(record.camera_name)}</td>
                <td>${record.avg_similarity ? (record.avg_similarity * 100).toFixed(1) + '%' : 'N/A'}</td>
            </tr>
        `).join('');

    } catch (error) {
        console.error('Error loading attendance:', error);
        tableBody.innerHTML = '<tr><td colspan="6" class="empty">Error loading data</td></tr>';
    }
}

function exportAttendance() {
    // Export attendance as CSV
    fetch(`${CONFIG.API_BASE_URL}/api/attendance/today`)
        .then(response => response.json())
        .then(data => {
            const csv = convertToCSV(data, [
                'subject_name', 'first_entry', 'last_entry', 'total_entries', 'camera_name'
            ]);
            downloadCSV(csv, `attendance_${getCurrentDate()}.csv`);
        })
        .catch(error => {
            console.error('Error exporting attendance:', error);
            showError('Failed to export attendance');
        });
}

// ==================== UNAUTHORIZED ACCESS ====================
async function refreshUnauthorized() {
    const hours = document.getElementById('unauthorizedHours').value;
    const tableBody = document.getElementById('unauthorizedTable');
    const countDiv = document.getElementById('unauthorizedCount');

    tableBody.innerHTML = '<tr><td colspan="5" class="loading">Loading...</td></tr>';

    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/access/unauthorized?hours=${hours}`);
        if (!response.ok) throw new Error('Failed to fetch unauthorized access');

        const data = await response.json();

        countDiv.innerHTML = `⚠️ <strong>${data.length}</strong> unauthorized access attempt(s) in the last ${hours} hour(s)`;

        if (data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="5" class="empty">No unauthorized access attempts found</td></tr>';
            return;
        }

        tableBody.innerHTML = data.map(record => `
            <tr>
                <td>${formatTime(record.timestamp)}</td>
                <td>${escapeHtml(record.camera_name)}</td>
                <td>${escapeHtml(record.camera_location || 'N/A')}</td>
                <td>${escapeHtml(record.subject_name || 'Unknown Person')}</td>
                <td><span class="badge ${record.alert_sent ? 'alert-sent' : 'no-alert'}">
                    ${record.alert_sent ? 'Alert Sent' : 'No Alert'}
                </span></td>
            </tr>
        `).join('');

    } catch (error) {
        console.error('Error loading unauthorized access:', error);
        tableBody.innerHTML = '<tr><td colspan="5" class="empty">Error loading data</td></tr>';
    }
}

// ==================== CAPTURED IMAGES ====================
async function refreshCapturedImages(page = 1) {
    const imageGrid = document.getElementById('capturedImagesGrid');
    const imageCount = document.getElementById('imageCount');

    currentImagePage = page;
    imageGrid.innerHTML = '<div class="loading">Chargement des images capturées...</div>';

    try {
        const response = await fetch(
            `${CONFIG.API_BASE_URL}/api/images/latest?page=${page}&per_page=${CONFIG.IMAGES_PER_PAGE}`
        );
        if (!response.ok) throw new Error('Failed to fetch images');

        const data = await response.json();
        const images = data.images || [];
        const total = data.total || 0;
        totalImagePages = data.total_pages || 1;

        imageCount.textContent = `${total} image(s) d'accès non autorisé trouvée(s)`;

        if (images.length === 0) {
            imageGrid.innerHTML = '<div class="empty">Aucune image d\'accès non autorisé trouvée</div>';
            return;
        }

        // Build images grid
        let gridHTML = images.map(img => {
            const date = new Date(img.timestamp * 1000);
            const timeStr = date.toLocaleString('fr-FR', {
                day: '2-digit',
                month: '2-digit',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });

            return `
                <div class="image-card" onclick="viewFullImage('${img.url}', '${escapeHtml(img.filename)}')">
                    <div class="image-wrapper">
                        <img src="${img.url}" alt="${escapeHtml(img.filename)}" loading="lazy" />
                    </div>
                    <div class="image-info">
                        <div class="image-filename">${escapeHtml(img.filename)}</div>
                        <div class="image-timestamp">🕒 ${timeStr}</div>
                    </div>
                </div>
            `;
        }).join('');

        // Add pagination controls if needed
        if (totalImagePages > 1) {
            gridHTML += `
                <div class="pagination-controls">
                    <button class="btn btn-secondary" ${page <= 1 ? 'disabled' : ''}
                            onclick="refreshCapturedImages(${page - 1})">
                        ◀ Précédent
                    </button>
                    <span class="pagination-info">
                        Page ${page} sur ${totalImagePages}
                    </span>
                    <button class="btn btn-secondary" ${page >= totalImagePages ? 'disabled' : ''}
                            onclick="refreshCapturedImages(${page + 1})">
                        Suivant ▶
                    </button>
                </div>
            `;
        }

        imageGrid.innerHTML = gridHTML;

    } catch (error) {
        console.error('Error loading captured images:', error);
        imageGrid.innerHTML = '<div class="empty">Erreur lors du chargement des images</div>';
        imageCount.textContent = 'Erreur';
    }
}

function viewFullImage(url, filename) {
    // Create modal overlay
    const modal = document.createElement('div');
    modal.className = 'image-modal';
    modal.innerHTML = `
        <div class="image-modal-content">
            <div class="image-modal-header">
                <h3>📸 ${escapeHtml(filename)}</h3>
                <button class="image-modal-close" onclick="closeImageModal()">&times;</button>
            </div>
            <div class="image-modal-body">
                <img src="${url}" alt="${escapeHtml(filename)}" />
            </div>
            <div class="image-modal-footer">
                <button class="btn btn-secondary" onclick="closeImageModal()">Fermer</button>
                <a href="${url}" download="${filename}" class="btn btn-primary">📥 Télécharger</a>
            </div>
        </div>
    `;

    modal.onclick = function(e) {
        if (e.target === modal) {
            closeImageModal();
        }
    };

    document.body.appendChild(modal);
    document.body.style.overflow = 'hidden';
}

function closeImageModal() {
    const modal = document.querySelector('.image-modal');
    if (modal) {
        modal.remove();
        document.body.style.overflow = '';
    }
}

// ==================== VIDEO STREAM ====================
function initializeVideoStream() {
    const videoStream = document.getElementById('liveVideoStream');
    const videoPlaceholder = document.getElementById('videoPlaceholder');

    if (!videoStream) return;

    // Try to load the stream
    videoStream.src = CONFIG.VIDEO_STREAM_URL;

    videoStream.onload = function() {
        // Stream loaded successfully
        videoPlaceholder.style.display = 'none';
        videoStream.style.display = 'block';
        console.log('Video stream connected successfully');
    };

    videoStream.onerror = function() {
        // Stream failed to load - keep placeholder visible
        videoPlaceholder.style.display = 'flex';
        videoStream.style.display = 'none';
        console.log('Video stream not available');
    };
}

// ==================== CAMERA STATUS ====================
async function refreshCameraStatus() {
    const cameraGrid = document.getElementById('cameraGrid');
    cameraGrid.innerHTML = '<div class="loading">Loading...</div>';

    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/camera/status`);
        if (!response.ok) throw new Error('Failed to fetch camera status');

        const cameras = await response.json();

        if (cameras.length === 0) {
            cameraGrid.innerHTML = '<div class="empty">No cameras detected</div>';
            return;
        }

        cameraGrid.innerHTML = cameras.map(camera => `
            <div class="camera-card ${camera.status}">
                <div class="camera-header">
                    <div class="camera-name">📹 ${escapeHtml(camera.camera_name)}</div>
                    <span class="camera-status ${camera.status}">${camera.status.toUpperCase()}</span>
                </div>
                <div class="camera-info">
                    <div class="camera-info-item">
                        <span class="camera-info-label">Location:</span>
                        <span class="camera-info-value">${escapeHtml(camera.camera_location || 'N/A')}</span>
                    </div>
                    <div class="camera-info-item">
                        <span class="camera-info-label">Camera IP:</span>
                        <span class="camera-info-value">${camera.camera_ip || 'N/A'}</span>
                    </div>
                    <div class="camera-info-item">
                        <span class="camera-info-label">Last Activity:</span>
                        <span class="camera-info-value">${formatTime(camera.last_activity)}</span>
                    </div>
                    <div class="camera-info-item">
                        <span class="camera-info-label">Detections (1h):</span>
                        <span class="camera-info-value">${camera.detections_last_hour}</span>
                    </div>
                    <div class="camera-info-item">
                        <span class="camera-info-label">Unauthorized (1h):</span>
                        <span class="camera-info-value">${camera.unauthorized_last_hour}</span>
                    </div>
                    ${camera.status_reason ? `
                    <div class="camera-info-item" style="grid-column: 1 / -1; margin-top: 0.5rem; padding-top: 0.5rem; border-top: 1px solid rgba(255,255,255,0.1);">
                        <span class="camera-info-label">Status:</span>
                        <span class="camera-info-value" style="font-style: italic; opacity: 0.9;">${escapeHtml(camera.status_reason)}</span>
                    </div>
                    ` : ''}
                </div>
            </div>
        `).join('');

    } catch (error) {
        console.error('Error loading camera status:', error);
        cameraGrid.innerHTML = '<div class="empty">Error loading camera data</div>';
    }
}

// ==================== REPORTS ====================
function initializeDateInputs() {
    const today = getCurrentDate();
    document.getElementById('reportStartDate').value = today;
    document.getElementById('reportEndDate').value = today;
}

// Load departments for report filters
async function loadReportDepartments() {
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/departments`);
        const data = await response.json();

        const deptSelect = document.getElementById('reportFilterDepartment');
        if (deptSelect && data.departments) {
            deptSelect.innerHTML = '<option value="">Tous les bataillons</option>';
            data.departments.forEach(dept => {
                const option = document.createElement('option');
                option.value = dept;
                option.textContent = dept;
                deptSelect.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Error loading report departments:', error);
    }
}

// Reset report filters
function resetReportFilters() {
    document.getElementById('reportStartDate').value = '';
    document.getElementById('reportEndDate').value = '';
    document.getElementById('reportFilterName').value = '';
    document.getElementById('reportFilterDepartment').value = '';
    document.getElementById('reportFilterSubDepartment').value = '';
    document.getElementById('reportFilterStatus').value = '';

    // Hide summary
    document.getElementById('reportSummary').style.display = 'none';

    // Clear table
    document.getElementById('reportTable').innerHTML = '<tr><td colspan="8" class="empty">Sélectionnez les filtres et cliquez sur "Générer Rapport"</td></tr>';
}

async function generateReport() {
    const startDate = document.getElementById('reportStartDate').value;
    const endDate = document.getElementById('reportEndDate').value;
    const nameFilter = document.getElementById('reportFilterName').value.trim();
    const departmentFilter = document.getElementById('reportFilterDepartment').value;
    const subDepartmentFilter = document.getElementById('reportFilterSubDepartment').value.trim();
    const statusFilter = document.getElementById('reportFilterStatus').value;

    const tableBody = document.getElementById('reportTable');
    const summaryDiv = document.getElementById('reportSummary');

    if (!startDate || !endDate) {
        alert('Veuillez sélectionner les dates de début et de fin');
        return;
    }

    tableBody.innerHTML = '<tr><td colspan="8" class="loading">Génération du rapport...</td></tr>';
    summaryDiv.style.display = 'none';

    try {
        // Build query parameters
        const params = new URLSearchParams({
            start_date: startDate,
            end_date: endDate
        });

        if (nameFilter) params.append('name', nameFilter);
        if (departmentFilter) params.append('department', departmentFilter);
        if (subDepartmentFilter) params.append('sub_department', subDepartmentFilter);
        if (statusFilter) params.append('status', statusFilter);

        const response = await fetch(`${CONFIG.API_BASE_URL}/api/attendance/report?${params}`);
        if (!response.ok) throw new Error('Failed to generate report');

        const data = await response.json();

        if (data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="8" class="empty">Aucune donnée trouvée pour les critères sélectionnés</td></tr>';
            return;
        }

        // Calculate summary statistics
        const uniquePersons = new Set(data.map(r => r.subject_name)).size;
        let authorizedCount = 0;
        let unauthorizedCount = 0;

        // Display table
        tableBody.innerHTML = data.map(record => {
            const isAuthorized = record.is_authorized !== false;
            if (isAuthorized) authorizedCount++;
            else unauthorizedCount++;

            const statusBadge = isAuthorized ?
                '<span class="badge alert-sent">✅ Autorisé</span>' :
                '<span class="badge no-alert">❌ Non Autorisé</span>';

            return `
                <tr>
                    <td>${record.date}</td>
                    <td><strong>${escapeHtml(record.subject_name)}</strong></td>
                    <td>${escapeHtml(record.department || '-')}</td>
                    <td>${escapeHtml(record.sub_department || '-')}</td>
                    <td>${formatTime(record.first_entry)}</td>
                    <td>${formatTime(record.last_entry)}</td>
                    <td>${record.entries_count}</td>
                    <td><strong>${record.avg_similarity ? (record.avg_similarity * 100).toFixed(1) + '%' : '-'}</strong></td>
                    <td>${statusBadge}</td>
                </tr>
            `;
        }).join('');

        // Display summary
        document.getElementById('summaryTotal').textContent = data.length;
        document.getElementById('summaryUnique').textContent = uniquePersons;
        document.getElementById('summaryAuthorized').textContent = authorizedCount;
        document.getElementById('summaryUnauthorized').textContent = unauthorizedCount;
        summaryDiv.style.display = 'grid';

    } catch (error) {
        console.error('Error generating report:', error);
        tableBody.innerHTML = '<tr><td colspan="9" class="empty">Erreur lors de la génération du rapport</td></tr>';
    }
}

function exportReport() {
    const startDate = document.getElementById('reportStartDate').value;
    const endDate = document.getElementById('reportEndDate').value;
    const nameFilter = document.getElementById('reportFilterName').value.trim();
    const departmentFilter = document.getElementById('reportFilterDepartment').value;
    const subDepartmentFilter = document.getElementById('reportFilterSubDepartment').value.trim();
    const statusFilter = document.getElementById('reportFilterStatus').value;

    if (!startDate || !endDate) {
        alert('Veuillez sélectionner les dates de début et de fin');
        return;
    }

    // Build query parameters
    const params = new URLSearchParams({
        start_date: startDate,
        end_date: endDate
    });

    if (nameFilter) params.append('name', nameFilter);
    if (departmentFilter) params.append('department', departmentFilter);
    if (subDepartmentFilter) params.append('sub_department', subDepartmentFilter);
    if (statusFilter) params.append('status', statusFilter);

    fetch(`${CONFIG.API_BASE_URL}/api/attendance/report?${params}`)
        .then(response => response.json())
        .then(data => {
            const csv = convertToCSV(data, [
                'date', 'subject_name', 'department', 'sub_department',
                'first_entry', 'last_entry', 'entries_count', 'avg_similarity', 'is_authorized'
            ]);
            downloadCSV(csv, `rapport_1bip_${startDate}_to_${endDate}.csv`);
        })
        .catch(error => {
            console.error('Error exporting report:', error);
            alert('Erreur lors de l\'exportation du rapport');
        });
}

// ==================== HOURLY CHART (Simple Canvas Chart) ====================
async function loadHourlyChart() {
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/stats/hourly?hours=24`);
        if (!response.ok) throw new Error('Failed to fetch hourly stats');

        const data = await response.json();

        if (data.length === 0) {
            console.log('No hourly data available');
            return;
        }

        drawChart(data);

    } catch (error) {
        console.error('Error loading hourly chart:', error);
    }
}

function drawChart(data) {
    const canvas = document.getElementById('activityChart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const width = canvas.width;
    const height = canvas.height;

    // Clear canvas
    ctx.clearRect(0, 0, width, height);

    if (data.length === 0) {
        ctx.fillStyle = '#6b7280';
        ctx.font = '14px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText('No data available', width / 2, height / 2);
        return;
    }

    // Calculate max value for scaling
    const maxValue = Math.max(...data.map(d => d.total), 1);

    // Chart dimensions
    const padding = 40;
    const chartWidth = width - padding * 2;
    const chartHeight = height - padding * 2;
    const barWidth = chartWidth / data.length;

    // Draw bars
    data.forEach((item, index) => {
        const x = padding + index * barWidth;
        const barHeight = (item.authorized / maxValue) * chartHeight;
        const yAuthorized = height - padding - barHeight;

        // Authorized (green)
        ctx.fillStyle = '#10b981';
        ctx.fillRect(x + 2, yAuthorized, barWidth - 4, barHeight);

        // Unauthorized (red) - stacked on top
        const unauthorizedHeight = (item.unauthorized / maxValue) * chartHeight;
        const yUnauthorized = yAuthorized - unauthorizedHeight;

        ctx.fillStyle = '#ef4444';
        ctx.fillRect(x + 2, yUnauthorized, barWidth - 4, unauthorizedHeight);
    });

    // Draw axes
    ctx.strokeStyle = '#e5e7eb';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(padding, padding);
    ctx.lineTo(padding, height - padding);
    ctx.lineTo(width - padding, height - padding);
    ctx.stroke();

    // Labels
    ctx.fillStyle = '#6b7280';
    ctx.font = '12px sans-serif';
    ctx.textAlign = 'center';

    // X-axis labels (hours)
    data.forEach((item, index) => {
        const x = padding + index * barWidth + barWidth / 2;
        const y = height - padding + 20;
        const hour = new Date(item.hour).getHours();
        ctx.fillText(hour + 'h', x, y);
    });

    // Y-axis labels
    ctx.textAlign = 'right';
    for (let i = 0; i <= 4; i++) {
        const value = Math.round((maxValue / 4) * i);
        const y = height - padding - (chartHeight / 4) * i;
        ctx.fillText(value.toString(), padding - 10, y + 5);
    }

    // Legend
    ctx.fillStyle = '#10b981';
    ctx.fillRect(width - 150, 20, 20, 20);
    ctx.fillStyle = '#1f2937';
    ctx.textAlign = 'left';
    ctx.fillText('Authorized', width - 120, 35);

    ctx.fillStyle = '#ef4444';
    ctx.fillRect(width - 150, 50, 20, 20);
    ctx.fillStyle = '#1f2937';
    ctx.fillText('Unauthorized', width - 120, 65);
}

// ==================== AUTO-REFRESH ====================
function startAutoRefresh() {
    const checkbox = document.getElementById('autoRefresh');

    if (refreshTimer) {
        clearInterval(refreshTimer);
        refreshTimer = null;
    }

    if (countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
    }

    if (checkbox.checked) {
        // Start refresh timer
        refreshTimer = setInterval(() => {
            loadAllData();
            countdownSeconds = CONFIG.REFRESH_INTERVAL / 1000;
        }, CONFIG.REFRESH_INTERVAL);

        // Start countdown timer
        countdownSeconds = CONFIG.REFRESH_INTERVAL / 1000;
        countdownTimer = setInterval(() => {
            countdownSeconds--;
            document.getElementById('refreshCountdown').textContent = `(${countdownSeconds}s)`;

            if (countdownSeconds <= 0) {
                countdownSeconds = CONFIG.REFRESH_INTERVAL / 1000;
            }
        }, CONFIG.COUNTDOWN_INTERVAL);
    } else {
        document.getElementById('refreshCountdown').textContent = '';
    }

    checkbox.addEventListener('change', startAutoRefresh);
}

// ==================== UTILITY FUNCTIONS ====================
function formatTime(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
    });
}

function getCurrentDate() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showError(message) {
    alert('Error: ' + message);
}

function convertToCSV(data, fields) {
    if (!data || data.length === 0) return '';

    const header = fields.join(',');
    const rows = data.map(row => {
        return fields.map(field => {
            let value = row[field] || '';
            // Escape commas and quotes
            if (typeof value === 'string' && (value.includes(',') || value.includes('"'))) {
                value = '"' + value.replace(/"/g, '""') + '"';
            }
            return value;
        }).join(',');
    });

    return [header, ...rows].join('\n');
}

function downloadCSV(csv, filename) {
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
}

// ==================== GALLERY FUNCTIONS ====================
async function loadDepartments() {
    //Load departments and sub-departments for filter dropdowns
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/departments`);
        if (!response.ok) throw new Error('Failed to fetch departments');

        const data = await response.json();

        // Populate department dropdown
        const deptSelect = document.getElementById('filterDepartment');
        if (deptSelect) {
            data.departments.forEach(dept => {
                const option = document.createElement('option');
                option.value = dept;
                option.textContent = dept;
                deptSelect.appendChild(option);
            });
        }

        // Store sub_departments for later use
        window.subDepartmentsData = data.sub_departments;

    } catch (error) {
        console.error('Error loading departments:', error);
    }
}

async function loadGalleryDepartments() {
    //Load departments from database to populate gallery filter dropdowns
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/departments`);
        const data = await response.json();

        // Populate department dropdown
        const deptSelect = document.getElementById('filterDepartment');
        if (deptSelect && data.departments) {
            deptSelect.innerHTML = '<option value="">Tous les bataillons</option>';
            data.departments.forEach(dept => {
                const option = document.createElement('option');
                option.value = dept;
                option.textContent = dept;
                deptSelect.appendChild(option);
            });
        }

        // Store sub-departments data globally for cascade
        if (data.sub_departments) {
            window.subDepartmentsData = data.sub_departments;
        }

        console.log('Gallery filter departments loaded:', data.departments);
    } catch (error) {
        console.error('Error loading gallery departments:', error);
    }
}

async function refreshGallery(page = 1) {
    //Load gallery images with current filters
    const imageGrid = document.getElementById('galleryImagesGrid');
    const galleryCount = document.getElementById('galleryCount');

    currentGalleryPage = page;
    imageGrid.innerHTML = '<div class="loading">Chargement de la galerie...</div>';

    try {
        // Build query parameters
        const params = new URLSearchParams({
            page: page,
            per_page: CONFIG.IMAGES_PER_PAGE,
            name: galleryFilters.name,
            department: galleryFilters.department,
            sub_department: galleryFilters.sub_department,
            status: galleryFilters.status
        });

        const response = await fetch(`${CONFIG.API_BASE_URL}/api/images/gallery?${params}`);
        if (!response.ok) throw new Error('Failed to fetch gallery');

        const data = await response.json();
        const images = data.images || [];
        const total = data.total || 0;
        totalGalleryPages = data.total_pages || 1;

        galleryCount.textContent = `${total} image(s) trouvée(s)`;

        if (images.length === 0) {
            imageGrid.innerHTML = '<div class="empty">Aucune image trouvée avec ces filtres</div>';
            return;
        }

        // Build images grid
        let gridHTML = images.map(img => {
            const date = new Date(img.timestamp);
            const timeStr = date.toLocaleString('fr-FR', {
                day: '2-digit',
                month: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
            });

            const statusClass = img.is_authorized ? 'authorized' : 'unauthorized';
            const statusIcon = img.is_authorized ? '✅' : '❌';
            const statusLabel = img.is_authorized ? 'Autorisé' : 'Non Autorisé';

            // Format confidence score
            const confidenceScore = img.similarity ? (img.similarity * 100).toFixed(1) + '%' : 'N/A';
            const confidenceClass = img.similarity >= 0.88 ? 'high-confidence' : (img.similarity >= 0.50 ? 'medium-confidence' : 'low-confidence');

            return `
                <div class="image-card ${statusClass}" onclick="viewFullImage('${img.url}', '${escapeHtml(img.filename)}')">
                    <div class="image-wrapper">
                        <img src="${img.url}" alt="${escapeHtml(img.subject_name)}" loading="lazy" />
                        <div class="image-status-badge ${statusClass}">${statusIcon} ${statusLabel}</div>
                        ${img.similarity ? `<div class="image-confidence-badge ${confidenceClass}">🎯 ${confidenceScore}</div>` : ''}
                    </div>
                    <div class="image-info">
                        <div class="image-subject">${statusIcon} ${escapeHtml(img.subject_name)}</div>
                        <div class="image-department">${escapeHtml(img.department || 'N/A')}</div>
                        <div class="image-timestamp">🕒 ${timeStr}</div>
                        ${img.similarity ? `<div class="image-confidence">Confiance: <strong>${confidenceScore}</strong></div>` : ''}
                    </div>
                </div>
            `;
        }).join('');

        // Add pagination controls if needed
        if (totalGalleryPages > 1) {
            gridHTML += `
                <div class="pagination-controls">
                    <button class="btn btn-secondary" ${page <= 1 ? 'disabled' : ''}
                            onclick="refreshGallery(${page - 1})">
                        ◀ Précédent
                    </button>
                    <span class="pagination-info">
                        Page ${page} sur ${totalGalleryPages}
                    </span>
                    <button class="btn btn-secondary" ${page >= totalGalleryPages ? 'disabled' : ''}
                            onclick="refreshGallery(${page + 1})">
                        Suivant ▶
                    </button>
                </div>
            `;
        }

        imageGrid.innerHTML = gridHTML;

    } catch (error) {
        console.error('Error loading gallery:', error);
        imageGrid.innerHTML = '<div class="empty">Erreur lors du chargement de la galerie</div>';
        galleryCount.textContent = 'Erreur';
    }
}

function applyGalleryFilters() {
    //Apply filters and refresh gallery
    // Get filter values
    galleryFilters.name = document.getElementById('filterName').value.trim();
    galleryFilters.department = document.getElementById('filterDepartment').value;
    galleryFilters.sub_department = document.getElementById('filterSubDepartment').value;
    galleryFilters.status = document.getElementById('filterStatus').value;

    // Reset to page 1 and refresh
    refreshGallery(1);
}

function resetGalleryFilters() {
    //Reset all filters
    document.getElementById('filterName').value = '';
    document.getElementById('filterDepartment').value = '';
    document.getElementById('filterSubDepartment').value = '';
    document.getElementById('filterStatus').value = '';

    galleryFilters = {
        name: '',
        department: '',
        sub_department: '',
        status: ''
    };

    refreshGallery(1);
}

// Update sub-department dropdown when department changes
document.addEventListener('DOMContentLoaded', function() {
    const deptSelect = document.getElementById('filterDepartment');
    const subDeptSelect = document.getElementById('filterSubDepartment');

    if (deptSelect && subDeptSelect) {
        deptSelect.addEventListener('change', function() {
            const selectedDept = this.value;

            // Clear sub-department dropdown
            subDeptSelect.innerHTML = '<option value="">Toutes les compagnies</option>';

            // Populate with matching sub-departments
            if (selectedDept && window.subDepartmentsData && window.subDepartmentsData[selectedDept]) {
                window.subDepartmentsData[selectedDept].forEach(subDept => {
                    const option = document.createElement('option');
                    option.value = subDept;
                    option.textContent = subDept;
                    subDeptSelect.appendChild(option);
                });
            }
        });
    }
});

// ==================== UNAUTHORIZED TABLE PAGINATION ====================
async function refreshUnauthorized(page = 1) {
    //Refresh unauthorized access table with pagination
    const hours = document.getElementById('unauthorizedHours').value;
    const tableBody = document.getElementById('unauthorizedTable');
    const countDiv = document.getElementById('unauthorizedCount');
    const pagination = document.getElementById('unauthorizedPagination');
    const pageInfo = document.getElementById('unauthorizedPageInfo');

    currentUnauthorizedPage = page;
    tableBody.innerHTML = '<tr><td colspan="5" class="loading">Chargement...</td></tr>';

    try {
        const response = await fetch(
            `${CONFIG.API_BASE_URL}/api/access/unauthorized_paginated?hours=${hours}&page=${page}&per_page=20`
        );
        if (!response.ok) throw new Error('Failed to fetch unauthorized access');

        const data = await response.json();
        const records = data.records || [];
        const total = data.total || 0;
        totalUnauthorizedPages = data.total_pages || 1;

        countDiv.innerHTML = `⚠️ <strong>${total}</strong> tentative(s) d'accès non autorisé dans les dernières ${hours} heure(s)`;

        if (records.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="5" class="empty">Aucune tentative d\'accès non autorisé trouvée</td></tr>';
            pagination.style.display = 'none';
            return;
        }

        tableBody.innerHTML = records.map(record => `
            <tr>
                <td>${formatTime(record.timestamp)}</td>
                <td>${escapeHtml(record.camera_name)}</td>
                <td>${escapeHtml(record.camera_location || 'N/A')}</td>
                <td>${escapeHtml(record.subject_name || 'Unknown Person')}</td>
                <td><span class="badge ${record.alert_sent ? 'alert-sent' : 'no-alert'}">
                    ${record.alert_sent ? 'Alerte Envoyée' : 'Pas d\'Alerte'}
                </span></td>
            </tr>
        `).join('');

        // Show/hide pagination
        if (totalUnauthorizedPages > 1) {
            pagination.style.display = 'flex';
            pageInfo.textContent = `Page ${page} sur ${totalUnauthorizedPages}`;
        } else {
            pagination.style.display = 'none';
        }

    } catch (error) {
        console.error('Error loading unauthorized access:', error);
        tableBody.innerHTML = '<tr><td colspan="5" class="empty">Erreur lors du chargement</td></tr>';
        pagination.style.display = 'none';
    }
}

function changeUnauthorizedPage(direction) {
    //Navigate unauthorized table pagination
    const newPage = currentUnauthorizedPage + direction;
    if (newPage >= 1 && newPage <= totalUnauthorizedPages) {
        refreshUnauthorized(newPage);
    }
}

// ==================== PERSONNEL MANAGEMENT ====================

// Global flags to prevent multiple event listener attachments and submissions
let personnelFormInitialized = false;
let isSubmittingPersonnel = false;
let isEditMode = false;
let editingSubject = null;

// Department/Sub-department configuration
// Note: Departments are now hardcoded in HTML for 1BIP military structure
// Sub-departments are manually entered (no cascade)

async function loadDepartmentConfig() {
    // Departments are hardcoded in the HTML form
    // Sub-department is now a text input (manual entry)
    // No dynamic loading or cascade needed
    console.log('Department configuration: Using 1BIP military structure (hardcoded)');
}

// Handle photo file selection and preview
function setupPhotoPreview() {
    const photoInput = document.getElementById('personnelPhotos');
    const previewContainer = document.getElementById('photoPreview');

    if (photoInput && previewContainer) {
        photoInput.addEventListener('change', function(e) {
            previewContainer.innerHTML = '';

            const files = Array.from(e.target.files);
            if (files.length < 3) {
                previewContainer.innerHTML = `
                    <div class="photo-warning">
                        ⚠️ Veuillez sélectionner au moins 3 photos (${files.length}/3 sélectionnées)
                    </div>
                `;
                return;
            }

            files.forEach((file, index) => {
                const reader = new FileReader();
                reader.onload = function(e) {
                    const preview = document.createElement('div');
                    preview.className = 'photo-preview-item';
                    preview.innerHTML = `
                        <img src="${e.target.result}" alt="Photo ${index + 1}">
                        <span class="photo-number">${index + 1}</span>
                    `;
                    previewContainer.appendChild(preview);
                };
                reader.readAsDataURL(file);
            });
        });
    }
}

// Handle personnel form submission
async function setupPersonnelForm() {
    // Prevent multiple event listener attachments
    if (personnelFormInitialized) {
        console.log('Personnel form already initialized, skipping duplicate setup');
        return;
    }

    const form = document.getElementById('addPersonnelForm');
    const messageDiv = document.getElementById('formMessage');

    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();

            // Prevent multiple simultaneous submissions (using global flag)
            if (isSubmittingPersonnel) {
                console.log('Submission already in progress, ignoring duplicate request');
                return;
            }

            // Check if we're in edit mode
            if (isEditMode) {
                // EDIT MODE - Update existing personnel metadata
                isSubmittingPersonnel = true;
                const submitBtn = form.querySelector('button[type="submit"]');
                const originalText = submitBtn.textContent;
                submitBtn.disabled = true;
                submitBtn.textContent = '⏳ Mise à jour...';

                try {
                    const updateData = {
                        department: document.getElementById('personnelDepartment').value,
                        sub_department: document.getElementById('personnelSubDepartment').value,
                        rank: document.getElementById('personnelRank').value
                    };

                    const response = await fetch(`${CONFIG.API_BASE_URL}/api/personnel/${encodeURIComponent(editingSubject)}`, {
                        method: 'PUT',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(updateData)
                    });

                    const result = await response.json();

                    if (response.ok && result.success) {
                        showFormMessage(`✅ ${result.message}`, 'success');

                        // Exit edit mode
                        cancelEdit();

                        // Refresh personnel list
                        setTimeout(() => {
                            refreshPersonnelList();
                        }, 1000);
                    } else {
                        showFormMessage(`❌ Erreur: ${result.error}`, 'error');
                    }

                } catch (error) {
                    console.error('Error updating personnel:', error);
                    showFormMessage('❌ Erreur lors de la mise à jour du personnel', 'error');
                } finally {
                    isSubmittingPersonnel = false;
                    submitBtn.disabled = false;
                    submitBtn.textContent = originalText;
                }

            } else {
                // ADD MODE - Add new personnel with photos
                // Validate photos
                const photoInput = document.getElementById('personnelPhotos');
                if (photoInput.files.length < 3) {
                    showFormMessage('Veuillez sélectionner au moins 3 photos du visage', 'error');
                    return;
                }

                // Set submission flag and show loading state
                isSubmittingPersonnel = true;
                const submitBtn = form.querySelector('button[type="submit"]');
                const originalText = submitBtn.textContent;
                submitBtn.disabled = true;
                submitBtn.textContent = '⏳ Ajout en cours...';

                try {
                    // Prepare form data
                    const formData = new FormData(form);

                    // Submit to API
                    const response = await fetch(`${CONFIG.API_BASE_URL}/api/personnel`, {
                        method: 'POST',
                        body: formData
                    });

                    const result = await response.json();

                    if (response.ok && result.success) {
                        showFormMessage(
                            `✅ ${result.message} (${result.uploaded_photos}/${result.total_photos} photos téléchargées)`,
                            'success'
                        );

                        // Reset form
                        form.reset();
                        document.getElementById('photoPreview').innerHTML = '';

                        // Refresh personnel list
                        setTimeout(() => {
                            refreshPersonnelList();
                        }, 1000);
                    } else if (response.status === 409) {
                        // Subject already exists
                        showFormMessage(
                            `❌ ${result.error}\n💡 Conseil: Vérifiez la liste du personnel ci-dessous ou utilisez un nom différent.`,
                            'error'
                        );
                    } else {
                        showFormMessage(`❌ Erreur: ${result.error}`, 'error');
                    }

                } catch (error) {
                    console.error('Error adding personnel:', error);
                    showFormMessage('❌ Erreur lors de l\'ajout du personnel', 'error');
                } finally {
                    // Reset submission flag and button state
                    isSubmittingPersonnel = false;
                    submitBtn.disabled = false;
                    submitBtn.textContent = originalText;
                }
            }
        });

        // Mark as initialized to prevent duplicate event listeners
        personnelFormInitialized = true;
        console.log('Personnel form initialized successfully');
    }
}

function showFormMessage(message, type) {
    //Show form message (success or error)
    const messageDiv = document.getElementById('formMessage');
    if (!messageDiv) return;

    messageDiv.textContent = message;
    messageDiv.className = `form-message ${type}`;
    messageDiv.style.display = 'block';

    // Auto-hide after 5 seconds
    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 5000);
}

// Edit personnel - populate form with existing data
function editPersonnel(subject, department, subDepartment, rank) {
    // Switch to personnel tab if not already there
    const personnelTab = document.querySelector('.tab-btn[data-tab="personnel"]');
    if (personnelTab) {
        personnelTab.click();
    }

    // Scroll to form
    setTimeout(() => {
        const form = document.getElementById('addPersonnelForm');
        if (!form) return;

        // Set edit mode
        isEditMode = true;
        editingSubject = subject;

        // Populate form fields
        document.getElementById('personnelName').value = subject;
        document.getElementById('personnelName').readOnly = true; // Name cannot be changed
        document.getElementById('personnelDepartment').value = department;
        document.getElementById('personnelSubDepartment').value = subDepartment;
        document.getElementById('personnelRank').value = rank;

        // Hide photo upload (not needed for edit)
        const photoGroup = document.getElementById('personnelPhotos').closest('.form-group');
        if (photoGroup) {
            photoGroup.style.display = 'none';
        }

        // Change submit button text
        const submitBtn = form.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.textContent = '✏️ Mettre à Jour Personnel';
            submitBtn.className = 'btn btn-primary';
        }

        // Add cancel button if not exists
        let cancelBtn = form.querySelector('.btn-cancel-edit');
        if (!cancelBtn) {
            cancelBtn = document.createElement('button');
            cancelBtn.type = 'button';
            cancelBtn.className = 'btn btn-secondary btn-cancel-edit';
            cancelBtn.textContent = '❌ Annuler';
            cancelBtn.onclick = cancelEdit;
            submitBtn.parentNode.insertBefore(cancelBtn, submitBtn.nextSibling);
        }

        // Show message
        showFormMessage(`✏️ Mode édition: Modification de "${subject}"`, 'info');

        // Scroll to form
        form.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 100);
}

// Cancel edit mode
function cancelEdit() {
    const form = document.getElementById('addPersonnelForm');
    if (!form) return;

    // Reset edit mode
    isEditMode = false;
    editingSubject = null;

    // Reset form
    form.reset();
    document.getElementById('personnelName').readOnly = false;

    // Show photo upload again
    const photoGroup = document.getElementById('personnelPhotos').closest('.form-group');
    if (photoGroup) {
        photoGroup.style.display = 'block';
    }

    // Reset submit button
    const submitBtn = form.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.textContent = '✅ Ajouter Personnel';
    }

    // Remove cancel button
    const cancelBtn = form.querySelector('.btn-cancel-edit');
    if (cancelBtn) {
        cancelBtn.remove();
    }

    // Hide message
    document.getElementById('formMessage').style.display = 'none';
}

// Load personnel list
async function refreshPersonnelList() {
    //Refresh the personnel list table
    const tableBody = document.getElementById('personnelListTable');
    if (!tableBody) return;

    try {
        tableBody.innerHTML = '<tr><td colspan="7" class="loading">Chargement...</td></tr>';

        const response = await fetch(`${CONFIG.API_BASE_URL}/api/personnel`);
        const data = await response.json();

        if (!data.personnel || data.personnel.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="7" class="empty">Aucun personnel enregistré</td></tr>';
            return;
        }

        // Sort by name
        data.personnel.sort((a, b) => a.name.localeCompare(b.name));

        tableBody.innerHTML = data.personnel.map(person => `
            <tr>
                <td>
                    <div class="personnel-photo-placeholder">
                        👤
                    </div>
                </td>
                <td><strong>${escapeHtml(person.name)}</strong></td>
                <td>${escapeHtml(person.rank || '-')}</td>
                <td>${escapeHtml(person.department || '-')}</td>
                <td>${escapeHtml(person.sub_department || '-')}</td>
                <td>${person.created_date ? new Date(person.created_date).toLocaleDateString('fr-FR') : '-'}</td>
                <td>
                    <button class="btn btn-primary btn-sm" onclick="editPersonnel('${escapeHtml(person.subject)}', '${escapeHtml(person.department || '')}', '${escapeHtml(person.sub_department || '')}', '${escapeHtml(person.rank || '')}')">
                        ✏️ Modifier
                    </button>
                    <button class="btn btn-danger btn-sm" onclick="deletePersonnel('${escapeHtml(person.subject)}')">
                        🗑️ Supprimer
                    </button>
                </td>
            </tr>
        `).join('');

    } catch (error) {
        console.error('Error loading personnel list:', error);
        tableBody.innerHTML = '<tr><td colspan="7" class="empty">Erreur lors du chargement</td></tr>';
    }
}

// Filter personnel list (client-side)
function filterPersonnelList() {
    //Filter personnel list based on search input
    const searchInput = document.getElementById('searchPersonnel');
    if (!searchInput) return;

    const searchTerm = searchInput.value.toLowerCase().trim();
    const tableBody = document.getElementById('personnelListTable');
    const rows = tableBody.querySelectorAll('tr');

    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        if (text.includes(searchTerm)) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// Delete personnel
async function deletePersonnel(subject) {
    //Delete a personnel record
    if (!confirm(`Êtes-vous sûr de vouloir supprimer "${subject}" ?`)) {
        return;
    }

    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/personnel/${encodeURIComponent(subject)}`, {
            method: 'DELETE'
        });

        const result = await response.json();

        if (response.ok && result.success) {
            alert(`✅ ${result.message}`);
            refreshPersonnelList();
        } else {
            alert(`❌ Erreur: ${result.error}`);
        }

    } catch (error) {
        console.error('Error deleting personnel:', error);
        alert('❌ Erreur lors de la suppression');
    }
}

// Initialize personnel management when personnel tab is clicked
function initPersonnelManagement() {
    //Initialize personnel management features
    loadDepartmentConfig();
    setupPhotoPreview();
    setupPersonnelForm();
    refreshPersonnelList();
}

// ==================== CONSOLE INFO ====================
console.log('1BIP Dashboard loaded successfully');
console.log('Auto-refresh interval:', CONFIG.REFRESH_INTERVAL / 1000, 'seconds');
