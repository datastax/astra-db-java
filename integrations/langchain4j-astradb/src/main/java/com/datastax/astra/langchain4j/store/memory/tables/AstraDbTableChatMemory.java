package com.datastax.astra.langchain4j.store.memory.tables;

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

import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.langchain4j.store.memory.AstraDbChatMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;

import java.util.List;
import java.util.UUID;

import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.core.query.Sort.ascending;
import static com.datastax.astra.langchain4j.store.memory.AstraDbChatMessage.PROP_MESSAGE_TIME;

/**
 * Encode a ChatMemory as a table in AstraDB.
 */
public class AstraDbTableChatMemory implements ChatMemory {

    /** identifier. */
    final UUID id;

    /**
     * Collection to store chat messages.
     */
    private final Table<AstraDBTableChatMessage> chatMemoryTable;

    /**
     * Filter to retrieve chat messages.
     */
    private final Filter filter;


    /**
     * Create a chat memory with an identifier and a collection.
     *
     * @param db
     *      database to store chat messages
     * @param tableName
     *      identifier of the chat memory
     * @param id
     *      collection to store chat messages
     */
    public AstraDbTableChatMemory(Database db, String tableName, UUID id) {
        this.id = id;
        this.filter = eq(AstraDbChatMessage.PROP_CHAT_ID, id);
        if (!db.tableExists(tableName)) {
            TableDefinition definition = new TableDefinition()
                    .addColumn("chat_id", TableColumnTypes.UUID)
                    .addColumn("message_id", TableColumnTypes.UUID)
                    .addColumn("message_type", TableColumnTypes.TEXT)
                    .addColumn("message_time", TableColumnTypes.TIMESTAMP)
                    .addColumn("text", TableColumnTypes.TEXT)
                    .addColumn("name", TableColumnTypes.TEXT)
                    .addColumnMap("contents",  TableColumnTypes.TEXT,  TableColumnTypes.TEXT)
                    .addColumnMap("tools_arguments", TableColumnTypes.TEXT,  TableColumnTypes.TEXT)
                    .addColumnMap("tools_name",  TableColumnTypes.TEXT,  TableColumnTypes.TEXT)
                    .partitionKey("chat_id")
                    .addPartitionSort(Sort.descending("message_time"))
                    .addPartitionSort(Sort.ascending("message_id"));
            this.chatMemoryTable = db.createTable(tableName, definition, AstraDBTableChatMessage.class);
        } else {
            this.chatMemoryTable = db.getTable(tableName, AstraDBTableChatMessage.class);
        }
    }

    /**
     * Retrieve all chat messages from AstraDB.
     *
     * @return all chat messages
     */
    public List<AstraDBTableChatMessage> messagesAstra() {
        TableFindOptions options = new TableFindOptions().sort(ascending(PROP_MESSAGE_TIME));
        return chatMemoryTable.find(filter, options).toList();
    }

    /** {@inheritDoc} */
    @Override
    public Object id() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public void add(ChatMessage chatMessage) {
        chatMemoryTable.insertOne(new AstraDBTableChatMessage(chatMessage).chatId(id));
    }

    /** {@inheritDoc} */
    @Override
    public List<ChatMessage> messages() {
        return messagesAstra().stream().map(AstraDBTableChatMessage::toChatMessage).toList();
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        chatMemoryTable.deleteMany(filter);
    }
}
