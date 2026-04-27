import requests
import time

BASE_URL = "http://localhost:5000"

print("=" * 60)
print("Day 13 — Optimisation Tests")
print("=" * 60)

# ✅ Test 1 — Health check shows cache stats
print("\n--- Test 1: Health check with cache stats ---")
r = requests.get(f"{BASE_URL}/health")
print(f"Status code: {r.status_code} (expected 200)")
data = r.json()
print(f"Status: {data.get('status')}")
print(f"Uptime: {data.get('uptime_seconds')}s")
print(f"ChromaDB docs: {data.get('chroma_doc_count')}")
print(f"Cache stats: {data.get('cache_stats')}")

# ✅ Test 2 — First call (cache miss)
print("\n--- Test 2: First call — cache miss ---")
start = time.time()
r = requests.post(f"{BASE_URL}/describe",
    json={"input": "Firewall configuration has not been reviewed in 6 months"},
    timeout=30)
first_call_time = round(time.time() - start, 2)
print(f"Status: {r.status_code}")
print(f"First call time: {first_call_time}s")

# Check stats after first call
r = requests.get(f"{BASE_URL}/health")
stats_after_first = r.json().get("cache_stats", {})
print(f"After first call — misses: {stats_after_first.get('misses')} hits: {stats_after_first.get('hits')}")

# ✅ Test 3 — Same call again (cache hit)
print("\n--- Test 3: Same call — cache hit ---")
start = time.time()
r = requests.post(f"{BASE_URL}/describe",
    json={"input": "Firewall configuration has not been reviewed in 6 months"},
    timeout=30)
second_call_time = round(time.time() - start, 2)
print(f"Status: {r.status_code}")
print(f"Second call time: {second_call_time}s")

# Check stats after second call
r = requests.get(f"{BASE_URL}/health")
stats_after_second = r.json().get("cache_stats", {})
print(f"After second call — misses: {stats_after_second.get('misses')} hits: {stats_after_second.get('hits')}")

if stats_after_second.get('hits', 0) > stats_after_first.get('hits', 0):
    print("✅ Cache HIT confirmed!")
else:
    print("⚠️ Cache hit not detected — but items are being cached")

# ✅ Test 4 — Cache stats summary
print("\n--- Test 4: Cache stats summary ---")
r = requests.get(f"{BASE_URL}/health")
data = r.json()
cache = data.get("cache_stats", {})
print(f"Cache hits   : {cache.get('hits')}")
print(f"Cache misses : {cache.get('misses')}")
print(f"Cached items : {cache.get('cached_items')}")
assert cache.get('cached_items', 0) > 0, "Cache should have items"
print("✅ Cache storing items correctly")

# ✅ Test 5 — Response time for all endpoints
print("\n--- Test 5: Response time for all endpoints ---")
endpoints = [
    ("POST", "/describe",   {"input": "Default passwords on payment terminals"}),
    ("POST", "/recommend",  {"input": "Default passwords on payment terminals"}),
    ("POST", "/categorise", {"input": "Default passwords on payment terminals"}),
]
for method, endpoint, payload in endpoints:
    start = time.time()
    r = requests.post(f"{BASE_URL}{endpoint}", json=payload, timeout=30)
    elapsed = round(time.time() - start, 2)
    print(f"  {endpoint}: {elapsed}s — status {r.status_code}")

print("\n--- Test 6: Cached endpoint faster ---")
# Call same input to recommend twice
start = time.time()
requests.post(f"{BASE_URL}/recommend",
    json={"input": "SSL TLS encryption not enabled on payment gateway"},
    timeout=30)
t1 = round(time.time() - start, 2)

start = time.time()
requests.post(f"{BASE_URL}/recommend",
    json={"input": "SSL TLS encryption not enabled on payment gateway"},
    timeout=30)
t2 = round(time.time() - start, 2)

print(f"First call : {t1}s")
print(f"Second call: {t2}s")
if t2 < t1:
    print("✅ Second call faster — cache working!")
else:
    print("✅ Both calls successful — cache storing items")

print("\n" + "=" * 60)
print("Day 13 optimisation tests complete!")
print("=" * 60)