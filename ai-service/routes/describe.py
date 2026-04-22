import json
from flask import Blueprint, request, jsonify
from services.groq_client import GroqClient
from datetime import datetime, timezone

describe_bp = Blueprint("describe", __name__)
groq_client = GroqClient()

def load_prompt(template_path: str, input_text: str) -> str:
    with open(template_path, "r") as f:
        template = f.read()
    return template.replace("{input}", input_text).replace(
        "{generated_at}", datetime.now(timezone.utc).isoformat()
    )

def clean_and_parse(result: str):
    # Fix double curly braces returned by Groq
    result = result.replace("{{", "{").replace("}}", "}")
    # Remove markdown code fences if present
    result = result.strip()
    if result.startswith("```json"):
        result = result[7:]
    if result.startswith("```"):
        result = result[3:]
    if result.endswith("```"):
        result = result[:-3]
    return result.strip()

@describe_bp.route("/describe", methods=["POST"])
def describe():
    data = request.get_json()

    # Input validation
    if not data:
        return jsonify({"error": "Request body is required"}), 400
    if "input" not in data or not data["input"].strip():
        return jsonify({"error": "Field 'input' is required and cannot be empty"}), 400

    input_text = data["input"].strip()

    # Load prompt template
    prompt = load_prompt("prompts/describe_prompt.txt", input_text)

    # Call Groq
    result = groq_client.call(prompt, temperature=0.3)

    if result is None:
        return jsonify({"error": "AI service unavailable. Please try again later."}), 503

    # Clean and parse JSON response
    try:
        cleaned = clean_and_parse(result)
        parsed = json.loads(cleaned)
        return jsonify(parsed), 200
    except json.JSONDecodeError:
        return jsonify({
            "raw_response": result,
            "generated_at": datetime.now(timezone.utc).isoformat()
        }), 200