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
    _instance = None

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
        self.CACHE_TTL = 900

        self._initialized = True
        logger.info("GroqClient singleton initialized")

    def _get_cache_key(self, prompt: str) -> str:
        return hashlib.sha256(prompt.encode("utf-8")).hexdigest()

    def _get_from_cache(self, key: str):
        if key in self._cache:
            entry = self._cache[key]

            if time.time() - entry["timestamp"] < self.CACHE_TTL:
                self._cache_hits += 1
                logger.info(
                    f"Cache HIT — hits:{self._cache_hits} misses:{self._cache_misses}"
                )
                return entry["value"]

            del self._cache[key]

        self._cache_misses += 1
        return None

    def _set_cache(self, key: str, value: dict):
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

    def call(self, prompt, temperature=0.3):
        start = time.time()
        cache_key = self._get_cache_key(prompt)

        cached = self._get_from_cache(cache_key)
        if cached:
            cached["meta"]["cached"] = True
            cached["meta"]["response_time_ms"] = int((time.time() - start) * 1000)
            return cached

        try:
            logger.info("Groq API call started")

            response = self.client.chat.completions.create(
                model=self.model,
                messages=[
                    {
                        "role": "system",
                        "content": (
                            "You are a PCI-DSS compliance expert. "
                            "Always respond in valid JSON only."
                        )
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                temperature=temperature,
                max_tokens=1000
            )

            content = response.choices[0].message.content
            tokens = response.usage.total_tokens if response.usage else 0
            duration = int((time.time() - start) * 1000)

            result = {
                "data": content,
                "meta": {
                    "confidence": 0.9,
                    "model_used": self.model,
                    "tokens_used": tokens,
                    "response_time_ms": duration,
                    "cached": False,
                    "is_fallback": False
                }
            }

            self._set_cache(cache_key, result)
            return result

        except Exception as e:
            logger.error(f"Groq API error: {str(e)}")

            duration = int((time.time() - start) * 1000)

            return {
                "data": {
                    "summary": "Unable to generate response at the moment. Please try again later.",
                    "risks": [],
                    "recommendations": ["Retry request after some time"]
                },
                "meta": {
                    "confidence": 0.2,
                    "model_used": self.model,
                    "tokens_used": 0,
                    "response_time_ms": duration,
                    "cached": False,
                    "is_fallback": True
                }
            }

    def call_stream(self, prompt: str, temperature: float = 0.3):
        try:
            logger.info("Groq streaming API call started")

            stream = self.client.chat.completions.create(
                model=self.model,
                messages=[
                    {
                        "role": "system",
                        "content": (
                            "You are a PCI-DSS compliance expert. "
                            "Always respond in valid JSON only."
                        )
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