import json
import logging
from flask import Blueprint, request, jsonify
from services.groq_client import GroqClient
from datetime import datetime, timezone

categorise_bp = Blueprint("categorise", __name__)
groq_client = GroqClient()
logger = logging.getLogger(__name__)

def load_prompt(input_text: str) -> str:
    return f"""You are a PCI-DSS v4.0 compliance expert.

Categorise the following PCI-DSS compliance item into exactly one of these categories:
- Network Security
- Access Control
- Data Protection
- Vulnerability Management
- Monitoring and Logging
- Physical Security
- Incident Response
- Policy and Governance

Compliance Item: {input_text}

Respond ONLY in this exact JSON format with no extra text, no markdown, no code fences:
{{
    "category": "One of the 8 categories above",
    "confidence": 0.95,
    "reasoning": "Brief explanation of why this category was chosen",
    "pci_dss_requirement": "Most relevant requirement number e.g. Requirement 1.2",
    "generated_at": "{datetime.now(timezone.utc).isoformat()}"
}}"""

def clean_and_parse(result: str):
    result = result.replace("{{", "{").replace("}}", "}")
    result = result.strip()
    if result.startswith("```json"):
        result = result[7:]
    if result.startswith("```"):
        result = result[3:]
    if result.endswith("```"):
        result = result[:-3]
    return result.strip()

def validate_input(data):
    if not data:
        return None, "Request body is required"
    if "input" not in data:
        return None, "Field 'input' is required"
    if not isinstance(data["input"], str):
        return None, "Field 'input' must be a string"
    if len(data["input"].strip()) == 0:
        return None, "Field 'input' cannot be empty"
    if len(data["input"].strip()) < 10:
        return None, "Field 'input' must be at least 10 characters"
    if len(data["input"].strip()) > 1000:
        return None, "Field 'input' must not exceed 1000 characters"
    return data["input"].strip(), None

@categorise_bp.route("/categorise", methods=["POST"])
def categorise():
    data = request.get_json(silent=True)
    input_text, error = validate_input(data)

    if error:
        logger.warning(f"/categorise validation failed: {error}")
        return jsonify({
            "error": error,
            "timestamp": datetime.now(timezone.utc).isoformat()
        }), 400

    prompt = load_prompt(input_text)
    logger.info(f"/categorise called with input length: {len(input_text)}")
    result = groq_client.call(prompt, temperature=0.2)

    if result is None:
        return jsonify({
            "error": "AI service unavailable",
            "is_fallback": True,
            "timestamp": datetime.now(timezone.utc).isoformat()
        }), 503

    try:
        cleaned = clean_and_parse(result)
        parsed = json.loads(cleaned)

        # Validate confidence is float between 0 and 1
        if "confidence" in parsed:
            parsed["confidence"] = float(parsed["confidence"])
            if parsed["confidence"] > 1.0:
                parsed["confidence"] = parsed["confidence"] / 100.0

        if "generated_at" not in parsed:
            parsed["generated_at"] = datetime.now(timezone.utc).isoformat()

        logger.info(f"/categorise completed: {parsed.get('category')}")
        return jsonify(parsed), 200

    except (json.JSONDecodeError, ValueError) as e:
        logger.error(f"/categorise parse error: {str(e)}")
        return jsonify({
            "raw_response": result,
            "generated_at": datetime.now(timezone.utc).isoformat()
        }), 200