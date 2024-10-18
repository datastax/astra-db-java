package com.datastax.astra.client.model;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Data @NoArgsConstructor
public class VectorServiceOptions {

    /** LLM provider. */
    private String provider;

    /** LLM Model name. */
    private String modelName;

    /** Authentication information like keys and secrets. */
    private Map<String, Object> authentication;

    /** Free form parameters. */
    private Map<String, Object> parameters;

    /**
     * Subclass representing a parameters for LLM Services
     */
    @Getter
    @Setter
    public static class Parameters {

        /** Type for the parameters. */
        private String type;

        /** declare if mandatory or not. */
        private boolean required;

        /** the default value for the parameter. */
        @JsonProperty("default")
        private Object defaultValue;

        /** description of the parameter. */
        private String help;

        /** Default constructor. */
        public Parameters() {
            // left blank, serialization with jackson
        }
    }
}
