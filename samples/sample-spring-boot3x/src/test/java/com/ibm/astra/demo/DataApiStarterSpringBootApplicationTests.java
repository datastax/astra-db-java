package com.ibm.astra.demo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Spring Boot application startup and REST API.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DataApiStarterSpringBootApplicationTests {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DataApiStarterSpringBootApplicationTests.class);
	
	@LocalServerPort
	private int port;
	
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void contextLoads() {
		LOGGER.info("=".repeat(80));
		LOGGER.info("✅ Spring Boot Application Context loaded successfully!");
		LOGGER.info("=".repeat(80));
		assertThat(restTemplate).isNotNull();
	}
	
	@Test
	void testHelloEndpoint() {
		LOGGER.info("Testing hello endpoint at port: {}", port);
		
		String url = "http://localhost:" + port + "/api/hello";
		ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
		
		LOGGER.info("Response status: {}", response.getStatusCode());
		LOGGER.info("Response body: {}", response.getBody());
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().get("message")).isEqualTo("Hello from DataAPI Spring Boot!");
		assertThat(response.getBody().get("status")).isEqualTo("running");
		
		LOGGER.info("✅ Hello endpoint test passed!");
	}
}
