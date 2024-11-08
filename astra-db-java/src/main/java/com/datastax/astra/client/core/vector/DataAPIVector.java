package com.datastax.astra.client.core.vector;

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

import java.io.Serializable;

/**
 * DataAPIVector is a vector of embeddings.
 */
@Getter
public class DataAPIVector implements Serializable {

    /**
     * Embeddings list of the vector.
     */
    private final float[] embeddings;

    /**
     * Load the vector from a float array.
     *
     * @param vector
     *      float array with the mebddings
     */
    public DataAPIVector(float[] vector) {
        this.embeddings = vector;
    }

    /**
     * Access dimension of the Vector.
     *
     * @return
     *      dimensionality of the vector
     */
    public int dimension() {
        return getEmbeddings().length;
    }

}
