import os
import time
import hashlib
import logging
from groq import Groq
from dotenv import load_dotenv

load_dotenv()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class GroqClient:
    _instance = None  # Singleton instance

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._initialized = False
        return cls._instance

    def __init__(self):
        if self._initialized:
            return
        api_key = os.getenv("GROQ_API_KEY")
        if not api_key:
            raise ValueError("GROQ_API_KEY not found in .env file")
        self.client = Groq(api_key=api_key)
        self.model = "llama-3.3-70b-versatile"
        self._cache = {}
        self._cache_hits = 0
        self._cache_misses = 0
        self.CACHE_TTL = 900  # 15 minutes
        self._initialized = True
        logger.info("GroqClient singleton initialized")

    def _get_cache_key(self, prompt: str) -> str:
        return hashlib.sha256(prompt.encode()).hexdigest()

    def _get_from_cache(self, key: str):
        if key in self._cache:
            entry = self._cache[key]
            if time.time() - entry["timestamp"] < self.CACHE_TTL:
                self._cache_hits += 1
                logger.info(f"Cache HIT — hits:{self._cache_hits} misses:{self._cache_misses}")
                return entry["value"]
            else:
                del self._cache[key]
        self._cache_misses += 1
        return None

    def _set_cache(self, key: str, value: str):
        self._cache[key] = {
            "value": value,
            "timestamp": time.time()
        }

    def get_cache_stats(self):
        return {
            "hits": self._cache_hits,
            "misses": self._cache_misses,
            "cached_items": len(self._cache)
        }

    def call(self, prompt: str, temperature: float = 0.3, max_tokens: int = 1000):
        cache_key = self._get_cache_key(prompt)
        cached = self._get_from_cache(cache_key)
        if cached:
            return cached

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
                self._set_cache(cache_key, result)
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