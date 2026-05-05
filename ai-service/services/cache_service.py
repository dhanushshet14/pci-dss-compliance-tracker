import time

class CacheService:
    def __init__(self):
        self.store = {}
        self.ttl = 900  # 15 minutes
        self.hits = 0
        self.misses = 0

    def make_key(self, question):
        return question.strip().lower()

    def get(self, question):
        key = self.make_key(question)

        if key in self.store:
            value, expiry = self.store[key]

            if time.time() < expiry:
                self.hits += 1
                return value
            else:
                del self.store[key]

        self.misses += 1
        return None

    def set(self, question, value):
        key = self.make_key(question)
        expiry = time.time() + self.ttl
        self.store[key] = (value, expiry)

    def hit(self):
        self.hits += 1

    def miss(self):
        self.misses += 1

    def stats(self):
        return {
            "hits": self.hits,
            "misses": self.misses,
            "ttl_seconds": self.ttl
        }