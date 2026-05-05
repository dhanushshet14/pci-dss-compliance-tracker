# AI Developer 1 — Demo Script
## Demo Day: Friday 9 May 2026 | Time: 90 seconds

---

## 🎯 Your 90-Second Section in 8-Minute Demo

You present after Java Developer 2 shows audit log and search features.
You have exactly 90 seconds. Practice until you can do it in 85 seconds.

---

## 📌 What to Say — Word by Word

### Opening (5 seconds)
> "Now I'll show you the AI features powering this compliance tracker.
> We have 6 AI endpoints built with Groq's LLaMA-3.3-70b model."

---

## 🔴 Demo Interaction 1 — /recommend (30 seconds)

### Step 1 — Open Postman or the UI AI panel
### Step 2 — Send this EXACT input:
```
POST http://localhost:5000/recommend

{
  "input": "Cardholder data is stored in plain text in the MySQL database"
}
```

### Step 3 — What to Say While It Loads:
> "I'm asking our AI to recommend actions for this critical compliance issue —
> plain text cardholder data storage, which violates PCI-DSS Requirement 3.4."

### Step 4 — Read Output Aloud:
When response appears, read aloud:
> "The AI returned 3 prioritised recommendations:
> First — an Immediate Action to encrypt all stored cardholder data.
> Second — a Short-term Fix to implement key management procedures.
> Third — a Long-term Improvement to establish a data classification policy."

### Expected Output:
```json
{
  "recommendations": [
    {
      "action_type": "Immediate Action",
      "description": "Encrypt all stored cardholder data using AES-256",
      "priority": "Critical",
      "effort": "High",
      "pci_dss_requirement": "Requirement 3.4",
      "expected_outcome": "Cardholder data protected from unauthorized access"
    },
    {
      "action_type": "Short-term Fix",
      "description": "Implement key management procedures",
      "priority": "High",
      "effort": "Medium",
      "pci_dss_requirement": "Requirement 3.5",
      "expected_outcome": "Encryption keys properly managed"
    },
    {
      "action_type": "Long-term Improvement",
      "description": "Establish data classification and retention policy",
      "priority": "Medium",
      "effort": "Low",
      "pci_dss_requirement": "Requirement 3.1",
      "expected_outcome": "Clear data handling procedures in place"
    }
  ],
  "generated_at": "2026-05-09T00:00:00+00:00"
}
```

---

## 🔴 Demo Interaction 2 — /generate-report with SSE Streaming (35 seconds)

### Step 1 — Send this EXACT input:
```
POST http://localhost:5000/generate-report

{
  "input": "Unencrypted cardholder data in database, default passwords on payment terminals, firewall not reviewed in 9 months, no MFA on admin accounts"
}
```

### Step 2 — What to Say While Streaming:
> "Watch the report streaming live — each token appears in real time
> using Server-Sent Events. The React frontend reads this with EventSource.
> This gives users instant feedback instead of waiting for the full response."

### Step 3 — When Done Say:
> "The AI generated a complete executive compliance report with
> critical findings ranked by severity and prioritised remediation timeline."

### Expected Output Structure:
```json
{
  "title": "PCI-DSS Compliance Report for Payment Processing Organization",
  "executive_summary": "The organization faces critical compliance risks...",
  "overview": "Assessment covers payment systems and data handling...",
  "top_items": [
    {"item": "Unencrypted cardholder data", "severity": "Critical"},
    {"item": "Default passwords on terminals", "severity": "High"},
    {"item": "No MFA on admin accounts", "severity": "High"}
  ],
  "recommendations": [
    {"priority": "1", "action": "Encrypt all cardholder data", "timeline": "Immediate"},
    {"priority": "2", "action": "Change all default passwords", "timeline": "Within 30 days"},
    {"priority": "3", "action": "Implement MFA", "timeline": "Within 90 days"}
  ]
}
```

---

## 🔴 Demo Interaction 3 — RAG Query via /describe (20 seconds)

### Step 1 — Send this EXACT input:
```
POST http://localhost:5000/describe

{
  "input": "TLS 1.0 is still enabled on the payment gateway instead of TLS 1.2"
}
```

### Step 2 — What to Say:
> "This uses our RAG pipeline — Retrieval Augmented Generation.
> We have 62 chunks of PCI-DSS knowledge stored in ChromaDB.
> The AI retrieves relevant context before generating the response,
> making answers more accurate and PCI-DSS specific."

### Expected Output:
```json
{
  "title": "TLS Version Compliance",
  "description": "TLS 1.0 is deprecated and must be replaced with TLS 1.2 or higher...",
  "pci_dss_requirement": "Requirement 4.1",
  "risk_level": "Critical",
  "scope": "Payment gateway and network infrastructure team"
}
```

---

## 📌 Q&A Preparation — Answer These Without Notes

**Q: What does your AI service do?**
> "It's a Flask microservice with 6 endpoints that analyse PCI-DSS
> compliance items using Groq's LLaMA-3.3-70b model. It generates
> descriptions, recommendations, compliance reports, and document analysis."

**Q: What AI model are you using?**
> "We're using Groq API with LLaMA-3.3-70b-versatile model.
> Groq provides extremely fast inference — responses in under 3 seconds."

**Q: What is RAG?**
> "RAG stands for Retrieval Augmented Generation. Instead of relying
> only on the AI model's training data, we store 62 chunks of PCI-DSS
> knowledge in ChromaDB vector database. When a query comes in,
> we retrieve the most relevant chunks and inject them as context
> into the AI prompt. This makes responses much more accurate
> and PCI-DSS specific."

**Q: What security measures did you implement?**
> "We have input validation on all endpoints — empty inputs return 400.
> Rate limiting blocks IPs exceeding 30 requests per minute.
> No secrets are hardcoded — all keys are in environment variables.
> We also have prompt injection protection in the sanitisation middleware."

**Q: What if Groq API goes down during demo?**
> "We have 3-retry with exponential backoff. If all retries fail,
> the service returns a fallback response with is_fallback: true
> instead of crashing. The Java backend handles this gracefully."

---

## 📌 Backup Plan — If Groq API Fails on Demo Day

Take screenshots of these outputs NOW and save as backup:

| Screenshot | What to Capture |
|---|---|
| `recommend_output.png` | /recommend response for plain text data |
| `report_output.png` | /generate-report full response |
| `describe_output.png` | /describe response for TLS issue |
| `health_output.png` | /health showing 62 chunks |

---

## 📌 Technical Numbers to Remember

| Fact | Value |
|---|---|
| AI Model | LLaMA-3.3-70b-versatile |
| AI Provider | Groq API |
| Vector DB | ChromaDB |
| Knowledge chunks | 62 |
| Knowledge documents | 10 |
| AI endpoints | 6 |
| Pytest tests | 10 (all passing) |
| Final QA checks | 52/52 passed |
| Port | 5000 |
| Cache TTL | 15 minutes |
| Max batch items | 20 |

---

## 📌 Practice Schedule

| When | What |
|---|---|
| Day 16 today | Read script 5 times |
| Day 17 | Full dry run with live system |
| Day 18 | Solo practice without notes |
| Day 19 | Final confidence check |
| Day 20 | DEMO DAY — you're ready! |