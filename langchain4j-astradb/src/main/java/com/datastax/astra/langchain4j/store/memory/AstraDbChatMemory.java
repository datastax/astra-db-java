package com.datastax.astra.langchain4j.store.memory;

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

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.databases.Database;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;

import java.util.List;

import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.core.query.Sort.ascending;
import static com.datastax.astra.langchain4j.store.memory.AstraDbChatMessage.PROP_MESSAGE;
import static com.datastax.astra.langchain4j.store.memory.AstraDbChatMessage.PROP_MESSAGE_TIME;

/**
 * AstraDbChatMemory is a ChatMemory implementation that uses AstraDB as a backend.
 */
public class AstraDbChatMemory implements ChatMemory {

    /**
     * Identifier of the chat memory.
     */
    private final String id;

    /**
     * Collection to store chat messages.
     */
    private final Collection<AstraDbChatMessage> chatMemoryCollection;

    /**
     * Filter to retrieve chat messages.
     */
    private final Filter filter;

    /**
     * Create a chat memory with an identifier and a collection.
     *
     * @param db
     *      database to store chat messages
      * @param collectionName
     *      identifier of the chat memory
     * @param id
     *      collection to store chat messages
     */
    public AstraDbChatMemory(Database db, String collectionName, String id) {
        this.id                   = id;
        this.filter = eq(AstraDbChatMessage.PROP_CHAT_ID, id);
        if (!db.collectionExists(collectionName)) {
            this.chatMemoryCollection = db.createCollection(collectionName,
                    new CollectionDefinition().indexingDeny(PROP_MESSAGE), AstraDbChatMessage.class);
        } else {
            this.chatMemoryCollection = db.getCollection(collectionName, AstraDbChatMessage.class);
        }
    }

    /**
     * Retrieve all chat messages from AstraDB.
     *
     * @return all chat messages
     */
    public List<AstraDbChatMessage> messagesAstra() {
        CollectionFindOptions options = new CollectionFindOptions().sort(ascending(PROP_MESSAGE_TIME));
        return chatMemoryCollection.find(filter, options).toList();
    }

    /** {@inheritDoc} */
    @Override
    public Object id() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public void add(ChatMessage chatMessage) {
        chatMemoryCollection.insertOne(new AstraDbChatMessage(chatMessage).chatId(id));
    }

    /** {@inheritDoc} */
    @Override
    public List<ChatMessage> messages() {
        return messagesAstra().stream().map(AstraDbChatMessage::toChatMessage).toList();
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        chatMemoryCollection.deleteMany(filter);
    }
}
