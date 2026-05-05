import pytest
import json
from unittest.mock import patch, MagicMock
from app import app

@pytest.fixture
def client():
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client

# Mock Groq response for describe
MOCK_DESCRIBE = json.dumps({
    "title": "Outdated Firewall Review",
    "description": "Firewall not reviewed in 6 months.",
    "pci_dss_requirement": "Requirement 1.2",
    "scope": "Network team",
    "risk_level": "High",
    "compliance_status_explanation": "Must review every 6 months.",
    "generated_at": "2026-04-27T00:00:00+00:00"
})

# Mock Groq response for recommend
MOCK_RECOMMEND = json.dumps({
    "recommendations": [
        {
            "action_type": "Immediate Action",
            "description": "Review firewall now",
            "priority": "High",
            "effort": "Low",
            "pci_dss_requirement": "Requirement 1.2",
            "expected_outcome": "Firewall secured"
        },
        {
            "action_type": "Short-term Fix",
            "description": "Schedule quarterly reviews",
            "priority": "Medium",
            "effort": "Medium",
            "pci_dss_requirement": "Requirement 1.2",
            "expected_outcome": "Regular reviews in place"
        },
        {
            "action_type": "Long-term Improvement",
            "description": "Automate firewall audits",
            "priority": "Low",
            "effort": "High",
            "pci_dss_requirement": "Requirement 1.2",
            "expected_outcome": "Automated compliance"
        }
    ],
    "generated_at": "2026-04-27T00:00:00+00:00"
})

# Mock Groq response for categorise
MOCK_CATEGORISE = json.dumps({
    "category": "Network Security",
    "confidence": 0.95,
    "reasoning": "Firewall is network security",
    "pci_dss_requirement": "Requirement 1.2",
    "generated_at": "2026-04-27T00:00:00+00:00"
})

# Mock Groq response for report
MOCK_REPORT = json.dumps({
    "title": "PCI-DSS Compliance Report",
    "executive_summary": "High risk findings identified.",
    "overview": "Assessment of compliance posture.",
    "top_items": [
        {"item": "Unencrypted data", "severity": "Critical",
         "pci_dss_requirement": "Requirement 3.4", "impact": "Data breach risk"}
    ],
    "recommendations": [
        {"priority": "1", "action": "Encrypt data",
         "timeline": "Immediate", "expected_outcome": "Data secured"}
    ],
    "generated_at": "2026-04-27T00:00:00+00:00"
})

# Mock Groq response for analyse
MOCK_ANALYSE = json.dumps({
    "document_summary": "Payment system has vulnerabilities.",
    "compliance_score": 40,
    "findings": [
        {
            "finding_id": "F001",
            "title": "Unencrypted Data",
            "description": "Card data stored in plain text.",
            "severity": "Critical",
            "pci_dss_requirement": "Requirement 3.4",
            "recommendation": "Encrypt all card data"
        }
    ],
    "key_insights": ["Critical encryption issue found"],
    "overall_risk": "Critical",
    "generated_at": "2026-04-27T00:00:00+00:00"
})


# ── Test 1 — /describe valid input ──────────────────────
def test_describe_valid_input(client):
    with patch("routes.describe.groq_client.call", return_value=MOCK_DESCRIBE):
        r = client.post("/describe",
            json={"input": "Firewall not reviewed in 6 months"},
            content_type="application/json")
        assert r.status_code == 200
        data = r.get_json()
        assert "title" in data
        assert "description" in data
        assert "risk_level" in data
        assert "generated_at" in data


# ── Test 2 — /describe empty input ──────────────────────
def test_describe_empty_input(client):
    r = client.post("/describe",
        json={"input": ""},
        content_type="application/json")
    assert r.status_code == 400
    data = r.get_json()
    assert "error" in data


# ── Test 3 — /describe missing field ────────────────────
def test_describe_missing_field(client):
    r = client.post("/describe",
        json={},
        content_type="application/json")
    assert r.status_code == 400
    data = r.get_json()
    assert "error" in data


# ── Test 4 — /describe no body ──────────────────────────
def test_describe_no_body(client):
    r = client.post("/describe")
    assert r.status_code == 400
    data = r.get_json()
    assert "error" in data


# ── Test 5 — /recommend valid input ─────────────────────
def test_recommend_valid_input(client):
    with patch("routes.recommend.groq_client.call", return_value=MOCK_RECOMMEND):
        r = client.post("/recommend",
            json={"input": "Cardholder data stored in plain text"},
            content_type="application/json")
        assert r.status_code == 200
        data = r.get_json()
        assert "recommendations" in data
        assert len(data["recommendations"]) == 3
        assert "action_type" in data["recommendations"][0]
        assert "priority" in data["recommendations"][0]


# ── Test 6 — /recommend empty input ─────────────────────
def test_recommend_empty_input(client):
    r = client.post("/recommend",
        json={"input": ""},
        content_type="application/json")
    assert r.status_code == 400


# ── Test 7 — /categorise valid input ────────────────────
def test_categorise_valid_input(client):
    with patch("routes.categorise.groq_client.call", return_value=MOCK_CATEGORISE):
        r = client.post("/categorise",
            json={"input": "Firewall not reviewed in 6 months"},
            content_type="application/json")
        assert r.status_code == 200
        data = r.get_json()
        assert "category" in data
        assert "confidence" in data
        assert "reasoning" in data
        assert data["confidence"] <= 1.0


# ── Test 8 — /generate-report valid input ───────────────
def test_generate_report_valid_input(client):
    with patch("routes.report.groq_client.call", return_value=MOCK_REPORT):
        r = client.post("/generate-report",
            json={"input": "Unencrypted data and default passwords found"},
            content_type="application/json")
        assert r.status_code == 200
        data = r.get_json()
        assert "title" in data
        assert "executive_summary" in data
        assert "top_items" in data
        assert "recommendations" in data


# ── Test 9 — /analyse-document valid input ──────────────
def test_analyse_document_valid_input(client):
    with patch("routes.analyse.groq_client.call", return_value=MOCK_ANALYSE):
        r = client.post("/analyse-document",
            json={"input": "Payment system stores card data without encryption and passwords not rotated"},
            content_type="application/json")
        assert r.status_code == 200
        data = r.get_json()
        assert "findings" in data
        assert "key_insights" in data
        assert "compliance_score" in data
        assert "overall_risk" in data
        assert isinstance(data["compliance_score"], int)


# ── Test 10 — /analyse-document Groq failure ────────────
def test_analyse_document_groq_failure(client):
    with patch("routes.analyse.groq_client.call", return_value=None):
        r = client.post("/analyse-document",
            json={"input": "Payment system stores card data without encryption"},
            content_type="application/json")
        assert r.status_code == 503
        data = r.get_json()
        assert "error" in data
        assert data.get("is_fallback") == True