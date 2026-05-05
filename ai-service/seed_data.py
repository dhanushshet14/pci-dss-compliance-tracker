from services.chroma_service import ChromaService

print("Starting seed...")

db = ChromaService()

print("Adding data...")

db.add_data("Cardholder data must be encrypted using strong cryptography like AES-256", "req1")
print("Added 1")

db.add_data("Use firewalls to protect cardholder data from unauthorized access", "req2")
print("Added 2")

db.add_data("Access to card data must be restricted based on business need-to-know", "req3")
print("Added 3")

db.add_data("Do not store sensitive authentication data after authorization", "req4")
print("Added 4")

print("✅ Data inserted successfully!")