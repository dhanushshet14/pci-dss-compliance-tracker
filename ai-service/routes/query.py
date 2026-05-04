from flask import Blueprint, request, jsonify
from services.chroma_service import ChromaService
from services.groq_client import GroqClient

query_bp = Blueprint("query", __name__)

# ✅ Initialize services
chroma = ChromaService()
groq = GroqClient()

# ✅ ADD SEED DATA HERE (runs when route loads)
chroma.add_data("Cardholder data must be encrypted using strong cryptography", "1")
chroma.add_data("Use firewalls to protect cardholder data", "2")

from flask import Blueprint, request, jsonify, current_app
import time

query_bp = Blueprint("query", __name__)


@query_bp.route("/query", methods=["POST"])
def query():
    start = time.time()

    data = request.get_json()
    question = data.get("question")
    fresh = data.get("fresh", False)

    if not question:
        return jsonify({"error": "Question is required"}), 400

    # ✅ Get services from app config
    chroma = current_app.config["CHROMA"]
    cache = current_app.config["CACHE"]

    # 🔥 Cache key
    cache_key = question.lower()

    # ✅ Use cache (only if not fresh)
    if not fresh:
        cached = cache.get(cache_key)
        if cached:
            cache.hit()
            return jsonify({"answer": cached})

    # 🔍 Query DB
    docs = chroma.query(question)

    if not docs:
        answer = "No relevant data found"
    else:
        answer = docs[0]

    # ✅ Save to cache
    cache.set(cache_key, answer)
    cache.miss()

    # ⏱ Track response time
    duration = time.time() - start
    current_app.config["RESPONSE_TIMES"].append(duration)

    return jsonify({"answer": answer})