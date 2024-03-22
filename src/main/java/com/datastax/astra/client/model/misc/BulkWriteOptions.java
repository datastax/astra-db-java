package com.datastax.astra.client.model.misc;

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

import lombok.Data;
/**
 *
 * Options used in the `bulkWrite` command.
 */
@Data
public final class BulkWriteOptions {

    /**
     * Flag to enforcer the ordering of the operations. If set to false the operations will be executed in parallel and put in an Execution Queue.
     */
    private boolean ordered = true;

    /**
     * When executed in parallel (ordered = false) the number of operations that can be executed at the same time.
     */
    private Integer concurrency = 5;

    /**
     * Default constructor.
     */
    public BulkWriteOptions() {}

}
