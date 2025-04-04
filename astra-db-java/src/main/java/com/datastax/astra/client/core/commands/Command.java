package com.datastax.astra.client.core.commands;

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

import com.datastax.astra.client.collections.commands.Update;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.tables.commands.TableUpdateOperation;
import com.datastax.astra.internal.utils.Assert;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represent a command to be executed against the Data API.
 */
@Getter @Setter
@JsonSerialize(using = Command.CommandSerializer.class)
public class Command implements Serializable {

    /** Command Name. */
    protected String name;

    /** Command payload.*/
    protected Document payload = new Document();

    /**
     * Default constructor.
     */
    public Command() {
        // left blank and initialize with Jackson
    }

    /**
     * Create an empty command from its name.
     *
     * @param name
     *      unique command name
     * @return
     *      instance of the command
     */
    public static Command create(String name) {
        return new Command(name);
    }

    /**
     * Constructor with a name.
     *
     * @param name
     *      unique command name
     */
    public Command(String name) {
        Assert.hasLength(name, "command name");
        this.name = name;
    }

    /**
     * Builder pattern, update filter.
     *
     * @param filter
     *      filter for the command
     * @return
     *      self-reference
     */
    public Command withFilter(Filter filter) {
        payload.appendIfNotNull("filter", filter);
        return this;
    }

    /**
     * Builder pattern, update replacement document.
     *
     * @param replacement
     *      replacement for the command
     * @return
     *      self-reference
     */
    public Command withReplacement(Object replacement) {
        payload.appendIfNotNull("replacement", replacement);
        return this;
    }

    /**
     * Builder pattern, update projection.
     *
     * @param projection
     *      projection for the command
     * @return
     *      self-reference
     */
    public Command withProjection(Map<String, Boolean> projection) {
        payload.appendIfNotNull("projection", projection);
        return this;
    }

    /**
     * Return projections.
     *
     * @param pProjections
     *      list of projection
     * @return
     *      self-reference
     */
    public Command withProjection(Projection... pProjections) {
        if (pProjections != null) {
            Map<String, Object> finalProjection = new LinkedHashMap<>();
            for (Projection p : pProjections) {
                if (p.getPresent() != null && p.getSliceStart() != null) {
                    throw new IllegalArgumentException("A projection cannot be include/exclude and a slide at same time");
                }
                if (p.getPresent() == null && p.getSliceStart() == null) {
                    throw new IllegalArgumentException("A projection must be include/exclude or a slide");
                }
                if (p.getPresent() != null) {
                    finalProjection.put(p.getField(), p.getPresent());
                } else {
                    int start = p.getSliceStart();
                    Map<String, Object> slice = new LinkedHashMap<>();
                    if (p.getSliceEnd() != null) {
                        slice.put(DataAPIKeywords.SLICE.getKeyword(), new Integer[]{start, p.getSliceEnd()});
                    } else {
                        slice.put(DataAPIKeywords.SLICE.getKeyword(), start);
                    }
                    finalProjection.put(p.getField(), slice);
                }
            }
            payload.appendIfNotNull("projection", finalProjection);
        }
        return this;
    }

    /**
     * Builder pattern, update sort.
     *
     * @param sortCriteria
     *      sort criteria for the command
     * @return
     *      self-reference
     */
    public Command withSort(Sort... sortCriteria) {
        if (sortCriteria != null) {
            LinkedHashMap<String, Object> results = new LinkedHashMap<>();
            for (Sort s : sortCriteria) {
                if ( s != null) {
                    results.put(s.getField(), s.getValue());
                }
            }
            payload.appendIfNotNull("sort", results);
        }
        return this;
    }

    /**
     * Builder pattern, update options.
     *
     * @param options
     *      options for the command
     * @return
     *      self-reference
     */
    public Command withOptions(Object options) {
        payload.appendIfNotNull("options", options);
        return this;
    }

    /**
     * Builder pattern, update document.
     *
     * @param document
     *      document for the command
     * @return
     *      self-reference
     */
    public Command withDocument(Object document) {
        payload.appendIfNotNull("document", document);
        return this;
    }

    /**
     * Builder pattern, update documents.
     *
     * @param documents
     *      documents for the command
     * @param <T>
     *      working clas for documents
     * @return
     *      self-reference
     */
    public <T> Command withDocuments(List<T> documents) {
        payload.appendIfNotNull("documents", documents);
        return this;
    }

    /**
     * Builder pattern, update documents.
     *
     * @param key
     *      name of the attribute
     * @param obj
     *      value of the attribute
     * @return
     *      self-reference
     */
    public Command append(String key, Object obj) {
        payload.appendIfNotNull(key, obj);
        return this;
    }

    /**
     * Builder pattern,  Update.
     *
     * @param update
     *      update of the command
     * @return
     *      self-reference
     */
    public Command withUpdate(Update update) {
        payload.appendIfNotNull("update", update);
        return this;
    }

    /**
     * Builder pattern,  Update.
     *
     * @param update
     *      update of the command
     * @return
     *      self-reference
     */
    public Command withUpdate(TableUpdateOperation update) {
        payload.appendIfNotNull("update", update);
        return this;
    }

    /**
     * Specialization of the command.
     *
     * @param name
     *      command name
     * @param payload
     *      command payload
     */
    public Command(String name, Document payload) {
        this.name = name;
        this.payload = payload;
    }

    /**
     * Custom serializer for Command class.
     */
    public static class CommandSerializer extends StdSerializer<Command> {

        /**
         * Default constructor.
         */
        public CommandSerializer() {
            this(null);
        }

        /**
         * Constructor with the class in used.
         *
         * @param clazz
         *      type of command for serialization
         */
        public CommandSerializer(Class<Command> clazz) {
            super(clazz);
        }

        /** {@inheritDoc} */
        @Override
        public void serialize(Command command, JsonGenerator gen, SerializerProvider provider) throws IOException {
            LinkedHashMap<String, Object> commandMap = new LinkedHashMap<>();
            commandMap.put(command.getName(), command.getPayload());
            gen.writeObject(commandMap);
        }
    }

}
