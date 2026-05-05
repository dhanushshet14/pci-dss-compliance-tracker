import requests
import json

BASE_URL = "http://localhost:5000"

print("=" * 60)
print("Day 11 — /batch-process endpoint tests")
print("=" * 60)

# ✅ Test 1 — Valid batch with 3 items
print("\n--- Test 1: Valid batch with 3 items ---")
r = requests.post(f"{BASE_URL}/batch-process",
    json={"items": [
        "Firewall configuration has not been reviewed in 6 months",
        "Cardholder data is stored in plain text in the database",
        "Default vendor passwords still active on payment terminals"
    ]},
    timeout=60)
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"Total items: {data.get('total_items')}")
print(f"Successful: {data.get('successful')}")
print(f"Failed: {data.get('failed')}")
print(f"Processing time: {data.get('processing_time_seconds')}s")
print(f"Number of results: {len(data.get('results', []))}")

for result in data.get("results", []):
    print(f"\n  Item {result.get('item_index')}:")
    print(f"    Input    : {result.get('input')}")
    print(f"    Title    : {result.get('title')}")
    print(f"    Risk     : {result.get('risk_level')}")
    print(f"    Req      : {result.get('pci_dss_requirement')}")
    print(f"    Action   : {result.get('immediate_action')}")

# ✅ Test 2 — Single item batch
print("\n--- Test 2: Single item batch ---")
r = requests.post(f"{BASE_URL}/batch-process",
    json={"items": [
        "No access control policy exists for employees handling card data"
    ]},
    timeout=30)
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"Total items: {data.get('total_items')}")
print(f"Successful: {data.get('successful')}")
result = data.get("results", [{}])[0]
print(f"Item index: {result.get('item_index')} (expected 1)")
print(f"Title: {result.get('title')}")

# ✅ Test 3 — Valid batch with 5 items
print("\n--- Test 3: Valid batch with 5 items ---")
r = requests.post(f"{BASE_URL}/batch-process",
    json={"items": [
        "Firewall has not been reviewed in 6 months",
        "Cardholder data stored in plain text in database",
        "Default vendor passwords active on payment terminals",
        "No multi factor authentication on admin accounts",
        "Access logs retained for only 30 days not 12 months"
    ]},
    timeout=90)
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"Total items: {data.get('total_items')} (expected 5)")
print(f"Successful: {data.get('successful')}")
print(f"Processing time: {data.get('processing_time_seconds')}s")
results = data.get("results", [])
for result in results:
    print(f"  Item {result.get('item_index')}: [{result.get('risk_level')}] {result.get('title')}")

# ❌ Test 4 — Too many items (21)
print("\n--- Test 4: Too many items (21) ---")
r = requests.post(f"{BASE_URL}/batch-process",
    json={"items": [
        f"Compliance issue number {i} needs to be fixed immediately"
        for i in range(21)
    ]},
    timeout=10)
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ❌ Test 5 — Empty items array
print("\n--- Test 5: Empty items array ---")
r = requests.post(f"{BASE_URL}/batch-process",
    json={"items": []},
    timeout=10)
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ❌ Test 6 — Missing items field
print("\n--- Test 6: Missing items field ---")
r = requests.post(f"{BASE_URL}/batch-process",
    json={},
    timeout=10)
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ❌ Test 7 — No body
print("\n--- Test 7: No body ---")
r = requests.post(f"{BASE_URL}/batch-process",
    timeout=10)
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

print("\n" + "=" * 60)
print("Day 11 tests complete!")
print("=" * 60)