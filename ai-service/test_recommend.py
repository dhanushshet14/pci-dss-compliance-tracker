import requests
import json

BASE_URL = "http://localhost:5000"

print("=" * 60)
print("Day 4 — Full /recommend endpoint tests")
print("=" * 60)

# ✅ Test 1 — Valid input
print("\n--- Test 1: Valid input ---")
r = requests.post(f"{BASE_URL}/recommend",
    json={"input": "Cardholder data is being stored in plain text in the database"})
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
recs = data.get("recommendations", [])
print(f"Number of recommendations: {len(recs)} (expected 3)")
for i, rec in enumerate(recs, 1):
    print(f"\nRecommendation {i}:")
    print(f"  action_type : {rec.get('action_type')}")
    print(f"  priority    : {rec.get('priority')}")
    print(f"  effort      : {rec.get('effort')}")
    print(f"  requirement : {rec.get('pci_dss_requirement')}")
    print(f"  description : {rec.get('description')}")
    print(f"  outcome     : {rec.get('expected_outcome')}")

# ❌ Test 2 — Empty input
print("\n--- Test 2: Empty input ---")
r = requests.post(f"{BASE_URL}/recommend", json={"input": ""})
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ❌ Test 3 — Missing field
print("\n--- Test 3: Missing input field ---")
r = requests.post(f"{BASE_URL}/recommend", json={})
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ❌ Test 4 — No body
print("\n--- Test 4: No body ---")
r = requests.post(f"{BASE_URL}/recommend")
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ✅ Test 5 — Second valid input
print("\n--- Test 5: Second valid input ---")
r = requests.post(f"{BASE_URL}/recommend",
    json={"input": "Default vendor passwords are still active on payment terminals"})
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
recs = data.get("recommendations", [])
print(f"Number of recommendations: {len(recs)} (expected 3)")
print(f"generated_at present: {'generated_at' in data}")
print(f"First rec priority: {recs[0].get('priority') if recs else 'N/A'}")

print("\n" + "=" * 60)
print("Day 4 tests complete!")
print("=" * 60)