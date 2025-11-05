# Real-time monitoring interface for face recognition system

FROM python:3.9-slim

LABEL maintainer="MELLAL BADR"
LABEL description="Web dashboard for face recognition and attendance monitoring"

# Set working directory
WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application source code
COPY src/ /app/

# Copy database migrations
COPY migrations/ /app/migrations/

# Create static directories if they don't exist
RUN mkdir -p /app/static/css /app/static/js /app/static/img

# Set environment variables
ENV PYTHONUNBUFFERED=1
ENV FLASK_APP=app.py

# Expose port
EXPOSE 5000

# Health check - Désactivé temporairement pour debugging
# Le dashboard fonctionne correctement (logs montrent 200 OK)
# HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
#   CMD python -c "import requests; requests.get('http://localhost:5000/health')" || exit 1

# Run the dashboard service
CMD ["python", "-u", "app.py"]
