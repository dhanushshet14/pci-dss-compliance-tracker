import json
import logging
from flask import Blueprint, request, jsonify
from services.shared import groq_client
from datetime import datetime, timezone

describe_bp = Blueprint("describe", __name__)
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
    errors = []
    if not data:
        return None, "Request body is required"
    if "input" not in data:
        errors.append("Field 'input' is required")
    elif not isinstance(data["input"], str):
        errors.append("Field 'input' must be a string")
    elif len(data["input"].strip()) == 0:
        errors.append("Field 'input' cannot be empty")
    elif len(data["input"].strip()) < 10:
        errors.append("Field 'input' must be at least 10 characters")
    elif len(data["input"].strip()) > 1000:
        errors.append("Field 'input' must not exceed 1000 characters")
    if errors:
        return None, errors[0]
    return data["input"].strip(), None

@describe_bp.route("/describe", methods=["POST"])
def describe():
    data = request.get_json(silent=True)
    input_text, error = validate_input(data)

    if error:
        logger.warning(f"/describe validation failed: {error}")
        return jsonify({
            "error": error,
            "timestamp": datetime.now(timezone.utc).isoformat()
        }), 400

    try:
        prompt = load_prompt("prompts/describe_prompt.txt", input_text)
    except FileNotFoundError:
        logger.error("describe_prompt.txt not found")
        return jsonify({"error": "Prompt template not found"}), 500

    logger.info(f"/describe called with input length: {len(input_text)}")
    result = groq_client.call(prompt, temperature=0.3)

    if result is None:
        logger.error("/describe Groq call failed after retries")
        return jsonify({
            "error": "AI service unavailable. Please try again later.",
            "is_fallback": True,
            "timestamp": datetime.now(timezone.utc).isoformat()
        }), 503

    try:
        cleaned = clean_and_parse(result)
        parsed = json.loads(cleaned)
        if "generated_at" not in parsed:
            parsed["generated_at"] = datetime.now(timezone.utc).isoformat()
        logger.info("/describe completed successfully")
        return jsonify(parsed), 200
    except json.JSONDecodeError as e:
        logger.error(f"/describe JSON parse error: {str(e)}")
        return jsonify({
            "raw_response": result,
            "generated_at": datetime.now(timezone.utc).isoformat()
        }), 200