import json
import logging
from flask import Blueprint, request, jsonify
from services.shared import groq_client
from datetime import datetime, timezone

generate_report_bp = Blueprint("generate_report", __name__)
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


@generate_report_bp.route("/generate-report", methods=["POST"])
def generate_report():
    data = request.get_json(silent=True)

    if not data:
        return jsonify({
            "data": {
                "error": "Request body is required",
                "timestamp": datetime.now(timezone.utc).isoformat()
            },
            "meta": build_error_meta()
        }), 400

    input_text = json.dumps(data, indent=2)

    prompt = f"""
You are a PCI-DSS compliance expert.

Generate a professional compliance report based on this data:

{input_text}

Return JSON only:
{{
  "title": "PCI-DSS Compliance Report",
  "executive_summary": "Summary",
  "overview": "Overview",
  "top_items": ["item 1", "item 2", "item 3"],
  "recommendations": ["recommendation 1", "recommendation 2", "recommendation 3"],
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
