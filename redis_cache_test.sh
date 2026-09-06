#!/bin/bash

echo "Starting Redis cache hit/miss test..."
echo "------------------------------------------------"

# Set some initial keys for the Medical Platform entities and API resources
for i in {1..5}; do
    docker exec redis-server redis-cli SET "doctor:$i" "Data for doctor $i"
    echo "✓ Set doctor:$i"
done

for i in {1..3}; do
    docker exec redis-server redis-cli SET "patient:$i" "Data for patient $i"
    echo "✓ Set patient:$i"
done

echo ""
echo "------------------------------------------------"
echo "Simulating cache hits and misses..."
echo "------------------------------------------------"
echo ""

# Generate random access patterns
for i in {1..30}; do
    if [ $((RANDOM % 10)) -lt 7 ]; then
        # Cache HIT (70% probability)
        entity=$( [ $((RANDOM % 2)) -eq 0 ] && echo "doctor" || echo "patient" )
        key="${entity}:$((RANDOM % 5 + 1))"
        result=$(docker exec redis-server redis-cli GET "$key")
        endpoint="/api/${entity}s/${key#*:}"
        echo "HIT  → $endpoint: $result"
    else
        # Cache MISS (30% probability)
        entity=$( [ $((RANDOM % 2)) -eq 0 ] && echo "doctor" || echo "patient" )
        key="${entity}:$((RANDOM % 100 + 100))"
        result=$(docker exec redis-server redis-cli GET "$key")
        endpoint="/api/${entity}s/${key#*:}"
        echo "MISS → $endpoint: (nil)"
    fi
    sleep 0.5
done

echo ""
echo "================================================"
echo "REDIS STATISTICS"
echo "================================================"
docker exec redis-server redis-cli INFO stats | grep -E "keyspace_hits|keyspace_misses|total_commands"