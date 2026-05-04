import logging
from flask import Flask, jsonify
from flask_cors import CORS

from routes.categorise import categorise_bp
from routes.describe import describe_bp
from routes.recommend import recommend_bp
from routes.generate_report import generate_report_bp
from routes.query import query_bp
from routes.analyse_document import analyse_document_bp
from services.shared import groq_client

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def create_app():
    app = Flask(__name__)
    CORS(app)

    app.register_blueprint(categorise_bp)
    app.register_blueprint(describe_bp)
    app.register_blueprint(recommend_bp)
    app.register_blueprint(generate_report_bp)
    app.register_blueprint(query_bp)
    app.register_blueprint(analyse_document_bp)

    @app.route("/", methods=["GET"])
    def home():
        return jsonify({
            "message": "PCI-DSS AI Service is running",
            "endpoints": [
                "/health",
                "/categorise",
                "/describe",
                "/recommend",
                "/generate-report",
                "/query",
                "/analyse-document"
            ]
        }), 200

    @app.route("/health", methods=["GET"])
    def health():
        return jsonify({
            "status": "UP",
            "service": "pci-dss-ai-service",
            "model": "llama-3.3-70b-versatile",
            "cache_stats": groq_client.get_cache_stats()
        }), 200

    @app.errorhandler(404)
    def not_found(error):
        return jsonify({
            "data": {
                "error": "Endpoint not found"
            },
            "meta": {
                "confidence": 0.0,
                "model_used": "llama-3.3-70b-versatile",
                "tokens_used": 0,
                "response_time_ms": 0,
                "cached": False
            }
        }), 404

    @app.errorhandler(500)
    def internal_error(error):
        logger.error(f"Internal server error: {str(error)}")
        return jsonify({
            "data": {
                "error": "Internal server error"
            },
            "meta": {
                "confidence": 0.0,
                "model_used": "llama-3.3-70b-versatile",
                "tokens_used": 0,
                "response_time_ms": 0,
                "cached": False
            }
        }), 500

    return app


app = create_app()

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)


