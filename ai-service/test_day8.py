import requests
import json

BASE_URL = "http://localhost:5000"

print("=" * 60)
print("Day 8 — SSE Streaming /generate-report/stream tests")
print("=" * 60)

# ✅ Test 1 — Standard endpoint still works
print("\n--- Test 1: Standard /generate-report still works ---")
r = requests.post(f"{BASE_URL}/generate-report",
    json={"input": "Cardholder data stored in plain text, no MFA on systems"},
    timeout=30)
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"Has title: {'title' in data}")
print(f"Has executive_summary: {'executive_summary' in data}")

# ✅ Test 2 — SSE streaming endpoint
print("\n--- Test 2: SSE streaming /generate-report/stream ---")
tokens_received = []
full_text = ""

with requests.post(
    f"{BASE_URL}/generate-report/stream",
    json={"input": "No patch management, default passwords on terminals, plain text card data"},
    stream=True,
    timeout=30
) as r:
    print(f"Status code: {r.status_code} (expected 200)")
    print(f"Content-Type: {r.headers.get('Content-Type')}")
    print("Streaming tokens...")

    for line in r.iter_lines():
        if line:
            decoded = line.decode("utf-8")
            if decoded.startswith("data: "):
                payload = decoded[6:]
                try:
                    event = json.loads(payload)
                    if "token" in event:
                        tokens_received.append(event["token"])
                        print(event["token"], end="", flush=True)
                    elif "done" in event and event["done"]:
                        print(f"\n\n✅ Stream complete!")
                        report = event.get("report", {})
                        print(f"Title: {report.get('title', 'N/A')}")
                        print(f"Has top_items: {'top_items' in report}")
                        print(f"Has recommendations: {'recommendations' in report}")
                except json.JSONDecodeError:
                    pass

print(f"\nTotal tokens received: {len(tokens_received)}")

# ❌ Test 3 — SSE with empty input
print("\n--- Test 3: SSE with empty input ---")
r = requests.post(f"{BASE_URL}/generate-report/stream",
    json={"input": ""},
    timeout=10)
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

# ❌ Test 4 — SSE with no body
print("\n--- Test 4: SSE with no body ---")
r = requests.post(f"{BASE_URL}/generate-report/stream",
    timeout=10)
print(f"Status code: {r.status_code} (expected 400)")
print(f"Output: {r.json()}")

print("\n" + "=" * 60)
print("Day 8 tests complete!")
print("=" * 60)