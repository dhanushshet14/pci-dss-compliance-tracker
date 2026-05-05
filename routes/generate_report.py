import json
import threading
from flask import Blueprint, request, jsonify

from services.job_service import create_job, update_job, get_job
from services.shared import groq_client

# ✅ Blueprint (VERY IMPORTANT)
generate_report_bp = Blueprint("generate_report", __name__)


# ================== BACKGROUND FUNCTION ==================
def process_report(job_id, input_text):
    prompt = f"""
You are a PCI-DSS compliance expert.

Generate a detailed compliance report for:

{input_text}

Return JSON:
{{
  "summary": "...",
  "risks": ["..."],
  "recommendations": ["..."]
}}
"""

    result = groq_client.call(prompt)

    # ✅ clean + parse JSON
    clean_text = result["data"].replace("```json", "").replace("```", "").strip()

    try:
        parsed = json.loads(clean_text)
    except:
        parsed = {"raw": clean_text}

    update_job(job_id, {
        "data": parsed,
        "meta": result["meta"]
    })


# ================== MAIN API ==================
@generate_report_bp.route("/generate-report", methods=["POST"])
def generate_report():
    data = request.get_json()
    input_text = data.get("input", "")

    job_id = create_job()

    thread = threading.Thread(target=process_report, args=(job_id, input_text))
    thread.start()

    return jsonify({
        "job_id": job_id,
        "status": "processing"
    }), 202


# ================== JOB STATUS API ==================
@generate_report_bp.route("/job/<job_id>", methods=["GET"])
def get_job_status(job_id):
    job = get_job(job_id)

    if not job:
        return jsonify({"error": "Job not found"}), 404

    return jsonify(job), 200