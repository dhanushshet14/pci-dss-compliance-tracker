import json
import logging
from flask import Blueprint, request, jsonify
from services.shared import groq_client
from datetime import datetime, timezone

recommend_bp = Blueprint("recommend", __name__)
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


@recommend_bp.route("/recommend", methods=["POST"])
def recommend():
    data = request.get_json(silent=True)

    if not data or "input" not in data:
        return jsonify({
            "data": {
                "error": "Field 'input' is required",
                "timestamp": datetime.now(timezone.utc).isoformat()
            },
            "meta": build_error_meta()
        }), 400

    input_text = data["input"].strip()

    if len(input_text) < 10:
        return jsonify({
            "data": {
                "error": "Field 'input' must be at least 10 characters",
                "timestamp": datetime.now(timezone.utc).isoformat()
            },
            "meta": build_error_meta()
        }), 400

    prompt = f"""
You are a PCI-DSS compliance expert.

Give 3 actionable recommendations for this compliance issue:

{input_text}

Return JSON only:
{{
  "recommendations": [
    {{
      "action_type": "Preventive/Corrective/Detective",
      "description": "Action to perform",
      "priority": "High/Medium/Low"
    }}
  ],
  "generated_at": "{datetime.now(timezone.utc).isoformat()}"
}}
"""

    result = groq_client.call(prompt, temperature=0.3)

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