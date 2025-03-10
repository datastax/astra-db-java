package com.datastax.astra.client.tables.commands.results;

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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Return update result.
 */
@Getter @Setter
public class TableUpdateResult {

    /**
     * Number of matched documents
     */
    Integer matchedCount;

    /**
     * Number of modified documents
     */
    Integer modifiedCount;

    /**
     * Populated if upserted
     */
    Object upsertedId;

    /**
     * Default constructor.
     */
    public TableUpdateResult() {}

}
