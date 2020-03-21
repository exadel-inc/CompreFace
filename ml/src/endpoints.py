from flask.json import jsonify


def endpoints(app):
    @app.route('/status')
    def get_status():
        return jsonify(status="OK")
