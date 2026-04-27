import os
import json
import time
import logging
from groq import Groq
from dotenv import load_dotenv

load_dotenv()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class GroqClient:
    def __init__(self):
        api_key = os.getenv("GROQ_API_KEY")
        if not api_key:
            raise ValueError("GROQ_API_KEY not found in .env file")
        self.client = Groq(api_key=api_key)
        self.model = "llama-3.3-70b-versatile"

    def call(self, prompt: str, temperature: float = 0.3, max_tokens: int = 1000):
        retries = 3
        for attempt in range(retries):
            try:
                logger.info(f"Groq API call attempt {attempt + 1}")
                response = self.client.chat.completions.create(
                    model=self.model,
                    messages=[
                        {
                            "role": "system",
                            "content": "You are a PCI-DSS compliance expert. Always respond in valid JSON only."
                        },
                        {
                            "role": "user",
                            "content": prompt
                        }
                    ],
                    temperature=temperature,
                    max_tokens=max_tokens
                )
                result = response.choices[0].message.content
                logger.info("Groq API call successful")
                return result

            except Exception as e:
                logger.error(f"Groq API error on attempt {attempt + 1}: {str(e)}")
                if attempt < retries - 1:
                    time.sleep(2 ** attempt)
                else:
                    logger.error("All retries failed")
                    return None

    def call_stream(self, prompt: str, temperature: float = 0.3):
        try:
            logger.info("Groq streaming API call started")
            stream = self.client.chat.completions.create(
                model=self.model,
                messages=[
                    {
                        "role": "system",
                        "content": "You are a PCI-DSS compliance expert. Always respond in valid JSON only."
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                temperature=temperature,
                max_tokens=1000,
                stream=True
            )
            for chunk in stream:
                token = chunk.choices[0].delta.content
                if token:
                    yield token
        except Exception as e:
            logger.error(f"Groq streaming error: {str(e)}")
            yield None