package com.datastax.astra.client.core.options;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@Setter
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
public class SerdesOptions implements Cloneable {

    /** Encode the vector as binary. */
    boolean encodeDurationAsISO8601 = true;

    /** Encode the vector as binary. */
    boolean encodeDataApiVectorsAsBase64 = true;

    /**
     * Gets encodeDurationAsISO8601
     *
     * @return value of encodeDurationAsISO8601
     */
    public boolean isEncodeDurationAsISO8601() {
        return encodeDurationAsISO8601;
    }

    /**
     * Gets encodeDataApiVectorsAsBase64
     *
     * @return value of encodeDataApiVectorsAsBase64
     */
    public boolean isEncodeDataApiVectorsAsBase64() {
        return encodeDataApiVectorsAsBase64;
    }

    public SerdesOptions disableEncodeDataApiVectorsAsBase64() {
        return encodeDataApiVectorsAsBase64(false);
    }

    public SerdesOptions disableEncodeDurationAsISO8601() {
        return encodeDurationAsISO8601(false);
    }

    @Override
    public SerdesOptions clone() {
        try {
            return (SerdesOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }
}
