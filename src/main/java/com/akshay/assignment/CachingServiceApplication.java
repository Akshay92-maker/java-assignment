package com.akshay.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.akshay.assignment.repositories")
@EntityScan(basePackages = "com.akshay.assignment.models")
public class CachingServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(CachingServiceApplication.class, args);
	}
}
