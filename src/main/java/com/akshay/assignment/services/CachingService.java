package com.akshay.assignment.services;

import com.akshay.assignment.models.BaseEntity;
import com.akshay.assignment.repositories.BaseEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Service
public class CachingService {

	// Field Injection for repository and maxSize
	@Value("${cachingService.maxSize}") // Inject maxSize from properties
	private int maxSize;

	private final Map<Long, BaseEntity> cache = new HashMap<>();
	private final LinkedHashMap<Long, Long> accessOrder = new LinkedHashMap<>(16, 0.75f, true); // LRU eviction
	private Optional<BaseEntityRepository> database;

	// Autowired for repository injection
	@Autowired
	public void setDatabase(Optional<BaseEntityRepository> database) {
		this.database = database;
	}

	// Add entity to cache, evict if cache exceeds maxSize
	public synchronized void add(BaseEntity entity) {
		try {
			if (cache.size() >= maxSize) {
				evictLeastUsed(); // Evict least recently used entity
			}
			cache.put(entity.getId(), entity);
			accessOrder.put(entity.getId(), System.nanoTime()); // Update access time
			database.ifPresent(db -> db.save(entity)); // Optionally save to database
			log.info("Entity added: {}", entity);
		} catch (Exception e) {
			log.error("Error adding entity to cache: {}", e.getMessage());
		}
	}

	// Remove entity from cache and database by ID
	public synchronized boolean remove(Long id) {
		try {
			if (cache.containsKey(id)) {
				cache.remove(id);
				accessOrder.remove(id);
				database.ifPresent(db -> db.deleteById(id)); // Remove from DB as well
				log.info("Entity removed: {}", id);
				return true;
			} else {
				log.warn("Entity with ID {} not found in cache.", id);
				return false; // Return false if entity isn't in the cache
			}
		} catch (Exception e) {
			log.error("Error removing entity from cache: {}", e.getMessage());
			return false; // Return false if there's an error
		}
	}

	// Remove all entities from cache and database
	public synchronized void removeAll() {
		try {
			cache.clear();
			accessOrder.clear();
			database.ifPresent(BaseEntityRepository::deleteAll); // Clear DB as well
			log.info("All entities removed from cache and database.");
		} catch (Exception e) {
			log.error("Error clearing all entities from cache and database: {}", e.getMessage());
		}
	}

	// Fetch entity from cache or database
	public synchronized BaseEntity get(Long id) {
		try {
			if (cache.containsKey(id)) {
				log.info("Fetching entity from cache: {}", id);
				accessOrder.put(id, System.nanoTime()); // Update access time when accessed
				return cache.get(id);
			}
			// If not in cache, fetch from DB and add to cache
			return database.flatMap(db -> db.findById(id)).map(entity -> {
				add(entity); // Add to cache if found in DB
				return entity;
			}).orElse(null); // Return null if not found
		} catch (Exception e) {
			log.error("Error fetching entity: {}", e.getMessage());
			return null;
		}
	}

	// Clear the entire cache
	public synchronized void clear() {
		try {
			cache.clear();
			accessOrder.clear();
			log.info("Cache cleared.");
		} catch (Exception e) {
			log.error("Error clearing cache: {}", e.getMessage());
		}
	}

	// Evict the least recently used item from the cache
	private void evictLeastUsed() {
		try {
			// Get the key with the least recent access time
			Long leastUsedId = accessOrder.entrySet().stream()
					.min(Map.Entry.comparingByValue())
					.map(Map.Entry::getKey)
					.orElse(null);

			if (leastUsedId != null) {
				cache.remove(leastUsedId);
				accessOrder.remove(leastUsedId);
				log.info("Evicted least used entity: {}", leastUsedId);
			}
		} catch (Exception e) {
			log.error("Error evicting least used entity: {}", e.getMessage());
		}
	}
}
