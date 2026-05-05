# PCI-DSS Compliance Tracker — AI Service

## Overview
AI microservice built with Flask and Groq API (LLaMA-3.3-70b) providing
intelligent PCI-DSS compliance analysis. Includes RAG pipeline using
ChromaDB and a vector embedding model for context-aware responses.

---

## Tech Stack
| Technology | Purpose |
|---|---|
| Python 3.11+ | Language |
| Flask 3.0.3 | Web framework |
| Groq API (LLaMA-3.3-70b) | AI model |
| ChromaDB | Vector database for RAG |
| flask-limiter | Rate limiting |
| pytest | Unit testing |

---

## Prerequisites
- Python 3.11 or higher
- Groq API key (free at https://console.groq.com)
- Microsoft C++ Build Tools (for ChromaDB on Windows)

---

## Environment Variables
Create a `.env` file in the `ai-service/` folder:
```
GROQ_API_KEY=your_groq_api_key_here
```

---

## Setup Instructions

### Step 1 — Clone and navigate
```bash
git clone https://github.com/tecsxpert/pci-dss-compliance-tracker
cd pci-dss-compliance-tracker/ai-service
```

### Step 2 — Install dependencies
```bash
pip install -r requirements.txt
```

### Step 3 — Create `.env` file
```bash
GROQ_API_KEY=your_groq_api_key_here
```

### Step 4 — Run the service
```bash
python app.py
```

### Step 5 — Verify service is running
```bash
curl http://localhost:5000/health
```

Expected response:
```json
{
  "status": "ok",
  "service": "pci-dss-ai-service",
  "version": "1.0.0",
  "uptime_seconds": 10,
  "chroma_doc_count": 11,
  "model": "llama-3.3-70b-versatile"
}
```

---

## Run with Docker
```bash
docker build -t pci-dss-ai-service .
docker run -p 5000:5000 --env-file .env pci-dss-ai-service
```

---

## API Endpoints

### 1. POST /describe
Generates structured description of a PCI-DSS compliance item.

**Request:**
```json
{
  "input": "Firewall configuration has not been reviewed in 6 months"
}
```

**Response:**
```json
{
  "title": "Outdated Firewall Configuration Review",
  "description": "Firewall not reviewed in 6 months...",
  "pci_dss_requirement": "Requirement 1.2",
  "scope": "Network Security Team",
  "risk_level": "High",
  "compliance_status_explanation": "Must review every 6 months",
  "generated_at": "2026-04-27T00:00:00+00:00"
}
```

---

### 2. POST /recommend
Returns 3 actionable recommendations for a compliance issue.

**Request:**
```json
{
  "input": "Cardholder data stored in plain text"
}
```

**Response:**
```json
{
  "recommendations": [
    {
      "action_type": "Immediate Action",
      "description": "Encrypt all cardholder data",
      "priority": "Critical",
      "effort": "High",
      "pci_dss_requirement": "Requirement 3.4",
      "expected_outcome": "Data protected"
    }
  ],
  "generated_at": "2026-04-27T00:00:00+00:00"
}
```

---

### 3. POST /categorise
Classifies compliance item into PCI-DSS category.

**Request:**
```json
{
  "input": "Firewall not reviewed in 6 months"
}
```

**Response:**
```json
{
  "category": "Network Security",
  "confidence": 0.95,
  "reasoning": "Firewall is a network security control",
  "pci_dss_requirement": "Requirement 1.2",
  "generated_at": "2026-04-27T00:00:00+00:00"
}
```

**Categories:**
- Network Security
- Access Control
- Data Protection
- Vulnerability Management
- Monitoring and Logging
- Physical Security
- Incident Response
- Policy and Governance

---

### 4. POST /generate-report
Generates full compliance report (JSON or SSE streaming).

**Request:**
```json
{
  "input": "Unencrypted data, default passwords, firewall not reviewed"
}
```

**Response:**
```json
{
  "title": "PCI-DSS Compliance Report",
  "executive_summary": "High risk findings identified...",
  "overview": "Assessment of compliance posture...",
  "top_items": [...],
  "recommendations": [...],
  "generated_at": "2026-04-27T00:00:00+00:00"
}
```

**SSE Streaming:** `POST /generate-report/stream`
React frontend reads with `EventSource`.

---

### 5. POST /analyse-document
Analyses document text for compliance insights and risks.

**Request:**
```json
{
  "input": "Our payment system stores credit card numbers without encryption..."
}
```

**Response:**
```json
{
  "document_summary": "Payment system has vulnerabilities...",
  "compliance_score": 40,
  "findings": [
    {
      "finding_id": "F001",
      "title": "Unencrypted Data",
      "severity": "Critical",
      "pci_dss_requirement": "Requirement 3.4",
      "recommendation": "Encrypt all card data"
    }
  ],
  "key_insights": ["Critical encryption issue found"],
  "overall_risk": "Critical",
  "generated_at": "2026-04-27T00:00:00+00:00"
}
```

---

### 6. POST /batch-process
Processes up to 20 compliance items in one request.

**Request:**
```json
{
  "items": [
    "Firewall not reviewed in 6 months",
    "Cardholder data stored in plain text"
  ]
}
```

**Response:**
```json
{
  "total_items": 2,
  "successful": 2,
  "failed": 0,
  "processing_time_seconds": 1.2,
  "results": [
    {
      "item_index": 1,
      "title": "Outdated Firewall Review",
      "risk_level": "High",
      "pci_dss_requirement": "Requirement 1.2",
      "immediate_action": "Review firewall now"
    }
  ],
  "generated_at": "2026-04-27T00:00:00+00:00"
}
```

---

### 7. GET /health
Returns service health status.

**Response:**
```json
{
  "status": "ok",
  "service": "pci-dss-ai-service",
  "version": "1.0.0",
  "uptime_seconds": 120,
  "chroma_doc_count": 11,
  "model": "llama-3.3-70b-versatile"
}
```

---

## Validation Rules

| Field | Rule |
|---|---|
| `input` | Required, string, 10-1000 chars |
| `items` | Required, array, 1-20 items |
| Each item | String, min 10 chars |

---

## Error Responses

| Code | Meaning |
|---|---|
| 400 | Validation error — check request body |
| 503 | AI service unavailable — Groq API down |
| 500 | Internal server error |

---

## Running Tests
```bash
pytest tests/test_endpoints.py -v
```
Expected: **10 passed**

---

## RAG Pipeline
- Knowledge base: `docs/pci_dss_knowledge.txt`
- 11 chunks stored in ChromaDB
- Embedding model: ChromaDB DefaultEmbeddingFunction
- Query returns top 3 relevant chunks

---

## Rate Limiting
- Default: 30 requests/minute per IP
- `/generate-report`: 10 requests/minute per IP

---

## Author
AI Developer 1 — Shashank K R
Sprint: 14 April – 9 May 2026