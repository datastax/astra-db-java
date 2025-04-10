package com.datastax.astra.client.core.vectorize;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2025 DataStax
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

import com.datastax.astra.internal.serdes.core.VectorizeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NonNull;

/**
 * Serialization of vectorize
 */
@Data
@JsonSerialize(using = VectorizeSerializer.class)
public class Vectorize {

    /**
     * The passage to vectorize
     */
    final String passage;

    /**
     * The passage to set
     */
    final String setPassage;

    /**
     * Default constructor.
     *
     * @param passage
     *     the passage to vectorize
     */
    public Vectorize(String passage) {
       this(passage, null);
    }

    /**
     * Constructor with passage and setPassage
     *
     * @param passage
     *     the passage to vectorize
     * @param setPassage
     *     the passage to set
     */
    public Vectorize(@NonNull String passage, String setPassage) {
        this.passage = passage;
        this.setPassage = setPassage;
    }
}
