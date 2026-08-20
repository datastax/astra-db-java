package com.datastax.astra.client.core.vector;

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

/**
 * Enumeration of supported source model types for vector embeddings.
 * These models are used to specify the embedding model when creating vector-enabled collections.
 */
public enum SourceModelTypes {
    
    /** OpenAI Ada 002 embedding model. */
    ADA002("ada002"),
    
    /** BERT embedding model. */
    BERT("bert"),
    
    /** Cohere v3 embedding model. */
    COHERE_V3("cohere-v3"),
    
    /** Google Gecko embedding model. */
    GECKO("gecko"),
    
    /** Other/custom embedding model. */
    OTHER("other"),
    
    /** NVIDIA QA-4 embedding model. */
    NV_QA_4("nv-qa-4"),
    
    /** OpenAI v3 Large embedding model. */
    OPENAI_V3_LARGE("openai-v3-large"),
    
    /** OpenAI v3 Small embedding model. */
    OPENAI_V3_SMALL("openai-v3-small");
    
    /** The string value used in API calls. */
    private final String value;
    
    /**
     * Constructor.
     *
     * @param value the string value for this source model type
     */
    SourceModelTypes(String value) {
        this.value = value;
    }
    
    /**
     * Gets the string value for this source model type.
     *
     * @return the string value
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Finds a SourceModelTypes enum by its string value.
     *
     * @param value the string value to search for
     * @return the matching SourceModelTypes, or null if not found
     */
    public static SourceModelTypes fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SourceModelTypes type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Returns the string value of this source model type.
     *
     * @return the string value
     */
    @Override
    public String toString() {
        return value;
    }
}