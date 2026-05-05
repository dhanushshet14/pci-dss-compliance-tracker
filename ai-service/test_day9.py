import requests
import json

BASE_URL = "http://localhost:5000"

print("=" * 60)
print("Day 9 — /analyse-document endpoint tests")
print("=" * 60)

# ✅ Test 1 — Valid document input
print("\n--- Test 1: Valid document input ---")
r = requests.post(f"{BASE_URL}/analyse-document",
    json={"input": "Our payment system stores credit card numbers in a MySQL database without encryption. Employee passwords are changed every 180 days. We have a firewall but it has not been reviewed in over 8 months. Access logs are kept for 30 days only."},
    timeout=30)
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"Has 'document_summary': {'document_summary' in data}")
print(f"Has 'findings': {'findings' in data}")
print(f"Has 'key_insights': {'key_insights' in data}")
print(f"Has 'overall_risk': {'overall_risk' in data}")
print(f"Has 'compliance_score': {'compliance_score' in data}")
print(f"Number of findings: {len(data.get('findings', []))}")
print(f"Compliance score: {data.get('compliance_score')}")
print(f"Overall risk: {data.get('overall_risk')}")
print(f"\nDocument Summary: {data.get('document_summary')}")
print(f"\nFindings:")
for f in data.get("findings", []):
    print(f"  [{f.get('severity')}] {f.get('finding_id')}: {f.get('title')}")
print(f"\nKey Insights:")
for insight in data.get("key_insights", []):
    print(f"  - {insight}")

# ✅ Test 2 — Second valid input
print("\n--- Test 2: Second valid input ---")
r = requests.post(f"{BASE_URL}/analyse-document",
    json={"input": "The organization has implemented multi-factor authentication for all system access. Regular penetration testing is conducted quarterly. However, vendor access is not monitored and third-party risk assessments have not been performed this year."},
    timeout=30)
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"Number of findings: {len(data.get('findings', []))}")
print(f"Overall risk: {data.get('overall_risk')}")
print(f"Compliance score: {data.get('compliance_score')}")

# ❌ Test 3 — Empty input
print("\n--- Test 3: Empty input ---")
r = requests.post(f"{BASE_URL}/analyse-document", json={"input": ""})
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ❌ Test 4 — No body
print("\n--- Test 4: No body ---")
r = requests.post(f"{BASE_URL}/analyse-document")
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ❌ Test 5 — Too short
print("\n--- Test 5: Input too short ---")
r = requests.post(f"{BASE_URL}/analyse-document", json={"input": "short"})
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

print("\n" + "=" * 60)
print("Day 9 tests complete!")
print("=" * 60)