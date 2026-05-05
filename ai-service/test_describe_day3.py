import requests
import json

BASE_URL = "http://localhost:5000"

print("=" * 60)
print("Day 3 — Full /describe endpoint tests")
print("=" * 60)

# ✅ Test 1 — Valid input (happy path)
print("\n--- Test 1: Valid input ---")
r = requests.post(f"{BASE_URL}/describe",
    json={"input": "Firewall configuration has not been reviewed in 6 months"})
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"Has 'title': {'title' in data}")
print(f"Has 'description': {'description' in data}")
print(f"Has 'pci_dss_requirement': {'pci_dss_requirement' in data}")
print(f"Has 'risk_level': {'risk_level' in data}")
print(f"Has 'generated_at': {'generated_at' in data}")
print(f"Output:\n{json.dumps(data, indent=2)}")

# ❌ Test 2 — Empty input
print("\n--- Test 2: Empty input ---")
r = requests.post(f"{BASE_URL}/describe", json={"input": ""})
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ❌ Test 3 — Missing input field
print("\n--- Test 3: Missing input field ---")
r = requests.post(f"{BASE_URL}/describe", json={})
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ❌ Test 4 — No body at all
print("\n--- Test 4: No body ---")
r = requests.post(f"{BASE_URL}/describe")
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ❌ Test 5 — Input too short
print("\n--- Test 5: Input too short ---")
r = requests.post(f"{BASE_URL}/describe", json={"input": "abc"})
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ✅ Test 6 — Valid long input
print("\n--- Test 6: Valid detailed input ---")
r = requests.post(f"{BASE_URL}/describe",
    json={"input": "No patch management process exists for updating antivirus software on point-of-sale systems"})
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"risk_level value: {data.get('risk_level')}")
print(f"pci_dss_requirement: {data.get('pci_dss_requirement')}")
print(f"generated_at present: {'generated_at' in data}")

print("\n" + "=" * 60)
print("Day 3 tests complete!")
print("=" * 60)