package com.datastax.astra.client.core.reranking;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2025 DataStax
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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Setter
@Accessors(fluent = true, chain = true)
public class RerankingServiceOptions {

    /**
     * The name of the LLM provider.
     */
    String provider;

    /**
     * The name of the LLM model to use.
     * Example: "gpt-4" or "claude-v1".
     */
    String modelName;

    /**
     * A map containing authentication information, such as API keys or secrets.
     */
    private Map<String, Object> authentication;

    /**
     * Free-form parameters for configuring the service, such as hyper-parameters.
     */
    private Map<String, Object> parameters;

    /**
     * Default constructor for serialization purposes.
     */
    public RerankingServiceOptions() {}

    /**
     * Adds a single authentication key-value pair to the {@code authentication} map.
     *
     * @param key   the key for the authentication entry
     * @param value the value for the authentication entry
     * @return the current {@code VectorServiceOptions} instance for method chaining
     */
    public RerankingServiceOptions addAuthentication(String key, Object value) {
        if (authentication == null) {
            authentication = new HashMap<>();
        }
        authentication.put(key, value);
        return this;
    }

    /**
     * Sets the authentication map.
     *
     * @param auth the map containing authentication details
     * @return the current {@code VectorServiceOptions} instance for method chaining
     */
    public RerankingServiceOptions authentication(Map<String, Object> auth) {
        this.authentication = auth;
        return this;
    }

    /**
     * Sets the LLM model name.
     *
     * @param modelName the name of the model
     * @return the current {@code VectorServiceOptions} instance for method chaining
     */
    public RerankingServiceOptions modelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    /**
     * Sets the LLM provider.
     *
     * @param provider the name of the provider
     * @return the current {@code VectorServiceOptions} instance for method chaining
     */
    public RerankingServiceOptions provider(String provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Sets the parameters map.
     *
     * @param parameters a map of free-form parameters
     * @return the current {@code VectorServiceOptions} instance for method chaining
     */
    public RerankingServiceOptions parameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * Represents a parameter definition for LLM services.
     * <p>
     * Each parameter can have a type, a required flag, a default value, and a description.
     * </p>
     */
    @Getter
    @Setter
    public static class Parameters {

        /** The type of the parameter, such as "string", "integer", etc. */
        private String type;

        /** Indicates whether the parameter is mandatory. */
        private boolean required;

        /** The default value of the parameter, if any. */
        @JsonProperty("default")
        private Object defaultValue;

        /** A description of the parameter's purpose. */
        private String help;

        /** Default constructor for serialization purposes. */
        public Parameters() {
            // Default constructor for Jackson or other serializers
        }

        /**
         * Sets the default value for this parameter.
         *
         * @param defaultValue the default value
         * @return the current {@code Parameters} instance for method chaining
         */
        public RerankingServiceOptions.Parameters defaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Sets the type of this parameter.
         *
         * @param type the type of the parameter
         * @return the current {@code Parameters} instance for method chaining
         */
        public RerankingServiceOptions.Parameters type(String type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the description for this parameter.
         *
         * @param help a brief description of the parameter
         * @return the current {@code Parameters} instance for method chaining
         */
        public RerankingServiceOptions.Parameters help(String help) {
            this.help = help;
            return this;
        }

        /**
         * Sets whether this parameter is required.
         *
         * @param required {@code true} if the parameter is mandatory, {@code false} otherwise
         * @return the current {@code Parameters} instance for method chaining
         */
        public RerankingServiceOptions.Parameters required(boolean required) {
            this.required = required;
            return this;
        }
    }

}
