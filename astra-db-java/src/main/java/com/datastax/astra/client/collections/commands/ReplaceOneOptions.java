package com.datastax.astra.client.collections.commands;

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

import com.datastax.astra.client.core.commands.CommandOptions;
import lombok.Getter;
import lombok.Setter;

/**
 * Options for the replaceOne operation.
 */
@Getter
@Setter
public class ReplaceOneOptions extends CommandOptions<ReplaceOneOptions> {

    /**
     * if upsert is selected
     */
    Boolean upsert;

    /**
     * Default constructor.
     */
    public ReplaceOneOptions() {
        // left blank as fields are populated by builder
    }

    /**
     * Upsert flag.
     *
     * @param upsert upsert flag
     * @return current command.
     */
    public ReplaceOneOptions upsert(Boolean upsert) {
        this.upsert = upsert;
        return this;
    }

    /**
     * Builder for creating {@link ReplaceOneOptions} instances with a fluent API.
     */
    @Deprecated
    public static class Builder {

        /**
         * Hide constructor.
         */
        private Builder() {
        }

        /**
         * Create a new instance of {@link ReplaceOneOptions}.
         *
         * @param upsert upsert flag
         * @return new instance of {@link ReplaceOneOptions}.
         */
        public static ReplaceOneOptions upsert(boolean upsert) {
            return new ReplaceOneOptions().upsert(upsert);
        }
    }
}
