import requests
import json

BASE_URL = "http://localhost:5000"

# 5 Real PCI-DSS compliance test inputs
test_inputs = [
    "Firewall configuration has not been reviewed in the last 6 months",
    "Default vendor passwords are still active on 3 payment terminals",
    "Cardholder data is being stored in plain text in the database",
    "SSL/TLS encryption is not enabled on the payment gateway API",
    "No access control policy exists for employees handling card data"
]

print("=" * 60)
print("Testing /describe endpoint with 5 PCI-DSS inputs")
print("=" * 60)

for i, test_input in enumerate(test_inputs, 1):
    print(f"\n--- Test {i} ---")
    print(f"Input: {test_input}")

    response = requests.post(
        f"{BASE_URL}/describe",
        json={"input": test_input},
        headers={"Content-Type": "application/json"}
    )

    if response.status_code == 200:
        result = response.json()
        print(f"Status: ✅ SUCCESS")
        print(f"Output:\n{json.dumps(result, indent=2)}")
    else:
        print(f"Status: ❌ FAILED ({response.status_code})")
        print(f"Error: {response.text}")

    print("-" * 60)