package com.datastax.astra.client.collections.commands.options;

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

import com.datastax.astra.client.core.options.BaseOptions;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.datastax.astra.client.collections.Collection.DEFAULT_COLLECTION_SERIALIZER;
import static com.datastax.astra.client.core.commands.CommandType.COLLECTION_ADMIN;

/**
 * Options for the dropCollection operation.
 */
@Setter
@Accessors(fluent = true, chain = true)
public class DropCollectionOptions extends BaseOptions<DropCollectionOptions> {

    /**
     * Default constructor.
     */
    public DropCollectionOptions() {
        super(null, COLLECTION_ADMIN, DEFAULT_COLLECTION_SERIALIZER, null);
    }
}
