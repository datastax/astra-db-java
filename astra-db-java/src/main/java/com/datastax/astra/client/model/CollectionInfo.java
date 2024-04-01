package com.datastax.astra.client.model;

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

import com.datastax.astra.internal.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the Collection definition with its name and metadata.
 */
@Getter @Setter
public class CollectionInfo {

    /**
     * Name of the collection.
     */
    private String name;

    /**
     * Options for the collection.
     */
    private CollectionOptions options;

    /**
     * Default constructor.
     */
    public CollectionInfo() {
        // left blank, serialization with jackson
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JsonUtils.marshall(this);
    }
}
