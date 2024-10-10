/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datastax.astra.client.model.types;

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

/**
 * Constants in the JSON API.
 */
@Getter
public enum DataAPIKeywords {

    /**
     * ID.
     */
    ID("_id"),

    /**
     * ALL.
     */
    ALL("$all"),

    /**
     * ALL.
     */
    DATE("$date"),

    /**
     * UUID
     */
    UUID("$uuid"),

    /**
     * OBJECT_ID.
     */
    OBJECT_ID("$objectId"),

    /**
     * SIZE.
     */
    SIZE("$size"),

    /**
     * EXISTS.
     */
    EXISTS("$exists"),

    /**
     * SIMILARITY.
     */
    SLICE("$slice"),

    /**
     * SIMILARITY.
     */
    SIMILARITY("$similarity"),

    /**
     * VECTOR.
     */
    VECTOR("$vector"),

    /**
     * SORT VECTOR.
     */
    SORT_VECTOR("sortVector"),

    /**
     * VECTORIZE.
     */
    VECTORIZE("$vectorize");

    /**
     * Keyword.
     */
    private final String keyword;

    /**
     * Constructor for the enum.
     *
     * @param op
     *      current operator
     */
    DataAPIKeywords(String op) {
        this.keyword = op;
    }
}
