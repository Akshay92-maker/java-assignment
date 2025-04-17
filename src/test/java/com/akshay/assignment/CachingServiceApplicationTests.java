package com.akshay.assignment;

import com.akshay.assignment.models.BaseEntity;
import com.akshay.assignment.repositories.BaseEntityRepository;
import com.akshay.assignment.services.CachingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CachingServiceApplicationTests {

	@InjectMocks
	private CachingService cachingService;

	@Mock
	private BaseEntityRepository mockRepository;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		cachingService.setDatabase(Optional.of(mockRepository));
	}

	// This test ensures that the Spring context is loaded successfully
	@Test
	void contextLoads() {
	}

	@Test
	void testAddEntity() {
		// Create a mock entity
		BaseEntity entity = new BaseEntity();
		entity.setId(1L);

		// Perform add operation
		cachingService.add(entity);

		// Verify that the entity is saved to the repository
		verify(mockRepository, times(1)).save(entity);
	}

	@Test
	void testRemoveEntity() {
		// Create a mock entity
		BaseEntity entity = new BaseEntity();
		entity.setId(1L);

		// Mock the repository to return the entity
		when(mockRepository.findById(1L)).thenReturn(Optional.of(entity));

		// Perform remove operation
		boolean result = cachingService.remove(1L);

		// Verify the entity was deleted from the repository
		verify(mockRepository, times(1)).deleteById(1L);

		// Assert the result
		assertTrue(result, "Entity should be removed successfully");
	}

	@Test
	void testGetEntityFromCache() {
		// Create a mock entity
		BaseEntity entity = new BaseEntity();
		entity.setId(1L);

		// Add the entity to the cache manually for this test
		cachingService.add(entity);

		// Fetch the entity from the cache
		BaseEntity fetchedEntity = cachingService.get(1L);

		// Assert the fetched entity is the same as the one added
		assertNotNull(fetchedEntity, "Entity should be found in cache");
		assertEquals(entity, fetchedEntity, "Fetched entity should match the added entity");
	}

	@Test
	void testGetEntityFromDatabase() {
		// Create a mock entity
		BaseEntity entity = new BaseEntity();
		entity.setId(1L);

		// Mock the repository to return the entity
		when(mockRepository.findById(1L)).thenReturn(Optional.of(entity));

		// Perform get operation (this will go to the database)
		BaseEntity fetchedEntity = cachingService.get(1L);

		// Verify that the repository was queried for the entity
		verify(mockRepository, times(1)).findById(1L);

		// Assert the fetched entity
		assertNotNull(fetchedEntity, "Entity should be fetched from the database");
		assertEquals(entity, fetchedEntity, "Fetched entity should match the entity in DB");
	}

	@Test
	void testClearCache() throws NoSuchFieldException, IllegalAccessException {
		// Create a mock entity and add to cache
		BaseEntity entity1 = new BaseEntity();
		BaseEntity entity2 = new BaseEntity();
		BaseEntity entity3 = new BaseEntity();

		entity1.setId(1L);
		entity2.setId(2L);
		entity3.setId(3L);
		cachingService.add(entity1);
		cachingService.add(entity2);
		cachingService.add(entity3);

		// Clear the cache
		cachingService.clear();

		// Use reflection to access the private 'cache' and 'accessOrder' fields
		try {
			try {
				Field cacheField = CachingService.class.getDeclaredField("cache");
				cacheField.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<Long, BaseEntity> cache = (Map<Long, BaseEntity>) cacheField.get(cachingService);

				// Verify that entity1 is evicted and entity2 and entity3 remain
				assertFalse(cache.containsKey(1L), "Entity1 should be evicted from the cache");
				assertFalse(cache.containsKey(2L), "Entity2 should remain in the cache");
				assertFalse(cache.containsKey(3L), "Entity3 should remain in the cache");
				assertEquals(0, cache.size(), "Cache should be cleared");
			} catch (NoSuchFieldException e) {
				fail("Reflection error: " + e.getMessage());
			}

		} catch (IllegalAccessException e) {
			fail("Reflection error: " + e.getMessage());
		}

		Field accessOrderField = CachingService.class.getDeclaredField("accessOrder");
		accessOrderField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<Long, Long> accessOrder = (Map<Long, Long>) accessOrderField.get(cachingService);

		// Verify that the cache is cleared
		assertEquals(0, accessOrder.size(), "Access order should be cleared");
	}

	@Test
	void testEvictLeastUsed()
			throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		// Create mock entities and add them to the cache
		BaseEntity entity1 = new BaseEntity();
		entity1.setId(1L);
		BaseEntity entity2 = new BaseEntity();
		entity2.setId(2L);

		cachingService.add(entity1);
		cachingService.add(entity2);

		// Simulate access to entity1 to make it the least recently used
		cachingService.get(1L);

		// Use reflection to access the private 'evictLeastUsed' method
		Method evictMethod = CachingService.class.getDeclaredMethod("evictLeastUsed");
		evictMethod.setAccessible(true);

		// Invoke the private method
		evictMethod.invoke(cachingService);

		// Use reflection to access the private 'cache' field
		Field cacheField = CachingService.class.getDeclaredField("cache");
		cacheField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<Long, BaseEntity> cache = (Map<Long, BaseEntity>) cacheField.get(cachingService);

		// Verify that entity2 is evicted
		assertFalse(cache.containsKey(2L), "Entity2 should be evicted from cache");
	}

	@Test
	void testAddNullEntity() {
		// Attempt to add a null entity
		BaseEntity entity= new BaseEntity();
		entity.setId(null);
		assertThrows(IllegalArgumentException.class, () -> cachingService.add(entity),
				"Adding null entity should throw an exception");
	}

	@Test
	void testEvictFromEmptyCache() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		// Use reflection to access the private 'evictLeastUsed' method
		Method evictMethod = CachingService.class.getDeclaredMethod("evictLeastUsed");
		evictMethod.setAccessible(true);

		// Invoke the private method on an empty cache
		evictMethod.invoke(cachingService);

		// Verify no exceptions are thrown and cache remains empty

		try {
			Field cacheField = CachingService.class.getDeclaredField("cache");
			cacheField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<Long, BaseEntity> cache = (Map<Long, BaseEntity>) cacheField.get(cachingService);

			assertEquals(0, cache.size(), "Cache should remain empty after eviction attempt");

		} catch (NoSuchFieldException e) {
			fail("Reflection error: " + e.getMessage());
		}

	}

	@Test
	void testAddDuplicateEntities() throws NoSuchFieldException, IllegalAccessException {
		// Create a mock entity
		BaseEntity entity = new BaseEntity();
		entity.setId(1L);

		// Add the entity twice
		cachingService.add(entity);
		cachingService.add(entity);

		// Use reflection to access the private 'cache' field
		Field cacheField = CachingService.class.getDeclaredField("cache");
		cacheField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<Long, BaseEntity> cache = (Map<Long, BaseEntity>) cacheField.get(cachingService);

		// Verify that the cache contains only one instance of the entity
		assertEquals(1, cache.size(), "Cache should contain only one instance of the entity");
		assertTrue(cache.containsKey(1L), "Entity should be present in the cache");
		assertEquals(entity, cache.get(1L), "The cached entity should match the added entity");
	}

	@Test
	void testCacheSizeLimit() throws NoSuchFieldException, IllegalAccessException {
		// Assuming the cache has a size limit of 5
		Field maxSizeField = CachingService.class.getDeclaredField("maxSize");
		maxSizeField.setAccessible(true);
		maxSizeField.set(cachingService, 5);

		BaseEntity entity1 = new BaseEntity();
		entity1.setId(1L);
		BaseEntity entity2 = new BaseEntity();
		entity2.setId(2L);
		BaseEntity entity3 = new BaseEntity();
		entity3.setId(3L);
		BaseEntity entity4 = new BaseEntity();
		entity4.setId(4L);
		BaseEntity entity5 = new BaseEntity();
		entity5.setId(5L);
		BaseEntity entity6 = new BaseEntity();
		entity6.setId(6L);

		// Add entities to exceed the cache limit
		cachingService.add(entity1);
		cachingService.add(entity2);
		cachingService.add(entity3);
		cachingService.add(entity4);
		cachingService.add(entity5);
		cachingService.add(entity6);

		// Use reflection to access the private 'cache' field
		Field cacheField = CachingService.class.getDeclaredField("cache");
		cacheField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<Long, BaseEntity> cache = (Map<Long, BaseEntity>) cacheField.get(cachingService);
		// The cache size should not exceed 5
		assertEquals(5, cache.size(), "Cache size should not exceed the maximum limit");

		// Check that the last two added entities remain
		assertTrue(cache.containsKey(5L), "Entity5 should remain in the cache");
		assertTrue(cache.containsKey(6L), "Entity6 should remain in the cache");

		// Ensure early entries are evicted
		assertFalse(cache.containsKey(1L), "Entity1 should be evicted from the cache");

	}


	@Test
	void testGetNonExistentEntity() {
		// Attempt to fetch an entity that does not exist in the cache
		BaseEntity fetchedEntity = cachingService.get(999L);

		// Verify that the result is null
		assertNull(fetchedEntity, "Fetching a non-existent entity should return null");
	}
}
