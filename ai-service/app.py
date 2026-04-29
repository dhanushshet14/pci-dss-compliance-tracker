import logging
import time
from flask import Flask
from routes.describe import describe_bp
from routes.recommend import recommend_bp
from routes.categorise import categorise_bp
from routes.report import report_bp
from routes.analyse import analyse_bp
from routes.batch import batch_bp

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)

logger = logging.getLogger(__name__)
START_TIME = time.time()
chroma_client = None

def init_chroma():
    global chroma_client
    try:
        from services.chroma_client import ChromaClient
        chroma_client = ChromaClient()
        count = chroma_client.load_documents("./docs")
        logger.info(f"RAG pipeline ready — {chroma_client.get_doc_count()} chunks in ChromaDB")
    except Exception as e:
        logger.error(f"ChromaDB startup error: {str(e)}")

app = Flask(__name__)

app.register_blueprint(describe_bp)
app.register_blueprint(recommend_bp)
app.register_blueprint(categorise_bp)
app.register_blueprint(report_bp)
app.register_blueprint(analyse_bp)
app.register_blueprint(batch_bp)

@app.route("/")
def home():
    return {
        "message": "PCI-DSS AI Service Running",
        "version": "1.0.0"
    }

@app.route("/health")
def health():
    uptime_seconds = int(time.time() - START_TIME)
    chroma_count = 0
    cache_stats = {}

    try:
        if chroma_client:
            chroma_count = chroma_client.get_doc_count()
    except Exception:
        pass

    try:
        from services.shared import groq_client
        cache_stats = groq_client.get_cache_stats()
    except Exception:
        pass

    return {
        "status": "ok",
        "service": "pci-dss-ai-service",
        "version": "1.0.0",
        "uptime_seconds": uptime_seconds,
        "chroma_doc_count": chroma_count,
        "model": "llama-3.3-70b-versatile",
        "cache_stats": cache_stats
    }

with app.app_context():
    init_chroma()

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)