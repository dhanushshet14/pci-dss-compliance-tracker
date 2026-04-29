import requests
import json

BASE_URL = "http://localhost:5000"

print("=" * 60)
print("Day 15 — Final AI QA — All 6 Endpoints")
print("=" * 60)

passed = 0
failed = 0

def check(condition, message):
    global passed, failed
    if condition:
        print(f"  ✅ {message}")
        passed += 1
    else:
        print(f"  ❌ {message}")
        failed += 1

# ── Endpoint 1: /describe ────────────────────────────────
print("\n━━━ Endpoint 1: POST /describe ━━━")
r = requests.post(f"{BASE_URL}/describe",
    json={"input": "SSL TLS 1.0 is still enabled on the payment gateway API instead of TLS 1.2"},
    timeout=30)
data = r.json()
check(r.status_code == 200, f"Status 200")
check("title" in data, "Has title")
check("description" in data, "Has description")
check("pci_dss_requirement" in data, "Has pci_dss_requirement")
check("risk_level" in data, "Has risk_level")
check(data.get("risk_level") in ["Critical","High","Medium","Low"], "risk_level is valid value")
check("generated_at" in data, "Has generated_at")
print(f"  Title: {data.get('title')}")
print(f"  Risk: {data.get('risk_level')}")
print(f"  Requirement: {data.get('pci_dss_requirement')}")

# ── Endpoint 2: /recommend ───────────────────────────────
print("\n━━━ Endpoint 2: POST /recommend ━━━")
r = requests.post(f"{BASE_URL}/recommend",
    json={"input": "No patch management process for payment systems — critical patches not applied for 3 months"},
    timeout=30)
data = r.json()
recs = data.get("recommendations", [])
check(r.status_code == 200, "Status 200")
check("recommendations" in data, "Has recommendations array")
check(len(recs) == 3, f"Has exactly 3 recommendations ({len(recs)} found)")
check("action_type" in recs[0], "Has action_type")
check("priority" in recs[0], "Has priority")
check("expected_outcome" in recs[0], "Has expected_outcome")
check("generated_at" in data, "Has generated_at")
print(f"  Rec 1: [{recs[0].get('priority')}] {recs[0].get('action_type')}")
print(f"  Rec 2: [{recs[1].get('priority')}] {recs[1].get('action_type')}")
print(f"  Rec 3: [{recs[2].get('priority')}] {recs[2].get('action_type')}")

# ── Endpoint 3: /categorise ──────────────────────────────
print("\n━━━ Endpoint 3: POST /categorise ━━━")
r = requests.post(f"{BASE_URL}/categorise",
    json={"input": "Audit logs are only retained for 30 days instead of required 12 months"},
    timeout=30)
data = r.json()
valid_categories = [
    "Network Security", "Access Control", "Data Protection",
    "Vulnerability Management", "Monitoring and Logging",
    "Physical Security", "Incident Response", "Policy and Governance"
]
check(r.status_code == 200, "Status 200")
check("category" in data, "Has category")
check(data.get("category") in valid_categories, f"Category is valid: {data.get('category')}")
check("confidence" in data, "Has confidence")
check(0.0 <= float(data.get("confidence", 0)) <= 1.0, "Confidence between 0 and 1")
check("reasoning" in data, "Has reasoning")
check("pci_dss_requirement" in data, "Has pci_dss_requirement")
print(f"  Category: {data.get('category')}")
print(f"  Confidence: {data.get('confidence')}")

# ── Endpoint 4: /generate-report ────────────────────────
print("\n━━━ Endpoint 4: POST /generate-report ━━━")
r = requests.post(f"{BASE_URL}/generate-report",
    json={"input": "Organization has unencrypted cardholder data in MySQL database, default passwords on 5 payment terminals, firewall not reviewed in 9 months, no MFA on admin accounts, audit logs retained for only 45 days"},
    timeout=30)
data = r.json()
top_items = data.get("top_items", [])
recs = data.get("recommendations", [])
check(r.status_code == 200, "Status 200")
check("title" in data, "Has title")
check("executive_summary" in data, "Has executive_summary")
check("overview" in data, "Has overview")
check("top_items" in data, "Has top_items")
check(len(top_items) >= 1, f"Has top items ({len(top_items)} found)")
check("recommendations" in data, "Has recommendations")
check(len(recs) >= 1, f"Has recommendations ({len(recs)} found)")
check("generated_at" in data, "Has generated_at")
print(f"  Title: {data.get('title')}")
print(f"  Top items: {len(top_items)}")
print(f"  Recommendations: {len(recs)}")
for item in top_items:
    print(f"    [{item.get('severity')}] {item.get('item')}")

# ── Endpoint 5: /analyse-document ───────────────────────
print("\n━━━ Endpoint 5: POST /analyse-document ━━━")
r = requests.post(f"{BASE_URL}/analyse-document",
    json={"input": "Security audit revealed: payment database stores full card numbers in plain text. Employee passwords unchanged for 2 years. No intrusion detection system in place. Physical access to server room is uncontrolled with no visitor logs maintained. Last penetration test was conducted 3 years ago."},
    timeout=30)
data = r.json()
findings = data.get("findings", [])
insights = data.get("key_insights", [])
check(r.status_code == 200, "Status 200")
check("document_summary" in data, "Has document_summary")
check("findings" in data, "Has findings array")
check(len(findings) >= 1, f"Has findings ({len(findings)} found)")
check("compliance_score" in data, "Has compliance_score")
check(isinstance(data.get("compliance_score"), int), "compliance_score is integer")
check("overall_risk" in data, "Has overall_risk")
check("key_insights" in data, "Has key_insights")
check(len(insights) >= 1, f"Has insights ({len(insights)} found)")
print(f"  Compliance score: {data.get('compliance_score')}")
print(f"  Overall risk: {data.get('overall_risk')}")
print(f"  Findings: {len(findings)}")
for f in findings:
    print(f"    [{f.get('severity')}] {f.get('title')}")

# ── Endpoint 6: /batch-process ───────────────────────────
print("\n━━━ Endpoint 6: POST /batch-process ━━━")
r = requests.post(f"{BASE_URL}/batch-process",
    json={"items": [
        "No encryption on cardholder data stored in database",
        "Default vendor passwords active on payment terminals",
        "Firewall configuration not reviewed for over 6 months",
        "No multi-factor authentication on privileged accounts",
        "Audit logs retained for only 30 days"
    ]},
    timeout=90)
data = r.json()
results = data.get("results", [])
check(r.status_code == 200, "Status 200")
check("total_items" in data, "Has total_items")
check(data.get("total_items") == 5, "total_items is 5")
check("successful" in data, "Has successful count")
check(data.get("successful") >= 4, f"At least 4 successful ({data.get('successful')} found)")
check("results" in data, "Has results array")
check(len(results) == 5, f"Has 5 results ({len(results)} found)")
check("processing_time_seconds" in data, "Has processing_time_seconds")
print(f"  Total: {data.get('total_items')}")
print(f"  Successful: {data.get('successful')}")
print(f"  Time: {data.get('processing_time_seconds')}s")
for res in results:
    print(f"    Item {res.get('item_index')}: [{res.get('risk_level')}] {res.get('title')}")

# ── Health Check ─────────────────────────────────────────
print("\n━━━ Health Check ━━━")
r = requests.get(f"{BASE_URL}/health")
data = r.json()
check(r.status_code == 200, "Status 200")
check(data.get("status") == "ok", "Status is ok")
check(data.get("chroma_doc_count", 0) > 50, f"ChromaDB has 50+ chunks ({data.get('chroma_doc_count')} found)")
check("uptime_seconds" in data, "Has uptime")
check("model" in data, "Has model name")
print(f"  Uptime: {data.get('uptime_seconds')}s")
print(f"  Model: {data.get('model')}")
print(f"  ChromaDB: {data.get('chroma_doc_count')} chunks")

# ── Final Summary ─────────────────────────────────────────
print("\n" + "=" * 60)
total = passed + failed
print(f"FINAL QA RESULTS: {passed}/{total} checks passed")
if failed == 0:
    print("✅ ALL CHECKS PASSED — DEMO READY!")
else:
    print(f"⚠️ {failed} checks failed — fix before demo")
print("=" * 60)