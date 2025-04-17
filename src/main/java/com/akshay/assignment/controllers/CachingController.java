package com.akshay.assignment.controllers;

import com.akshay.assignment.models.BaseEntity;
import com.akshay.assignment.services.CachingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/cache-demo")
public class CachingController {

    private final CachingService cachingService;

    // Constructor injection for better practices
    @Autowired
    public CachingController(CachingService cachingService) {
        this.cachingService = cachingService;
    }

    // Add an entity to the cache
    @PostMapping("/add")
    public ResponseEntity<String> addItem(@RequestBody BaseEntity entity) {
        try {
            cachingService.add(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body("Entity added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add entity: " + e.getMessage());
        }
    }

    // Get an entity from the cache by ID
    @GetMapping("/get/{id}")
    public ResponseEntity<BaseEntity> getItem(@PathVariable Long id) {
        try {
            Optional<BaseEntity> entity = Optional.ofNullable(cachingService.get(id));
            return entity.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(null));  // Return 404 if not found
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);  // Return 500 if there's an error fetching the entity
        }
    }

    // Remove an entity from the cache by ID
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> removeItem(@PathVariable Long id) {
        try {
            boolean removed = cachingService.remove(id);
            if (removed) {
                return ResponseEntity.ok("Entity removed successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Entity not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to remove entity: " + e.getMessage());
        }
    }

    // Clear all entities in the cache
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCache() {
        try {
            cachingService.clear();
            return ResponseEntity.ok("Cache cleared successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to clear cache: " + e.getMessage());
        }
    }

    @DeleteMapping("/removeAll")
    public ResponseEntity<String> removeAll() {
        try {
            cachingService.removeAll();
            return ResponseEntity.ok("Removed all entities from cache and database successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to clear cache: " + e.getMessage());
        }
    }

}
