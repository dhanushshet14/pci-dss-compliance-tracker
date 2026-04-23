from flask import Blueprint, request, jsonify
from services.groq_client import GroqClient
import json

categorize_bp = Blueprint("categorize", __name__)

client = GroqClient()

@categorize_bp.route("/categorise", methods=["POST"])
def categorise():
    try:
        data = request.get_json()
        user_input = data.get("text")

        if not user_input:
            return jsonify({"error": "No input provided"}), 400

        prompt = f"""
You are a PCI-DSS compliance assistant.

Classify the following issue into ONE of these categories:
- Network Security
- Data Protection
- Access Control
- Monitoring
- Policy Compliance

Also provide:
- confidence (0 to 1)
- reasoning (short explanation)

Return ONLY valid JSON in this format:
{{
  "category": "...",
  "confidence": 0.0,
  "reasoning": "..."
}}

Issue: {user_input}
"""

        result = client.generate(prompt)

        # Clean response (important)
        result = result.strip().replace("```json", "").replace("```", "")

        parsed = json.loads(result)

        return jsonify(parsed)

    except Exception as e:
        return jsonify({"error": str(e)}), 500