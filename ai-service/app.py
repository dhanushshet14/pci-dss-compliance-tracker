# ============================================================================
# PCI-DSS Compliance Tracker — AI Service
# ============================================================================

import time
from flask import Flask, jsonify

from routes.query import query_bp
from services.chroma_service import ChromaService
from services.cache_service import CacheService

app = Flask(__name__)

# Initialize services
chroma = ChromaService()
cache = CacheService()

# Store shared objects
app.config["CHROMA"] = chroma
app.config["CACHE"] = cache
app.config["DOCS"] = chroma.data
app.config["RESPONSE_TIMES"] = []
app.config["START_TIME"] = time.time()

# Register routes
app.register_blueprint(query_bp)


@app.route("/")
def home():
    return jsonify({
        "message": "PCI DSS AI Service is running"
    })


@app.route("/health")
def health():
    times = app.config["RESPONSE_TIMES"]
    last_10 = times[-10:]

    avg_time = sum(last_10) / len(last_10) if last_10 else 0

    return jsonify({
        "status": "ok",
        "model": "simple-keyword-model",
        "doc_count": len(app.config["DOCS"]),
        "avg_response_time_last_10": round(avg_time, 4),
        "uptime_seconds": round(time.time() - app.config["START_TIME"], 2),
        "cache": app.config["CACHE"].stats()
    })


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)