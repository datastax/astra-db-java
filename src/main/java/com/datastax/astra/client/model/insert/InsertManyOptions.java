package com.datastax.astra.client.model.insert;

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

import com.datastax.astra.client.DataAPIOptions;
import lombok.Data;

/**
 * Options for InsertMany
 */
@Data
public class InsertManyOptions {

    /**
     * If the flag is set to true the command is failing on first error
     */
    private boolean ordered = false;

    /**
     * If the flag is set to true the command is failing on first error
     */
    private int concurrency = 1;

    /**
     * If the flag is set to true the command is failing on first error
     */
    private int chunkSize = DataAPIOptions.getMaxDocumentsInInsert();

    /**
     * If the flag is set to true the command is failing on first error
     */
    private int timeout = DataAPIOptions.DEFAULT_REQUEST_TIMEOUT_SECONDS * 1000;

    /**
     * Default options
     */
    public InsertManyOptions() {}

}
