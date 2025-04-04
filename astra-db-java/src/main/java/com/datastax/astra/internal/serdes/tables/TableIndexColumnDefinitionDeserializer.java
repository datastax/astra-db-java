package com.datastax.astra.internal.serdes.tables;

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

import com.datastax.astra.client.tables.definition.TableDuration;
import com.datastax.astra.client.tables.definition.indexes.TableIndexColumnDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableIndexMapTypes;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * A custom deserializer for converting JSON strings into {@link TableDuration} objects.
 * Supports both ISO 8601 duration notation (e.g., "P1Y2M10DT2H30M") and compact notation (e.g., "1y2mo10d2h30m").
 * <p>The deserializer attempts to parse the input string in the following order:</p>
 * <ol>
 *     <li>ISO 8601 notation</li>
 *     <li>Compact notation</li>
 * </ol>
 * If the input does not match either format, an {@link IOException} is thrown.
 */
public class TableIndexColumnDefinitionDeserializer extends JsonDeserializer<TableIndexColumnDefinition> {

    /**
     * Default constructor.
     */
    public TableIndexColumnDefinitionDeserializer() {
    }

    /**
     * Deserializes a JSON string into a {@link TableDuration} object.
     *
     * @param jp the {@link JsonParser} providing the JSON input
     * @param ctxt the {@link DeserializationContext} in which the deserializer is operating
     * @return a {@link TableDuration} object representing the parsed duration
     * @throws IOException if the input string is not a valid duration format
     */
    @Override
    public TableIndexColumnDefinition deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        TableIndexColumnDefinition columnDefinition;
        if (node.isTextual()) {
            // Case where input is a simple string
            String name = node.asText();
            columnDefinition = new TableIndexColumnDefinition(name);
        } else if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            if (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String name = field.getKey();
                columnDefinition = new TableIndexColumnDefinition(name);
                String type = field.getValue().asText();
                if ("$keys".equals(type)) {
                    columnDefinition.setType(TableIndexMapTypes.KEYS);
                } else if ("$values".equals(type)) {
                    columnDefinition.setType(TableIndexMapTypes.VALUES);
                }
            } else {
                throw new IOException("Unexpected JSON format for TableIndexColumnDefinition");
            }
        } else {
            throw new IOException("Unexpected JSON format for TableIndexColumnDefinition");
        }
        return columnDefinition;
    }

}
