import json
import logging
from flask import Blueprint, request, jsonify
from services.shared import groq_client
from datetime import datetime, timezone

categorise_bp = Blueprint("categorise", __name__)
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


@categorise_bp.route("/categorise", methods=["POST"])
def categorise():
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

    prompt = f"""
You are a PCI-DSS v4.0 compliance expert.

Categorise this item into exactly one category:
- Network Security
- Access Control
- Data Protection
- Vulnerability Management
- Monitoring and Logging
- Physical Security
- Incident Response
- Policy and Governance

Compliance Item:
{input_text}

Return JSON only:
{{
  "category": "One category",
  "confidence": 0.95,
  "reasoning": "Brief reason",
  "pci_dss_requirement": "Requirement number",
  "generated_at": "{datetime.now(timezone.utc).isoformat()}"
}}
"""

    result = groq_client.call(prompt, temperature=0.2)

    try:
        parsed = json.loads(clean_response(result["data"]))

        if "confidence" in parsed:
            parsed["confidence"] = float(parsed["confidence"])
            result["meta"]["confidence"] = parsed["confidence"]

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



