package com.datastax.astra.test.integration.utils;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.client.DataAPIDestination;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Test configuration loader that reads settings from properties files.
 * <p>
 * Configuration priority (highest to lowest):
 * <ol>
 *   <li>Environment variables (e.g., ASTRA_DB_APPLICATION_TOKEN)</li>
 *   <li>System properties (e.g., -Dtest.environment=local)</li>
 *   <li>test-config-astra.properties (for Astra settings, gitignored)</li>
 *   <li>test-config-local.properties (for local settings)</li>
 *   <li>test-config.properties (default settings)</li>
 * </ol>
 * <p>
 * This allows running tests in IDE without setting environment variables,
 * while still allowing environment variables to override for CI/CD.
 */
@Slf4j
public final class TestConfig {

    // Environment variable names (matching existing convention)
    public static final String ENV_VAR_DESTINATION = "ASTRA_DB_JAVA_TEST_ENV";
    public static final String ENV_VAR_ASTRA_TOKEN = "ASTRA_DB_APPLICATION_TOKEN";
    public static final String ENV_VAR_ASTRA_TOKEN_DEV = "ASTRA_DB_APPLICATION_TOKEN_DEV";
    public static final String ENV_VAR_CLOUD_PROVIDER = "ASTRA_CLOUD_PROVIDER";
    public static final String ENV_VAR_CLOUD_REGION = "ASTRA_CLOUD_REGION";

    // Environment values
    public static final String ENV_LOCAL = "local";
    public static final String ENV_ASTRA_DEV = "astra_dev";
    public static final String ENV_ASTRA_PROD = "astra_prod";
    public static final String ENV_ASTRA_TEST = "astra_test";

    // Property keys (for config files)
    private static final String PROP_ENVIRONMENT = "test.environment";
    private static final String PROP_LOCAL_ENDPOINT = "local.endpoint";
    private static final String PROP_LOCAL_USERNAME = "local.username";
    private static final String PROP_LOCAL_PASSWORD = "local.password";
    private static final String PROP_LOCAL_KEYSPACE = "local.keyspace";
    private static final String PROP_ASTRA_TOKEN = "astra.token";
    private static final String PROP_ASTRA_TOKEN_DEV = "astra.token.dev";
    private static final String PROP_ASTRA_CLOUD_PROVIDER = "astra.cloud.provider";
    private static final String PROP_ASTRA_CLOUD_REGION = "astra.cloud.region";

    // API Keys for embedding providers
    private static final String PROP_OPENAI_API_KEY = "openai.api.key";
    private static final String PROP_HF_API_KEY = "huggingface.api.key";
    private static final String PROP_MISTRAL_API_KEY = "mistral.api.key";
    private static final String PROP_JINA_API_KEY = "jina.api.key";
    private static final String PROP_VOYAGE_API_KEY = "voyage.api.key";
    private static final String PROP_UPSTAGE_API_KEY = "upstage.api.key";
    private static final String PROP_NVIDIA_API_KEY = "nvidia.api.key";
    private static final String PROP_COHERE_API_KEY = "cohere.api.key";

    // AWS Bedrock settings
    private static final String PROP_BEDROCK_ACCESS_ID = "bedrock.aws.access.id";
    private static final String PROP_BEDROCK_SECRET_ID = "bedrock.aws.secret.id";
    private static final String PROP_BEDROCK_REGION = "bedrock.aws.region";

    // HuggingFace Dedicated settings
    private static final String PROP_HF_DEDICATED_API_KEY = "huggingface.dedicated.api.key";
    private static final String PROP_HF_DEDICATED_DIMENSION = "huggingface.dedicated.dimension";
    private static final String PROP_HF_DEDICATED_ENDPOINT = "huggingface.dedicated.endpoint";
    private static final String PROP_HF_DEDICATED_REGION = "huggingface.dedicated.region";
    private static final String PROP_HF_DEDICATED_CLOUD = "huggingface.dedicated.cloud";

    // Config files (loaded in order, later overrides earlier)
    private static final String[] CONFIG_FILES = {
        "test-config.properties",
        "test-config-local.properties",
        "test-config-astra.properties",
        "test-config-embedding-providers.properties"
    };

    // Singleton instance
    private static TestConfig instance;

    // Loaded properties
    private final Properties properties;

    private TestConfig() {
        this.properties = loadProperties();
        log.info("TestConfig initialized - environment: {}, isAstra: {}, hasToken: {}",
                getEnvironment(), isAstra(), hasAstraToken());
    }

    /**
     * Get the singleton instance.
     */
    public static synchronized TestConfig getInstance() {
        if (instance == null) {
            instance = new TestConfig();
        }
        return instance;
    }

    /**
     * Load properties from config files on classpath.
     */
    private Properties loadProperties() {
        Properties props = new Properties();
        for (String configFile : CONFIG_FILES) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(configFile)) {
                if (is != null) {
                    props.load(is);
                    log.debug("Loaded config file: {}", configFile);
                }
            } catch (IOException e) {
                log.warn("Failed to load config file: {}", configFile, e);
            }
        }
        return props;
    }

    /**
     * Get a configuration value with priority: env var > system property > config file > default.
     *
     * @param envVar       environment variable name (can be null)
     * @param propertyKey  property key for config file
     * @param defaultValue default value if not found
     * @return the configuration value
     */
    public String getConfig(String envVar, String propertyKey, String defaultValue) {

        // 1. Check environment variable
        if (envVar != null) {
            String envValue = System.getenv(envVar);
            if (hasLength(envValue)) {
                //log.info("(env var) {} = {}", envVar, envValue);
                return envValue;
            }
        }

        // 2. Check system property (try both env var style and property style)
        if (envVar != null) {
            String sysPropEnv = System.getProperty(envVar);
            if (hasLength(sysPropEnv)) {
                //log.info("(system prop env) {} = {}", envVar, sysPropEnv);
                return sysPropEnv;
            }
        }
        String sysProp = System.getProperty(propertyKey);
        if (hasLength(sysProp)) {
            //log.info("(system prop) {} = {}", propertyKey, sysProp);
            return sysProp;
        }

        // 3. Check config file
        String configValue = properties.getProperty(propertyKey);
        if (hasLength(configValue)) {
            //log.info("(prop) {} = {}", propertyKey, configValue);
            return configValue;
        }

        // 4. Return default
        //log.info("(default) {} = {}", propertyKey, defaultValue);
        return defaultValue;
    }

    /**
     * Get the test environment (local, astra_dev, astra_prod, astra_test).
     */
    public String getEnvironment() {
        return getConfig(ENV_VAR_DESTINATION, PROP_ENVIRONMENT, ENV_LOCAL);
    }

    /**
     * Get the DataAPI destination based on the environment.
     */
    public DataAPIDestination getDataAPIDestination() {
        String env = getEnvironment();
        switch (env) {
            case ENV_ASTRA_DEV:
                return DataAPIDestination.ASTRA_DEV;
            case ENV_ASTRA_PROD:
                return DataAPIDestination.ASTRA;
            case ENV_ASTRA_TEST:
                return DataAPIDestination.ASTRA_TEST;
            case ENV_LOCAL:
            default:
                return DataAPIDestination.HCD;
        }
    }

    /**
     * Check if running against local HCD/DSE.
     */
    public boolean isLocal() {
        return ENV_LOCAL.equals(getEnvironment());
    }

    /**
     * Check if running against Astra (any environment).
     */
    public boolean isAstra() {
        String env = getEnvironment();
        return ENV_ASTRA_DEV.equals(env) || ENV_ASTRA_PROD.equals(env) || ENV_ASTRA_TEST.equals(env);
    }

    /**
     * Get the Astra token.
     */
    public String getAstraToken() {
        String env = getEnvironment();
        // For dev environment, try the dev-specific token first
        if (ENV_ASTRA_DEV.equals(env)) {
            String devToken = getConfig(ENV_VAR_ASTRA_TOKEN_DEV, PROP_ASTRA_TOKEN, null);
            if (hasLength(devToken)) {
                return devToken;
            }
        }
        return getConfig(ENV_VAR_ASTRA_TOKEN, PROP_ASTRA_TOKEN, null);
    }

    /**
     * Get the cloud provider for Astra.
     */
    public CloudProviderType getCloudProvider() {
        String provider = getConfig(ENV_VAR_CLOUD_PROVIDER, PROP_ASTRA_CLOUD_PROVIDER, "GCP");
        return CloudProviderType.valueOf(provider);
    }

    /**
     * Get the cloud region for Astra.
     */
    public String getCloudRegion() {
        return getConfig(ENV_VAR_CLOUD_REGION, PROP_ASTRA_CLOUD_REGION, "us-east1");
    }

    /**
     * Get the local endpoint.
     */
    public String getLocalEndpoint() {
        return getConfig(null, PROP_LOCAL_ENDPOINT, "http://localhost:8181");
    }

    /**
     * Get the local username.
     */
    public String getLocalUsername() {
        return getConfig(null, PROP_LOCAL_USERNAME, "cassandra");
    }

    /**
     * Get the local password.
     */
    public String getLocalPassword() {
        return getConfig(null, PROP_LOCAL_PASSWORD, "cassandra");
    }

    /**
     * Get the local keyspace.
     */
    public String getLocalKeyspace() {
        return getConfig(null, PROP_LOCAL_KEYSPACE, "default_keyspace");
    }

    /**
     * Check if Astra token is available (required for Astra tests).
     */
    public boolean hasAstraToken() {
        return hasLength(getAstraToken());
    }

    // ========================================================================
    // Embedding Provider API Keys
    // ========================================================================

    /**
     * Get the OpenAI API key.
     */
    public String getOpenAiApiKey() {
        return getConfig("OPENAI_API_KEY", PROP_OPENAI_API_KEY, null);
    }

    /**
     * Check if OpenAI API key is available.
     */
    public boolean hasOpenAiApiKey() {
        return hasLength(getOpenAiApiKey());
    }

    /**
     * Get the HuggingFace API key.
     */
    public String getHuggingFaceApiKey() {
        return getConfig("HF_API_KEY", PROP_HF_API_KEY, null);
    }

    /**
     * Check if HuggingFace API key is available.
     */
    public boolean hasHuggingFaceApiKey() {
        return hasLength(getHuggingFaceApiKey());
    }

    /**
     * Get the MistralAI API key.
     */
    public String getMistralApiKey() {
        return getConfig("MISTRAL_API_KEY", PROP_MISTRAL_API_KEY, null);
    }

    /**
     * Check if MistralAI API key is available.
     */
    public boolean hasMistralApiKey() {
        return hasLength(getMistralApiKey());
    }

    /**
     * Get the JinaAI API key.
     */
    public String getJinaApiKey() {
        return getConfig("JINA_API_KEY", PROP_JINA_API_KEY, null);
    }

    /**
     * Check if JinaAI API key is available.
     */
    public boolean hasJinaApiKey() {
        return hasLength(getJinaApiKey());
    }

    /**
     * Get the VoyageAI API key.
     */
    public String getVoyageApiKey() {
        return getConfig("VOYAGE_API_KEY", PROP_VOYAGE_API_KEY, null);
    }

    /**
     * Check if VoyageAI API key is available.
     */
    public boolean hasVoyageApiKey() {
        return hasLength(getVoyageApiKey());
    }

    /**
     * Get the UpstageAI API key.
     */
    public String getUpstageApiKey() {
        return getConfig("UPSTAGE_API_KEY", PROP_UPSTAGE_API_KEY, null);
    }

    /**
     * Check if UpstageAI API key is available.
     */
    public boolean hasUpstageApiKey() {
        return hasLength(getUpstageApiKey());
    }

    /**
     * Get the Nvidia API key.
     */
    public String getNvidiaApiKey() {
        return getConfig("NVIDIA_API_KEY", PROP_NVIDIA_API_KEY, null);
    }

    /**
     * Check if Nvidia API key is available.
     */
    public boolean hasNvidiaApiKey() {
        return hasLength(getNvidiaApiKey());
    }

    /**
     * Get the Cohere API key.
     */
    public String getCohereApiKey() {
        return getConfig("COHERE_API_KEY", PROP_COHERE_API_KEY, null);
    }

    /**
     * Check if Cohere API key is available.
     */
    public boolean hasCohereApiKey() {
        return hasLength(getCohereApiKey());
    }

    // ========================================================================
    // AWS Bedrock Configuration
    // ========================================================================

    /**
     * Get the AWS Bedrock Access ID.
     */
    public String getBedrockAccessId() {
        return getConfig("BEDROCK_HEADER_AWS_ACCESS_ID", PROP_BEDROCK_ACCESS_ID, null);
    }

    /**
     * Get the AWS Bedrock Secret ID.
     */
    public String getBedrockSecretId() {
        return getConfig("BEDROCK_HEADER_AWS_SECRET_ID", PROP_BEDROCK_SECRET_ID, null);
    }

    /**
     * Get the AWS Bedrock Region.
     */
    public String getBedrockRegion() {
        return getConfig("BEDROCK_HEADER_AWS_REGION", PROP_BEDROCK_REGION, null);
    }

    /**
     * Check if AWS Bedrock credentials are available.
     */
    public boolean hasBedrockCredentials() {
        return hasLength(getBedrockAccessId())
            && hasLength(getBedrockSecretId())
            && hasLength(getBedrockRegion());
    }

    // ========================================================================
    // HuggingFace Dedicated Configuration
    // ========================================================================

    /**
     * Get the HuggingFace Dedicated API key.
     */
    public String getHuggingFaceDedicatedApiKey() {
        return getConfig("HUGGINGFACEDED_API_KEY", PROP_HF_DEDICATED_API_KEY, null);
    }

    /**
     * Get the HuggingFace Dedicated embedding dimension.
     */
    public String getHuggingFaceDedicatedDimension() {
        return getConfig("HUGGINGFACEDED_DIMENSION", PROP_HF_DEDICATED_DIMENSION, null);
    }

    /**
     * Get the HuggingFace Dedicated endpoint name.
     */
    public String getHuggingFaceDedicatedEndpoint() {
        return getConfig("HUGGINGFACEDED_ENDPOINTNAME", PROP_HF_DEDICATED_ENDPOINT, null);
    }

    /**
     * Get the HuggingFace Dedicated region name.
     */
    public String getHuggingFaceDedicatedRegion() {
        return getConfig("HUGGINGFACEDED_REGIONNAME", PROP_HF_DEDICATED_REGION, null);
    }

    /**
     * Get the HuggingFace Dedicated cloud name.
     */
    public String getHuggingFaceDedicatedCloud() {
        return getConfig("HUGGINGFACEDED_CLOUDNAME", PROP_HF_DEDICATED_CLOUD, null);
    }

    /**
     * Check if HuggingFace Dedicated configuration is available.
     */
    public boolean hasHuggingFaceDedicatedConfig() {
        return hasLength(getHuggingFaceDedicatedApiKey())
            && hasLength(getHuggingFaceDedicatedDimension())
            && hasLength(getHuggingFaceDedicatedEndpoint())
            && hasLength(getHuggingFaceDedicatedRegion())
            && hasLength(getHuggingFaceDedicatedCloud());
    }

    /**
     * Utility method to check if a string has content.
     */
    private static boolean hasLength(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
