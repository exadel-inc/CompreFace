#!/usr/bin/env python3
"""
MELLAL Video Streaming Server
Serves MJPEG stream from camera service for dashboard viewing
Optimized for low latency and smooth playback
"""

from flask import Flask, Response, jsonify
import cv2
import logging
import time
import os

logger = logging.getLogger(__name__)

class StreamServer:
    """HTTP server for MJPEG video streaming"""

    def __init__(self, camera_service, port=5001):
        self.camera_service = camera_service
        self.port = port
        self.app = Flask(__name__)

        # Load streaming configuration from environment
        self.stream_width = int(os.getenv('STREAM_WIDTH', '1280'))
        self.stream_height = int(os.getenv('STREAM_HEIGHT', '720'))
        self.jpeg_quality = int(os.getenv('STREAM_JPEG_QUALITY', '60'))
        self.target_fps = int(os.getenv('STREAM_FPS', '25'))
        self.frame_delay = 1.0 / self.target_fps

        logger.info(f"Stream configured: {self.stream_width}x{self.stream_height} @ {self.target_fps}fps, quality={self.jpeg_quality}%")

        self.setup_routes()

    def setup_routes(self):
        """Setup Flask routes"""

        @self.app.route('/stream/video.mjpeg')
        def video_feed():
            """MJPEG video stream endpoint"""
            return Response(
                self.generate_mjpeg_stream(),
                mimetype='multipart/x-mixed-replace; boundary=frame'
            )

        @self.app.route('/stream/health')
        def health():
            """Health check endpoint"""
            return jsonify({
                'status': 'ok',
                'streaming': self.camera_service.latest_frame is not None,
                'frame_count': self.camera_service.frame_count
            })

        @self.app.route('/stream/snapshot.jpg')
        def snapshot():
            """Get latest frame as JPEG (optimized for web display)"""
            with self.camera_service.frame_lock:
                if self.camera_service.latest_frame is None:
                    return "No frame available", 503

                frame = self.camera_service.latest_frame.copy()

            # Resize for web display
            frame_resized = cv2.resize(frame, (self.stream_width, self.stream_height),
                                      interpolation=cv2.INTER_LINEAR)

            # Optimized JPEG encoding
            encode_params = [
                cv2.IMWRITE_JPEG_QUALITY, self.jpeg_quality,
                cv2.IMWRITE_JPEG_OPTIMIZE, 1
            ]
            ret, buffer = cv2.imencode('.jpg', frame_resized, encode_params)
            if not ret:
                return "Failed to encode frame", 500

            return Response(buffer.tobytes(), mimetype='image/jpeg')

    def generate_mjpeg_stream(self):
        """Generate MJPEG stream with optimized settings"""
        logger.info(f"New client connected to MJPEG stream ({self.stream_width}x{self.stream_height} @ {self.target_fps}fps)")

        while True:
            try:
                # Get latest frame
                with self.camera_service.frame_lock:
                    if self.camera_service.latest_frame is None:
                        time.sleep(0.05)
                        continue

                    frame = self.camera_service.latest_frame.copy()

                # Resize frame for streaming
                # Full resolution is still used for face recognition
                # This reduces bandwidth without affecting accuracy
                frame_resized = cv2.resize(frame, (self.stream_width, self.stream_height),
                                          interpolation=cv2.INTER_LINEAR)

                # Encode frame as JPEG with optimized settings
                encode_params = [
                    cv2.IMWRITE_JPEG_QUALITY, self.jpeg_quality,  # Configurable quality
                    cv2.IMWRITE_JPEG_OPTIMIZE, 1,                  # Optimize compression
                    cv2.IMWRITE_JPEG_PROGRESSIVE, 0                # Baseline JPEG (faster decode)
                ]
                ret, buffer = cv2.imencode('.jpg', frame_resized, encode_params)
                if not ret:
                    continue

                # Yield frame in MJPEG format
                yield (b'--frame\r\n'
                       b'Content-Type: image/jpeg\r\n\r\n' + buffer.tobytes() + b'\r\n')

                # Control frame rate
                time.sleep(self.frame_delay)

            except GeneratorExit:
                logger.info("Client disconnected from MJPEG stream")
                break
            except Exception as e:
                logger.error(f"Error in MJPEG stream: {e}")
                break

    def run(self):
        """Run the streaming server"""
        logger.info(f"Starting video streaming server on port {self.port}")
        self.app.run(host='0.0.0.0', port=self.port, threaded=True)
