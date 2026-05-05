from services.groq_client import GroqClient

client = GroqClient()

response = client.generate("Explain PCI-DSS in one line")
print(response)