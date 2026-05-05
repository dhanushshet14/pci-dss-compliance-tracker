import json
import logging
from flask import Blueprint, request, jsonify
from services.shared import groq_client
from datetime import datetime, timezone

describe_bp = Blueprint("describe", __name__)
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


def validate_input(data):
    if not data:
        return None, "Request body is required"
    if "input" not in data:
        return None, "Field 'input' is required"
    if not isinstance(data["input"], str):
        return None, "Field 'input' must be a string"
    if len(data["input"].strip()) < 10:
        return None, "Field 'input' must be at least 10 characters"
    if len(data["input"].strip()) > 1000:
        return None, "Field 'input' must not exceed 1000 characters"
    return data["input"].strip(), None


def clean_response(text):
    text = text.strip()
    if text.startswith("```json"):
        text = text[7:]
    if text.startswith("```"):
        text = text[3:]
    if text.endswith("```"):
        text = text[:-3]
    return text.strip()


@describe_bp.route("/describe", methods=["POST"])
def describe():
    data = request.get_json(silent=True)
    input_text, error = validate_input(data)

    if error:
        return jsonify({
            "data": {
                "error": error,
                "timestamp": datetime.now(timezone.utc).isoformat()
            },
            "meta": build_error_meta()
        }), 400

    prompt = f"""
You are a PCI-DSS compliance expert.

Describe the following compliance item in clear professional language.

Input:
{input_text}

Return JSON only:
{{
  "description": "Clear explanation of the compliance item",
  "importance": "Why this matters for PCI-DSS compliance",
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
                "description": result.get("data", ""),
                "generated_at": datetime.now(timezone.utc).isoformat()
            },
            "meta": result.get("meta", build_error_meta())
        }), 200
