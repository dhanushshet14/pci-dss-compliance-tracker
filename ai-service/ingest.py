import chromadb

print("Starting ingestion...")

# new API
client = chromadb.PersistentClient(path="./chroma_db")

# reset collection
try:
    client.delete_collection("pci_dss")
except:
    pass

collection = client.get_or_create_collection("pci_dss")

documents = [
    "PCI DSS Requirement 1: Install and maintain firewall configuration to protect cardholder data.",
    "PCI DSS Requirement 2: Do not use vendor-supplied defaults for system passwords.",
    "PCI DSS Requirement 3: Protect stored cardholder data.",
    "PCI DSS Requirement 4: Encrypt transmission of cardholder data across open networks.",
    "PCI DSS Requirement 5: Protect all systems against malware.",
    "PCI DSS Requirement 6: Develop and maintain secure systems and applications."
]

print("Adding documents...")

collection.add(
    documents=documents,
    ids=[str(i) for i in range(len(documents))]
)

print("✅ Done! PCI DSS data inserted.")