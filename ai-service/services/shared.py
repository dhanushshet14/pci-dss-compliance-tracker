# Shared singleton instances across all routes
from services.groq_client import GroqClient

# One shared instance for entire app
groq_client = GroqClient()