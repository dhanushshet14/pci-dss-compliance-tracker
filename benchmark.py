import requests
import time
import statistics

BASE_URL = "http://localhost:5000"

ENDPOINTS = [
    ("/describe", {"input": "Weak firewall rules"}),
    ("/categorise", {"input": "Weak firewall rules"}),
    ("/recommend", {"input": "Weak firewall rules"}),
    ("/generate-report", {"input": "Weak firewall rules and no monitoring"}),
    ("/query", {"question": "Why are weak firewall rules dangerous?"}),
]

NUM_REQUESTS = 50


def measure_endpoint(path, payload):
    times = []

    for i in range(NUM_REQUESTS):
        start = time.time()

        try:
            res = requests.post(BASE_URL + path, json=payload, timeout=5)
        except Exception as e:
            print(f"Error: {e}")
            continue

        duration = (time.time() - start) * 1000  # ms
        times.append(duration)

    if not times:
        return None

    times.sort()

    p50 = statistics.median(times)
    p95 = times[int(0.95 * len(times)) - 1]
    p99 = times[int(0.99 * len(times)) - 1]

    return p50, p95, p99


def main():
    print("Running benchmarks...\n")

    for path, payload in ENDPOINTS:
        print(f"Testing {path} ...")

        result = measure_endpoint(path, payload)

        if result:
            p50, p95, p99 = result
            print(f"p50: {p50:.2f} ms")
            print(f"p95: {p95:.2f} ms")
            print(f"p99: {p99:.2f} ms\n")
        else:
            print("Failed to measure\n")


if __name__ == "__main__":
    main()