package com.datastax.astra.internal.serdes.collections;

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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.IOException;
import java.util.UUID;

/**
 * Custom deserializer for EJson Date type.
 */
public class UUIDDeserializer extends JsonDeserializer<UUID> {

    /**
     * Default constructor.
     */
    public UUIDDeserializer() {
        // left blank, will be populated by jackson
    }

    /** {@inheritDoc} */
    @Override
    public UUID deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException {
        String uuidStr = null;
        JsonNode node = ctxt.readTree(jp);

        switch (node.getNodeType()) {
            case STRING:
                uuidStr = node.asText();
                break;

            case OBJECT:
                JsonNode uuidValue = node.get("$uuid");
                if (null != uuidValue && uuidValue.isTextual()) {
                    uuidStr = uuidValue.textValue();
                }
            default:
                break;
        }
        if (null == uuidStr) {
            throw new IllegalArgumentException("Cannot convert the expression as an UUID " + node);
        }
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID String: \"" + uuidStr + "\"", e);
        }
    }
}
