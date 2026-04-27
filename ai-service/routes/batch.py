import json
import logging
import time
from flask import Blueprint, request, jsonify
from services.groq_client import GroqClient
from datetime import datetime, timezone

batch_bp = Blueprint("batch", __name__)
groq_client = GroqClient()
logger = logging.getLogger(__name__)

MAX_ITEMS = 20
DELAY_BETWEEN_ITEMS = 0.1  # 100ms

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

def process_single_item(item: str, index: int):
    safe_item = item[:50].replace('"', '').replace('\n', ' ')
    prompt = f"""You are a PCI-DSS v4.0 compliance expert.

Analyse this PCI-DSS compliance item and provide a brief structured assessment.

Item: {item}

Respond ONLY in this exact JSON format with no extra text, no markdown, no code fences:
{{
    "title": "Short title for this compliance item",
    "risk_level": "Critical or High or Medium or Low",
    "pci_dss_requirement": "Most relevant requirement e.g. Requirement 1.2",
    "summary": "One sentence summary of the compliance issue",
    "immediate_action": "Most urgent action to take"
}}"""

    result = groq_client.call(prompt, temperature=0.3, max_tokens=400)

    if result is None:
        return {
            "item_index": index,
            "input": safe_item,
            "error": "AI processing failed",
            "is_fallback": True,
            "processed_at": datetime.now(timezone.utc).isoformat()
        }

    try:
        cleaned = clean_and_parse(result)
        parsed = json.loads(cleaned)
        # Always force correct index and metadata
        parsed["item_index"] = index
        parsed["input"] = safe_item
        parsed["processed_at"] = datetime.now(timezone.utc).isoformat()
        return parsed
    except json.JSONDecodeError:
        return {
            "item_index": index,
            "input": safe_item,
            "raw_response": result,
            "processed_at": datetime.now(timezone.utc).isoformat()
        }

def validate_input(data):
    if not data:
        return None, "Request body is required"
    if "items" not in data:
        return None, "Field 'items' is required"
    if not isinstance(data["items"], list):
        return None, "Field 'items' must be an array"
    if len(data["items"]) == 0:
        return None, "Field 'items' cannot be empty"
    if len(data["items"]) > MAX_ITEMS:
        return None, f"Maximum {MAX_ITEMS} items allowed per request"
    for i, item in enumerate(data["items"]):
        if not isinstance(item, str):
            return None, f"Item at index {i} must be a string"
        if len(item.strip()) < 10:
            return None, f"Item at index {i} must be at least 10 characters"
    return data["items"], None

@batch_bp.route("/batch-process", methods=["POST"])
def batch_process():
    # Step 1 — Validate
    data = request.get_json(silent=True)
    items, error = validate_input(data)

    if error:
        logger.warning(f"/batch-process validation failed: {error}")
        return jsonify({
            "error": error,
            "timestamp": datetime.now(timezone.utc).isoformat()
        }), 400

    logger.info(f"/batch-process started — {len(items)} items")
    start_time = time.time()

    results = []
    successful = 0
    failed = 0

    for index, item in enumerate(items):
        # index + 1 so it starts from 1 not 0
        current_index = index + 1
        logger.info(f"Processing item {current_index}/{len(items)}")

        result = process_single_item(item.strip(), current_index)
        results.append(result)

        if "error" in result:
            failed += 1
        else:
            successful += 1

        # 100ms delay between items
        if index < len(items) - 1:
            time.sleep(DELAY_BETWEEN_ITEMS)

    total_time = round(time.time() - start_time, 2)
    logger.info(f"/batch-process completed — {successful} success, {failed} failed in {total_time}s")

    return jsonify({
        "total_items": len(items),
        "successful": successful,
        "failed": failed,
        "processing_time_seconds": total_time,
        "results": results,
        "generated_at": datetime.now(timezone.utc).isoformat()
    }), 200