package com.datastax.astra.internal.serdes.shared;

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

import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Custom deserializer for EJson Date type.
 */
public class SimilarityMetricDeserializer extends JsonDeserializer<SimilarityMetric> {

    /**
     * Default constructor.
     */
    public SimilarityMetricDeserializer() {
        // left blank, will be populated by jackson
    }

    /** {@inheritDoc} */
    @Override
    public SimilarityMetric deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        return SimilarityMetric.valueOf(node.asText().toUpperCase());
    }

}
