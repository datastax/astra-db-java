package com.ibm.astra.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listener to log when the Spring application context is fully ready.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationStartupListener.class);
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LOGGER.info("=".repeat(80));
        LOGGER.info("🚀 Spring Boot Application Context is READY!");
        LOGGER.info("=".repeat(80));
        LOGGER.info("✅ DataAPI Client configured and available");
        LOGGER.info("✅ Database bean available (if endpoint-url configured)");
        LOGGER.info("📍 API endpoint: http://localhost:{}/api/hello", 
            event.getApplicationContext().getEnvironment().getProperty("server.port", "8080"));
        LOGGER.info("=".repeat(80));
    }
}
