package com.ibm.astra.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple REST controller for health check and testing.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@RestController
@RequestMapping("/api")
public class HelloController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);
    
    /**
     * Simple hello endpoint to verify the application is running.
     *
     * @return a greeting message
     */
    @GetMapping("/hello")
    public Map<String, String> hello() {
        LOGGER.info("Hello endpoint called");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from DataAPI Spring Boot!");
        response.put("status", "running");
        return response;
    }
}
