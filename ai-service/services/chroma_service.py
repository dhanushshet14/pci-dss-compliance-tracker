class ChromaService:
    def __init__(self):
        print("Initializing simple in-memory DB...")

        # Store as dictionary (IMPORTANT)
        self.data = {}

        # ✅ Add default PCI data
        self.add_data("PCI DSS Requirement 3 protects stored cardholder data using encryption", "req3")
        self.add_data("PCI DSS Requirement 1 is about firewalls and network security", "req1")
        self.add_data("Sensitive authentication data includes CVV, PIN, and full track data", "sad1")
        self.add_data("PCI DSS compliance steps include securing network, encrypting data, and monitoring access", "steps1")
        self.add_data("Cardholder data should be stored securely using encryption and access control", "storage1")

        print("DB ready!")

    def add_data(self, text, doc_id):
        print(f"Adding: {doc_id}")
        self.data[doc_id] = text.lower()
        print(f"Added: {doc_id}")

    def query(self, question):
        question = question.lower()

        # Remove useless words
        stopwords = {
            "what", "is", "the", "in", "of", "how", "to",
            "are", "why", "a", "an", "dss", "pci"
        }

        question_words = [w for w in question.split() if w not in stopwords]

        best_score = 0
        best_doc = None

        for doc_id, text in self.data.items():
            score = 0

            # ✅ keyword match
            score += sum(1 for word in question_words if word in text)

            # ✅ exact phrase boost
            if question in text:
                score += 5

            # ✅ requirement boost
            if "requirement 1" in question and "requirement 1" in text:
                score += 5
            if "requirement 3" in question and "requirement 3" in text:
                score += 5

            # ✅ topic boosts
            if "firewall" in question and "firewall" in text:
                score += 3
            if "encryption" in question and "encryption" in text:
                score += 3
            if "authentication" in question and "authentication" in text:
                score += 3
            if "store" in question and "store" in text:
                score += 2
            if "compliance" in question and "compliance" in text:
                score += 2

            # pick best
            if score > best_score:
                best_score = score
                best_doc = text

        if best_score == 0:
            return []

        return [best_doc]