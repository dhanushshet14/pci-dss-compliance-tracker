import json
import logging
from flask import Blueprint, request, jsonify
from services.shared import groq_client
from datetime import datetime, timezone

analyse_document_bp = Blueprint("analyse_document", __name__)
logger = logging.getLogger(__name__)

MODEL_NAME = "llama-3.3-70b-versatile"


def build_error_meta():
    return {
        "confidence": 0.0,
        "model_used": MODEL_NAME,
        "tokens_used": 0,
        "response_time_ms": 0,
        "cached": False
    }


def clean_response(text):
    text = text.strip()
    if text.startswith("```json"):
        text = text[7:]
    if text.startswith("```"):
        text = text[3:]
    if text.endswith("```"):
        text = text[:-3]
    return text.strip()


@analyse_document_bp.route("/analyse-document", methods=["POST"])
def analyse_document():
    data = request.get_json(silent=True)

    if not data or "text" not in data:
        return jsonify({
            "data": {
                "error": "Field 'text' is required",
                "timestamp": datetime.now(timezone.utc).isoformat()
            },
            "meta": build_error_meta()
        }), 400

    text = data["text"].strip()

    if len(text) < 20:
        return jsonify({
            "data": {
                "error": "Field 'text' must be at least 20 characters",
                "timestamp": datetime.now(timezone.utc).isoformat()
            },
            "meta": build_error_meta()
        }), 400

    prompt = f"""
You are a PCI-DSS compliance expert.

Analyse the following document text. Identify key insights and risks.

Document:
{text}

Return JSON only:
{{
  "findings": [
    {{
      "insight": "Key insight",
      "risk": "Compliance risk",
      "severity": "High/Medium/Low",
      "recommendation": "Recommended fix"
    }}
  ],
  "generated_at": "{datetime.now(timezone.utc).isoformat()}"
}}
"""

    result = groq_client.call(prompt, temperature=0.3, max_tokens=1500)

    try:
        parsed = json.loads(clean_response(result["data"]))
        return jsonify({
            "data": parsed,
            "meta": result["meta"]
        }), 200
    except Exception:
        return jsonify({
            "data": {
                "raw_response": result.get("data", ""),
                "generated_at": datetime.now(timezone.utc).isoformat()
            },
            "meta": result.get("meta", build_error_meta())
        }), 200

