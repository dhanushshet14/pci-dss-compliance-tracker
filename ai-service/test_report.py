import requests
import json

BASE_URL = "http://localhost:5000"

print("=" * 60)
print("Day 6 — /generate-report endpoint tests")
print("=" * 60)

# ✅ Test 1 — Valid input
print("\n--- Test 1: Valid input ---")
r = requests.post(f"{BASE_URL}/generate-report",
    json={"input": "Organization has unencrypted cardholder data in database, default vendor passwords on payment terminals, and firewall not reviewed in 8 months"},
    timeout=30)
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"Has 'title': {'title' in data}")
print(f"Has 'executive_summary': {'executive_summary' in data}")
print(f"Has 'overview': {'overview' in data}")
print(f"Has 'top_items': {'top_items' in data}")
print(f"Has 'recommendations': {'recommendations' in data}")
print(f"Has 'generated_at': {'generated_at' in data}")
print(f"Number of top_items: {len(data.get('top_items', []))}")
print(f"Number of recommendations: {len(data.get('recommendations', []))}")
print(f"\nTitle: {data.get('title')}")
print(f"\nExecutive Summary: {data.get('executive_summary')}")
print(f"\nTop Items:")
for item in data.get("top_items", []):
    print(f"  - [{item.get('severity')}] {item.get('item')}")
print(f"\nRecommendations:")
for rec in data.get("recommendations", []):
    print(f"  {rec.get('priority')}. [{rec.get('timeline')}] {rec.get('action')}")

# ❌ Test 2 — Empty input
print("\n--- Test 2: Empty input ---")
r = requests.post(f"{BASE_URL}/generate-report", json={"input": ""})
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ❌ Test 3 — No body
print("\n--- Test 3: No body ---")
r = requests.post(f"{BASE_URL}/generate-report")
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ✅ Test 4 — Second valid input
print("\n--- Test 4: Second valid input ---")
r = requests.post(f"{BASE_URL}/generate-report",
    json={"input": "No multi-factor authentication on cardholder data systems, access logs not retained for 12 months, no incident response plan"},
    timeout=30)
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"Title: {data.get('title')}")
print(f"Top item severity: {data.get('top_items', [{}])[0].get('severity')}")
print(f"generated_at present: {'generated_at' in data}")

print("\n" + "=" * 60)
print("Day 6 tests complete!")
print("=" * 60)