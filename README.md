# CachingServiceDemoAssignment

This generated using Springboot Initializer for the base project and then customized to meet the assignment requirements.

## Overview

`CachingService` is a Java service that provides an in-memory caching mechanism for entities extending `BaseEntity`. The service uses a Least Recently Used (LRU) eviction strategy to ensure the cache doesn't grow too large. This service aims to reduce database calls by caching frequently accessed entities and synchronizing with the database when necessary.

The service supports operations such as adding, removing, and fetching entities from both the cache and the database. It can also clear the cache and evict the least recently used entity when the cache size exceeds a predefined limit.

## Features

- **Caching:** The service stores entities in memory, reducing the number of database queries by returning cached entities when possible.
- **Database Synchronization:** Entities are optionally saved to the database, allowing for persistence across application restarts.
- **LRU Eviction:** When the cache reaches its maximum size, the service will automatically evict the least recently used entity to make room for new ones.
- **Cache Management:** The service provides methods to add, remove, get, and clear entities from the cache. It also supports clearing all cached entities at once.

# How it Works
## LRU Eviction
The CachingService uses an LRU (Least Recently Used) eviction strategy. When the cache size exceeds the predefined limit, the service will remove the entity that has been accessed least recently to make space for new entries. The access order is tracked using a LinkedHashMap, which ensures that the eviction happens in the correct order.

## Integration with Database (I have used a demo H2 in-memory database)
Entities are optionally saved to the database when added to the cache. If an entity is not found in the cache when requested, the service will query the database and add it to the cache for future use. This integration helps reduce redundant database queries, especially for frequently accessed entities.

## Max Cache Size
The maximum size of the cache is configurable through application properties. The cache will automatically evict entries once this size is reached.

## Configuration
Cache Size
The maximum cache size can be configured in your application.properties
`cachingService.maxSize=100`

This example sets the maximum size of the cache to 100 entities. If the cache exceeds this size, the least recently used entities will be evicted.

---

## Test Cases and Edge Cases

The `CachingService` has been thoroughly tested to ensure its robustness and correctness. Below is a summary of the test cases and edge cases covered:

### Basic Functionality
- **Add Entity**: Verifies that entities can be added to the cache and saved to the database.
- **Remove Entity**: Ensures entities can be removed from the cache and database.
- **Get Entity from Cache**: Confirms that entities can be retrieved from the cache if they exist.
- **Get Entity from Database**: Ensures that entities are fetched from the database if not found in the cache.

### Cache Management
- **Clear Cache**: Tests that the cache can be cleared completely, removing all entities.
- **Evict Least Recently Used**: Verifies that the least recently used entity is evicted when the cache size exceeds the limit.

### Edge Cases
- **Add Null Entity**: Ensures that adding a null entity throws an `IllegalArgumentException`.
- **Evict from Empty Cache**: Confirms that evicting from an empty cache does not throw exceptions.
- **Add Duplicate Entities**: Verifies that adding the same entity multiple times does not create duplicates in the cache.
- **Cache Size Limit**: Tests that the cache respects the maximum size limit and evicts the least recently used entity when the limit is exceeded.
- **Get Non-Existent Entity**: Ensures that fetching an entity not present in the cache or database returns `null`.

### Additional Scenarios
- **Reflection-Based Testing**: Used reflection to verify the internal state of the cache and access order for advanced validation.
