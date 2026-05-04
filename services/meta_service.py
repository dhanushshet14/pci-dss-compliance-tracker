import time

def build_meta(start_time, tokens_used=0, cached=False):
    response_time = int((time.time() - start_time) * 1000)

    return {
        "confidence": round(0.7 + (tokens_used / 1000), 2) if tokens_used else 0.8,
        "model_used": "llama-3.3-70b",
        "tokens_used": tokens_used,
        "response_time_ms": response_time,
        "cached": cached
    }

