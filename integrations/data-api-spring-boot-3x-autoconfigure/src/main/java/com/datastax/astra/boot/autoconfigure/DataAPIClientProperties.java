package com.datastax.astra.boot.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * Configuration properties for DataAPI Client.
 * Maps to 'astra.data-api' prefix in application.yaml
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Data
@ConfigurationProperties(prefix = "astra.data-api")
public class DataAPIClientProperties {

    /**
     * Authentication token for DataAPI access
     */
    private String token;

    /**
     * DataAPI endpoint URL
     */
    private String endpointUrl;

    /**
     * Keyspace name for database operations
     */
    private String keyspace;

    /**
     * Destination type (ASTRA, DSE, HCD, etc.)
     */
    private String destination;

    /**
     * Enable request logging
     */
    private Boolean logRequest;

    /**
     * Schema action for collection/table management.
     * Similar to JPA's ddl-auto property.
     * Possible values:
     * - CREATE_IF_NOT_EXISTS: Create collections/tables if they don't exist (default)
     * - VALIDATE: Validate that collections/tables exist but don't create them
     * - NONE: Do nothing, assume collections/tables already exist
     */
    private SchemaAction schemaAction = SchemaAction.CREATE_IF_NOT_EXISTS;

    /**
     * Advanced options for DataAPI client
     */
    private Options options;

    @Data
    public static class Options {
        /**
         * API key for embedding services
         */
        private String embeddingApiKey;

        /**
         * API key for reranking services
         */
        private String rerankApiKey;

        /**
         * HTTP client configuration
         */
        private Http http;

        /**
         * HTTP proxy configuration
         */
        private Proxy proxy;

        /**
         * Caller tracking information
         */
        private List<Caller> callers;

        /**
         * Timeout configuration
         */
        private Timeout timeout;

        /**
         * Additional headers
         */
        private Headers headers;

        /**
         * Observer configuration
         */
        private List<Observer> observers;

        /**
         * Serialization/Deserialization options
         */
        private Serdes serdes;
    }

    @Data
    public static class Http {
        /**
         * Number of retry attempts
         */
        private Integer retryCount;

        /**
         * Delay between retries in milliseconds
         */
        private Integer retryDelay;

        /**
         * HTTP protocol version (HTTP_1_1, HTTP_2)
         */
        private String version;

        /**
         * HTTP redirect policy (NEVER, ALWAYS, NORMAL)
         */
        private String redirect;
    }

    @Data
    public static class Proxy {
        /**
         * Proxy username
         */
        private String username;

        /**
         * Proxy password
         */
        private String password;

        /**
         * Proxy hostname
         */
        private String hostname;

        /**
         * Proxy port
         */
        private Integer port;
    }

    @Data
    public static class Caller {
        /**
         * Caller name
         */
        private String name;

        /**
         * Caller version
         */
        private String version;
    }

    @Data
    public static class Timeout {
        /**
         * HTTP connection timeout in milliseconds
         */
        private Integer connect;

        /**
         * HTTP request timeout in milliseconds
         */
        private Integer request;

        /**
         * General data operation timeout in milliseconds
         */
        private Integer general;

        /**
         * Database admin operation timeout in milliseconds
         */
        private Integer dbAdmin;

        /**
         * Keyspace admin operation timeout in milliseconds
         */
        private Integer keyspaceAdmin;

        /**
         * Collection admin operation timeout in milliseconds
         */
        private Integer collectionAdmin;

        /**
         * Table admin operation timeout in milliseconds
         */
        private Integer tableAdmin;
    }

    @Data
    public static class Headers {
        /**
         * Database-level headers
         */
        private Map<String, String> db;

        /**
         * Admin-level headers
         */
        private Map<String, String> admin;
    }

    @Data
    public static class Observer {
        /**
         * Observer type (logging, custom, etc.)
         */
        private String type;

        /**
         * Observer name
         */
        private String name;

        /**
         * Custom observer class name
         */
        private String className;

        /**
         * Whether observer is enabled
         */
        private Boolean enabled;
    }

    @Data
    public static class Serdes {
        /**
         * Encode Duration objects as ISO8601 strings
         */
        private Boolean encodeDurationAsISO8601;

        /**
         * Encode DataAPIVector objects as Base64
         */
        private Boolean encodeDataApiVectorsAsBase64;
    }
}
