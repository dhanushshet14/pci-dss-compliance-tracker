import json
import logging
import time
from datetime import datetime, timezone

from flask import Blueprint, request, jsonify
from services.shared import groq_client

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


def clean_response(text: str):
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
    start_time = time.time()

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

    # ✅ STRONG PROMPT (fixes your issue)
    prompt = f"""
You are a PCI-DSS v4.0 compliance expert.

Categorise the following compliance item into EXACTLY ONE of the categories below.

Categories:
- Network Security
- Access Control
- Data Protection
- Vulnerability Management
- Monitoring and Logging
- Physical Security
- Incident Response
- Policy and Governance

STRICT RULES:
- You MUST return valid JSON
- DO NOT return empty fields
- DO NOT skip any field
- DO NOT add extra text
- Always choose the BEST matching category

Compliance Item:
{input_text}

Return ONLY valid JSON in this format:
{{
  "category": "One category from the list",
  "confidence": 0.95,
  "reasoning": "Clear and short explanation",
  "pci_dss_requirement": "Example: Requirement 8.2",
  "generated_at": "{datetime.now(timezone.utc).isoformat()}"
}}
"""

    result = groq_client.call(prompt, temperature=0.2)

    # ⏱ response time
    duration = time.time() - start_time

    try:
        ai_text = result.get("data", "")
        parsed = json.loads(clean_response(ai_text))

        # ✅ normalize confidence
        confidence = float(parsed.get("confidence", 0.9))
        if confidence > 1:
            confidence = confidence / 100

        parsed["confidence"] = confidence

        # update meta
        meta = result.get("meta", build_error_meta())
        meta["confidence"] = confidence
        meta["response_time_ms"] = int(duration * 1000)

        return jsonify({
            "data": parsed,
            "meta": meta
        }), 200

    except Exception as e:
        logger.error(f"/categorise parse error: {str(e)}")

        return jsonify({
            "data": {
                "category": "Unknown",
                "confidence": 0.0,
                "reasoning": "Failed to parse AI response",
                "pci_dss_requirement": "Unknown",
                "generated_at": datetime.now(timezone.utc).isoformat()
            },
            "meta": build_error_meta()
        }), 200