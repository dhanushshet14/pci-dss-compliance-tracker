import json
import logging
from flask import Blueprint, request, jsonify
from services.shared import groq_client
from datetime import datetime, timezone

analyse_bp = Blueprint("analyse", __name__)
logger = logging.getLogger(__name__)

def load_prompt(template_path: str, input_text: str) -> str:
    with open(template_path, "r") as f:
        template = f.read()
    return template.replace("{input}", input_text).replace(
        "{generated_at}", datetime.now(timezone.utc).isoformat()
    )

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
    if len(data["input"].strip()) > 3000:
        return None, "Field 'input' must not exceed 3000 characters"
    return data["input"].strip(), None

@analyse_bp.route("/analyse-document", methods=["POST"])
def analyse_document():
    data = request.get_json(silent=True)
    input_text, error = validate_input(data)

    if error:
        logger.warning(f"/analyse-document validation failed: {error}")
        return jsonify({
            "error": error,
            "timestamp": datetime.now(timezone.utc).isoformat()
        }), 400

    try:
        prompt = load_prompt("prompts/analyse_prompt.txt", input_text)
    except FileNotFoundError:
        return jsonify({"error": "Prompt template not found"}), 500

    logger.info(f"/analyse-document called with input length: {len(input_text)}")
    result = groq_client.call(prompt, temperature=0.3, max_tokens=1000)

    if result is None:
        return jsonify({
            "error": "AI service unavailable.",
            "is_fallback": True,
            "timestamp": datetime.now(timezone.utc).isoformat()
        }), 503

    try:
        cleaned = clean_and_parse(result)
        parsed = json.loads(cleaned)
        if "findings" not in parsed or not isinstance(parsed["findings"], list):
            parsed["findings"] = []
        if "key_insights" not in parsed or not isinstance(parsed["key_insights"], list):
            parsed["key_insights"] = []
        if "compliance_score" in parsed:
            parsed["compliance_score"] = int(parsed["compliance_score"])
        if "generated_at" not in parsed:
            parsed["generated_at"] = datetime.now(timezone.utc).isoformat()
        logger.info(f"/analyse-document completed — {len(parsed['findings'])} findings")
        return jsonify(parsed), 200
    except (json.JSONDecodeError, ValueError) as e:
        logger.error(f"/analyse-document parse error: {str(e)}")
        return jsonify({
            "raw_response": result,
            "generated_at": datetime.now(timezone.utc).isoformat()
        }), 200