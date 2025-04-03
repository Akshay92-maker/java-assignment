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
		BaseEntity entity = new BaseEntity();
		entity.setId(1L);
		cachingService.add(entity);

		// Clear the cache
		cachingService.clear();

		// Use reflection to access the private 'cache' and 'accessOrder' fields
		Field cacheField = CachingService.class.getDeclaredField("cache");
		cacheField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<Long, BaseEntity> cache = (Map<Long, BaseEntity>) cacheField.get(cachingService);

		Field accessOrderField = CachingService.class.getDeclaredField("accessOrder");
		accessOrderField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<Long, Long> accessOrder = (Map<Long, Long>) accessOrderField.get(cachingService);

		// Verify that the cache is cleared
		assertEquals(0, cache.size(), "Cache should be cleared");
		assertEquals(0, accessOrder.size(), "Access order should be cleared");
	}

	@Test
void testEvictLeastUsed() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
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

}
