import requests
import json

BASE_URL = "http://localhost:5000"

print("=" * 60)
print("Day 7 — /categorise and /health endpoint tests")
print("=" * 60)

# ✅ Test 1 — Health check
print("\n--- Test 1: Health check ---")
r = requests.get(f"{BASE_URL}/health")
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"Status: {data.get('status')}")
print(f"Model: {data.get('model')}")
print(f"Uptime: {data.get('uptime_seconds')} seconds")
print(f"ChromaDB docs: {data.get('chroma_doc_count')}")

# ✅ Test 2 — Valid categorise
print("\n--- Test 2: Valid categorise input ---")
r = requests.post(f"{BASE_URL}/categorise",
    json={"input": "Firewall configuration has not been reviewed in 6 months"})
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"Category: {data.get('category')}")
print(f"Confidence: {data.get('confidence')}")
print(f"Reasoning: {data.get('reasoning')}")
print(f"Requirement: {data.get('pci_dss_requirement')}")

# ✅ Test 3 — Second categorise
print("\n--- Test 3: Second categorise input ---")
r = requests.post(f"{BASE_URL}/categorise",
    json={"input": "Cardholder data stored in plain text in database"})
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"Category: {data.get('category')}")
print(f"Confidence: {data.get('confidence')}")

# ✅ Test 4 — Third categorise
print("\n--- Test 4: Third categorise input ---")
r = requests.post(f"{BASE_URL}/categorise",
    json={"input": "No incident response plan exists for data breach scenarios"})
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"Category: {data.get('category')}")
print(f"Confidence: {data.get('confidence')}")

# ❌ Test 5 — Empty input
print("\n--- Test 5: Empty input ---")
r = requests.post(f"{BASE_URL}/categorise", json={"input": ""})
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ❌ Test 6 — No body
print("\n--- Test 6: No body ---")
r = requests.post(f"{BASE_URL}/categorise")
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

print("\n" + "=" * 60)
print("Day 7 tests complete!")
print("=" * 60)