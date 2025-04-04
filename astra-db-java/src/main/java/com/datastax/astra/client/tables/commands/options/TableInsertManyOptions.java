package com.datastax.astra.client.tables.commands.options;

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

import com.datastax.astra.client.core.commands.CommandType;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.options.BaseOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.datastax.astra.client.tables.Table.DEFAULT_TABLE_SERIALIZER;

/**
 * Options for InsertMany
 */
@Getter @Setter
@Accessors(fluent = true, chain = true)
public class TableInsertManyOptions extends BaseOptions<TableInsertManyOptions> {

    /**
     * If the flag is set to true the command is failing on first error
     */
    boolean ordered = false;

    /**
     * When `true`, response will contain an additional field: 'documentResponses'"
     * with is an array of Document Response Objects. Each Document Response Object"
     * contains the `_id` of the document and the `status` of the operation (one of"
     * `OK`, `ERROR` or `SKIPPED`). Additional `errorsIdx` field is present when the"
     * " status is `ERROR` and contains the index of the error in the main `errors` array.",
     * defaultValue = "false").
     */
    boolean returnDocumentResponses = false;

    /**
     * If the flag is set to true the command is failing on first error
     */
    int concurrency = 1;

    /**
     * If the flag is set to true the command is failing on first error
     */
    int chunkSize = DataAPIClientOptions.MAX_CHUNK_SIZE;

    /**
     * Default constructor.
     */
    public TableInsertManyOptions() {
        super(null, CommandType.GENERAL_METHOD, DEFAULT_TABLE_SERIALIZER, null);
    }

}
