package com.datastax.astra.client.collections.documents;

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
 * Enum to define the return document option.
 */
@Getter
public enum ReturnDocument {

    /** if set to before, the command will return the document before the update */
    BEFORE("before"),

    /** if set to after, the command will return the document after the update */
    AFTER("after");

    /** key to be used in the JSON payload */
    private final String key;

    /**
     * Constructor.
     *
     * @param key key to be used in the JSON payload
     */
    ReturnDocument(String key) {
        this.key = key;
    }
}
