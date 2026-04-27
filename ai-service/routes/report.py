import json
import logging
from flask import Blueprint, request, jsonify, Response, stream_with_context
from services.shared import groq_client
from datetime import datetime, timezone

report_bp = Blueprint("report", __name__)
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
    if len(data["input"].strip()) > 2000:
        return None, "Field 'input' must not exceed 2000 characters"
    return data["input"].strip(), None

@report_bp.route("/generate-report", methods=["POST"])
def generate_report():
    data = request.get_json(silent=True)
    input_text, error = validate_input(data)

    if error:
        logger.warning(f"/generate-report validation failed: {error}")
        return jsonify({
            "error": error,
            "timestamp": datetime.now(timezone.utc).isoformat()
        }), 400

    try:
        prompt = load_prompt("prompts/generate_report_prompt.txt", input_text)
    except FileNotFoundError:
        return jsonify({"error": "Prompt template not found"}), 500

    logger.info(f"/generate-report called with input length: {len(input_text)}")
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
        required = ["title", "executive_summary", "overview", "top_items", "recommendations"]
        for field in required:
            if field not in parsed:
                parsed[field] = "Not available"
        if "generated_at" not in parsed:
            parsed["generated_at"] = datetime.now(timezone.utc).isoformat()
        logger.info("/generate-report completed successfully")
        return jsonify(parsed), 200
    except json.JSONDecodeError as e:
        logger.error(f"/generate-report JSON parse error: {str(e)}")
        return jsonify({
            "raw_response": result,
            "generated_at": datetime.now(timezone.utc).isoformat()
        }), 200

@report_bp.route("/generate-report/stream", methods=["POST"])
def generate_report_stream():
    data = request.get_json(silent=True)
    input_text, error = validate_input(data)

    if error:
        logger.warning(f"/generate-report/stream validation failed: {error}")
        return jsonify({
            "error": error,
            "timestamp": datetime.now(timezone.utc).isoformat()
        }), 400

    try:
        prompt = load_prompt("prompts/generate_report_prompt.txt", input_text)
    except FileNotFoundError:
        return jsonify({"error": "Prompt template not found"}), 500

    logger.info("/generate-report/stream called")

    def generate():
        try:
            stream = groq_client.call_stream(prompt, temperature=0.3)
            full_response = ""
            for token in stream:
                if token:
                    full_response += token
                    yield f"data: {json.dumps({'token': token})}\n\n"
            try:
                cleaned = clean_and_parse(full_response)
                parsed = json.loads(cleaned)
                if "generated_at" not in parsed:
                    parsed["generated_at"] = datetime.now(timezone.utc).isoformat()
                yield f"data: {json.dumps({'done': True, 'report': parsed})}\n\n"
            except json.JSONDecodeError:
                yield f"data: {json.dumps({'done': True, 'raw_response': full_response})}\n\n"
        except Exception as e:
            logger.error(f"SSE stream error: {str(e)}")
            yield f"data: {json.dumps({'error': str(e)})}\n\n"

    return Response(
        stream_with_context(generate()),
        mimetype="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no",
            "Access-Control-Allow-Origin": "*"
        }
    )