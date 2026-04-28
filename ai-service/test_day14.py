import requests

BASE_URL = "http://localhost:5000"

print("=" * 60)
print("Day 14 — ChromaDB Seeding Tests")
print("=" * 60)

# ✅ Test 1 — Health check shows more chunks
print("\n--- Test 1: Health check — ChromaDB chunk count ---")
r = requests.get(f"{BASE_URL}/health")
data = r.json()
print(f"Status: {data.get('status')}")
chroma_count = data.get('chroma_doc_count', 0)
print(f"ChromaDB chunks: {chroma_count}")
assert chroma_count > 11, f"Expected more than 11 chunks, got {chroma_count}"
print(f"✅ ChromaDB seeded with {chroma_count} chunks from 10 documents")

# ✅ Test 2 — Query about firewall
print("\n--- Test 2: Describe with firewall topic ---")
r = requests.post(f"{BASE_URL}/describe",
    json={"input": "Firewall rules not reviewed in 8 months"},
    timeout=30)
print(f"Status: {r.status_code} (expected 200)")
data = r.json()
print(f"Title: {data.get('title')}")
print(f"Requirement: {data.get('pci_dss_requirement')}")

# ✅ Test 3 — Query about encryption
print("\n--- Test 3: Describe with encryption topic ---")
r = requests.post(f"{BASE_URL}/describe",
    json={"input": "Cardholder data encrypted but key management procedures not documented"},
    timeout=30)
print(f"Status: {r.status_code} (expected 200)")
data = r.json()
print(f"Title: {data.get('title')}")
print(f"Risk level: {data.get('risk_level')}")

# ✅ Test 4 — Query about access control
print("\n--- Test 4: Recommend for access control ---")
r = requests.post(f"{BASE_URL}/recommend",
    json={"input": "No multi-factor authentication on admin accounts"},
    timeout=30)
print(f"Status: {r.status_code} (expected 200)")
data = r.json()
recs = data.get("recommendations", [])
print(f"Number of recommendations: {len(recs)}")
print(f"First rec priority: {recs[0].get('priority') if recs else 'N/A'}")

# ✅ Test 5 — Generate report with seeded data
print("\n--- Test 5: Generate report ---")
r = requests.post(f"{BASE_URL}/generate-report",
    json={"input": "Missing patches on payment systems, no MFA, plain text card storage"},
    timeout=30)
print(f"Status: {r.status_code} (expected 200)")
data = r.json()
print(f"Title: {data.get('title')}")
print(f"Top items count: {len(data.get('top_items', []))}")

# ✅ Test 6 — Analyse document
print("\n--- Test 6: Analyse document ---")
r = requests.post(f"{BASE_URL}/analyse-document",
    json={"input": "Our organization uses TLS 1.0 for payment gateway. Logs are kept for 60 days. No penetration testing has been done this year. Access logs show multiple failed login attempts with no alerts generated."},
    timeout=30)
print(f"Status: {r.status_code} (expected 200)")
data = r.json()
print(f"Compliance score: {data.get('compliance_score')}")
print(f"Overall risk: {data.get('overall_risk')}")
print(f"Findings count: {len(data.get('findings', []))}")

print("\n" + "=" * 60)
print("Day 14 seeding tests complete!")
print("=" * 60)