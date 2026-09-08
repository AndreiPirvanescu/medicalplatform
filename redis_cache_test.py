#!/usr/bin/env python3
import redis
import time
import random

# Connect to Redis
r = redis.Redis(host='localhost', port=6379, decode_responses=True)

print("Starting Redis cache hit/miss test...")
print("-" * 50)

# Set some initial keys for the Medical Platform entities and API resources
keys = [
    'doctor:1', 'doctor:2', 'doctor:3', 'doctor:4', 'doctor:5',
    'patient:1', 'patient:2', 'patient:3'
]
for key in keys:
    r.set(key, f"Data for {key}")
    print(f"✓ Set {key}")

print("\n" + "-" * 50)
print("Simulating cache hits and misses...")
print("-" * 50 + "\n")


def api_route_for_key(key: str) -> str:
    entity, entity_id = key.split(":")
    return f"/api/{entity}s/{entity_id}"


# Simulate random access patterns
for i in range(50):
    # 70% cache hits, 30% cache misses
    if random.random() < 0.7:
        # Cache HIT - accessing existing key
        key = random.choice(keys)
        value = r.get(key)
        print(f"HIT  → {api_route_for_key(key)}: {value}")
    else:
        # Cache MISS - accessing non-existent key
        entity = random.choice(['doctor', 'patient'])
        key = f"{entity}:{random.randint(100, 200)}"
        value = r.get(key)
        print(f"MISS → {api_route_for_key(key)}: {value}")

    time.sleep(0.5)

# Get statistics
info = r.info('stats')
print("\n" + "=" * 50)
print("REDIS STATISTICS")
print("=" * 50)
print(f"Total Commands: {info['total_commands_processed']}")
print(f"Keyspace Hits: {info.get('keyspace_hits', 0)}")
print(f"Keyspace Misses: {info.get('keyspace_misses', 0)}")

if info.get('keyspace_hits', 0) > 0 or info.get('keyspace_misses', 0) > 0:
    total = info['keyspace_hits'] + info['keyspace_misses']
    hit_rate = (info['keyspace_hits'] / total) * 100
    print(f"Hit Rate: {hit_rate:.2f}%")