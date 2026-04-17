package com.datastax.astra.boot.autoconfigure;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.options.TimeoutOptions;
import com.datastax.astra.client.core.http.HttpClientOptions;
import com.datastax.astra.client.core.http.HttpProxy;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.databases.Database;
import com.dtsx.astra.sdk.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

/**
 * Spring Boot Auto-Configuration for DataAPI Client.
 * 
 * Initializing DataAPIClient (if class present in classpath)
 * - #1 Configuration with application.properties/yaml
 * - #2 Configuration with environment variables
 * - #3 Configuration with .astrarc on file system in user.home
 * 
 * You can also define your {@link DataAPIClient} explicitly.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Configuration
@ConditionalOnClass(DataAPIClient.class)
@EnableConfigurationProperties(DataAPIClientProperties.class)
public class DataAPIAutoConfiguration {
    
    /** Logger for our Client. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataAPIAutoConfiguration.class);

    /** Reference Properties. */
    @Autowired
    private DataAPIClientProperties dataAPIClientProperties;

    /**
     * Spring Configuration
     */
    @Autowired 
    private ConfigurableEnvironment env;

    /**
     * Default constructor
     */
    public DataAPIAutoConfiguration() {}

    /**
     * Accessing DataAPI client.
     *
     * @return dataAPI client
     */
    @Bean
    @ConditionalOnMissingBean
    public DataAPIClient dataAPIClient() {
        LOGGER.info("Setup of DataAPIClient from application.yaml");
        
        /* 
         * Load properties and initialize the client options
         */
        DataAPIClientOptions clientOptions = new DataAPIClientOptions();
        
        // Set destination
        if (Utils.hasLength(dataAPIClientProperties.getDestination())) {
            LOGGER.debug("+ Destination detected: {}", dataAPIClientProperties.getDestination());
            clientOptions.destination(DataAPIDestination.valueOf(dataAPIClientProperties.getDestination()));
        }
        
        // Enable request logging
        if (dataAPIClientProperties.getLogRequest() != null && dataAPIClientProperties.getLogRequest()) {
            LOGGER.debug("+ Request logging enabled");
            clientOptions.logRequests();
        }
        
        // Configure options if present
        if (dataAPIClientProperties.getOptions() != null) {
            DataAPIClientProperties.Options options = dataAPIClientProperties.getOptions();
            
            // Embedding API Key
            if (Utils.hasLength(options.getEmbeddingApiKey())) {
                LOGGER.debug("+ Embedding API key detected");
                clientOptions.embeddingAPIKey(options.getEmbeddingApiKey());
            }
            
            // HTTP Configuration
            if (options.getHttp() != null) {
                DataAPIClientProperties.Http http = options.getHttp();
                HttpClientOptions httpOptions = new HttpClientOptions();
                
                if (http.getRetryCount() != null && http.getRetryDelay() != null) {
                    LOGGER.debug("+ HTTP retry configuration: count={}, delay={}ms", 
                        http.getRetryCount(), http.getRetryDelay());
                    httpOptions.httpRetries(http.getRetryCount(), Duration.ofMillis(http.getRetryDelay()));
                }
                
                if (Utils.hasLength(http.getVersion())) {
                    LOGGER.debug("+ HTTP version: {}", http.getVersion());
                    httpOptions.httpVersion(HttpClient.Version.valueOf(http.getVersion()));
                }
                
                if (Utils.hasLength(http.getRedirect())) {
                    LOGGER.debug("+ HTTP redirect policy: {}", http.getRedirect());
                    httpOptions.httpRedirect(HttpClient.Redirect.valueOf(http.getRedirect()));
                }
                
                clientOptions.httpClientOptions(httpOptions);
            }
            
            // Proxy Configuration
            if (options.getProxy() != null) {
                DataAPIClientProperties.Proxy proxy = options.getProxy();
                if (Utils.hasLength(proxy.getHostname()) && proxy.getPort() != null) {
                    LOGGER.debug("+ HTTP proxy configured: {}:{}", proxy.getHostname(), proxy.getPort());
                    HttpProxy httpProxy = new HttpProxy(proxy.getHostname(), proxy.getPort());
                    // Note: HttpProxy class doesn't support username/password in constructor
                    // Authentication would need to be handled separately if supported by the API
                    clientOptions.httpClientOptions(
                        new HttpClientOptions().httpProxy(httpProxy)
                    );
                }
            }
            
            // Caller Tracking
            if (options.getCallers() != null && !options.getCallers().isEmpty()) {
                LOGGER.debug("+ Caller tracking configured with {} caller(s)", options.getCallers().size());
                for (DataAPIClientProperties.Caller caller : options.getCallers()) {
                    if (Utils.hasLength(caller.getName()) && Utils.hasLength(caller.getVersion())) {
                        clientOptions.addCaller(caller.getName(), caller.getVersion());
                    }
                }
            }
            
            // Timeout Configuration
            if (options.getTimeout() != null) {
                DataAPIClientProperties.Timeout timeout = options.getTimeout();
                TimeoutOptions timeoutOptions = new TimeoutOptions();
                
                if (timeout.getConnect() != null) {
                    LOGGER.debug("+ Connect timeout: {}ms", timeout.getConnect());
                    timeoutOptions.connectTimeoutMillis(timeout.getConnect());
                }
                if (timeout.getRequest() != null) {
                    LOGGER.debug("+ Request timeout: {}ms", timeout.getRequest());
                    timeoutOptions.requestTimeoutMillis(timeout.getRequest());
                }
                if (timeout.getGeneral() != null) {
                    LOGGER.debug("+ General timeout: {}ms", timeout.getGeneral());
                    timeoutOptions.generalMethodTimeoutMillis(timeout.getGeneral());
                }
                if (timeout.getDbAdmin() != null) {
                    LOGGER.debug("+ Database admin timeout: {}ms", timeout.getDbAdmin());
                    timeoutOptions.databaseAdminTimeoutMillis(timeout.getDbAdmin());
                }
                if (timeout.getKeyspaceAdmin() != null) {
                    LOGGER.debug("+ Keyspace admin timeout: {}ms", timeout.getKeyspaceAdmin());
                    timeoutOptions.keyspaceAdminTimeoutMillis(timeout.getKeyspaceAdmin());
                }
                if (timeout.getCollectionAdmin() != null) {
                    LOGGER.debug("+ Collection admin timeout: {}ms", timeout.getCollectionAdmin());
                    timeoutOptions.collectionAdminTimeoutMillis(timeout.getCollectionAdmin());
                }
                if (timeout.getTableAdmin() != null) {
                    LOGGER.debug("+ Table admin timeout: {}ms", timeout.getTableAdmin());
                    timeoutOptions.tableAdminTimeoutMillis(timeout.getTableAdmin());
                }
                
                clientOptions.timeoutOptions(timeoutOptions);
            }
            
            // Additional Headers
            if (options.getHeaders() != null) {
                if (options.getHeaders().getDb() != null) {
                    LOGGER.debug("+ Database headers configured: {} header(s)", 
                        options.getHeaders().getDb().size());
                    for (Map.Entry<String, String> header : options.getHeaders().getDb().entrySet()) {
                        clientOptions.addDatabaseAdditionalHeader(header.getKey(), header.getValue());
                    }
                }
                if (options.getHeaders().getAdmin() != null) {
                    LOGGER.debug("+ Admin headers configured: {} header(s)", 
                        options.getHeaders().getAdmin().size());
                    for (Map.Entry<String, String> header : options.getHeaders().getAdmin().entrySet()) {
                        clientOptions.addAdminAdditionalHeader(header.getKey(), header.getValue());
                    }
                }
            }
            
            // Observers
            if (options.getObservers() != null && !options.getObservers().isEmpty()) {
                LOGGER.debug("+ Observers configured: {} observer(s)", options.getObservers().size());
                for (DataAPIClientProperties.Observer observer : options.getObservers()) {
                    if (observer.getEnabled() != null && observer.getEnabled()) {
                        if ("logging".equalsIgnoreCase(observer.getType())) {
                            clientOptions.addObserver(observer.getName(), 
                                new com.datastax.astra.internal.command.LoggingCommandObserver(
                                    DataAPIClient.class));
                        }
                        // Custom observers can be added here if className is provided
                    }
                }
            }
            
            // Serialization/Deserialization Options
            if (options.getSerdes() != null) {
                DataAPIClientProperties.Serdes serdes = options.getSerdes();
                if (serdes.getEncodeDurationAsISO8601() != null) {
                    LOGGER.debug("+ Encode Duration as ISO8601: {}", serdes.getEncodeDurationAsISO8601());
                    // This would need to be set on the options if the API supports it
                }
                if (serdes.getEncodeDataApiVectorsAsBase64() != null) {
                    LOGGER.debug("+ Encode DataAPIVectors as Base64: {}", 
                        serdes.getEncodeDataApiVectorsAsBase64());
                    // This would need to be set on the options if the API supports it
                }
            }
        }
        
        // Create and return the DataAPIClient
        if (Utils.hasLength(dataAPIClientProperties.getToken())) {
            LOGGER.info("DataAPIClient initialized with token");
            return new DataAPIClient(dataAPIClientProperties.getToken(), clientOptions);
        } else {
            LOGGER.info("DataAPIClient initialized without default token");
            return new DataAPIClient(clientOptions);
        }
    }
    
    /**
     * Creates a Database bean if endpoint-url is provided in configuration.
     * This allows direct injection of Database instance in Spring components.
     * If a keyspace is specified, it will be used for database operations.
     *
     * @param dataAPIClient the DataAPIClient bean
     * @return Database instance configured with the endpoint URL and optional keyspace
     */
    @Bean
    @ConditionalOnMissingBean
    public Database database(DataAPIClient dataAPIClient) {

        if (Utils.hasLength(dataAPIClientProperties.getEndpointUrl())) {
            Database database;
            if (Utils.hasLength(dataAPIClientProperties.getKeyspace())) {
                LOGGER.info("Setup of Database from endpoint-url: {} with keyspace: {}", 
                    dataAPIClientProperties.getEndpointUrl(), 
                    dataAPIClientProperties.getKeyspace());
                database = dataAPIClient.getDatabase(
                    dataAPIClientProperties.getEndpointUrl(), 
                    dataAPIClientProperties.getKeyspace());
            } else {
                LOGGER.info("Setup of Database from endpoint-url: {}", dataAPIClientProperties.getEndpointUrl());
                database = dataAPIClient.getDatabase(dataAPIClientProperties.getEndpointUrl());
            }

            // list keyspaces from Data API Client
            SchemaAction schemaAction = dataAPIClientProperties.getSchemaAction();
            DatabaseAdmin dbAdmin = database.getDatabaseAdmin();
            if (!dbAdmin.listKeyspaceNames().contains(database.getKeyspace())) {
                LOGGER.info("Schema action configured: {}", schemaAction);
                if (SchemaAction.CREATE_IF_NOT_EXISTS.equals(schemaAction)) {
                    dbAdmin.createKeyspace(database.getKeyspace());
                } else if (SchemaAction.VALIDATE.equals(schemaAction)) {
                    throw new IllegalArgumentException("Keyspace '"
                            + database.getKeyspace() + "' has not been found, create the keyspace or " +
                            "set astra.data-api.schema-action to CREATE_IF_NOT_EXISTS");
                }
            }

            // if expected keyspace does not exists





            return database;
        } else {
            LOGGER.warn("No endpoint-url provided in configuration. Database bean will not be created.");
            return null;
        }
    }
    
    /**
     * Gets the configured schema action.
     *
     * @return the schema action
     */
    public SchemaAction getSchemaAction() {
        return dataAPIClientProperties.getSchemaAction();
    }
}
