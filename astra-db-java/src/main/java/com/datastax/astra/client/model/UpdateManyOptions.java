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

import lombok.Getter;
import lombok.Setter;

/**
 * Options for the updateOne operation
 */
@Getter
@Setter
public class UpdateManyOptions extends CommandOptions<UpdateManyOptions> {

    /**
     * if upsert is selected
     */
    private Boolean upsert;

    /**
     * Default constructor.
     */
    public UpdateManyOptions() {
        // left blank as fields are populated by builder
    }

    /**
     * Upsert flag.
     *
     * @param upsert upsert flag
     * @return current command.
     */
    public UpdateManyOptions upsert(Boolean upsert) {
        this.upsert = upsert;
        return this;
    }

    /**
     * Builder for creating {@link UpdateManyOptions} instances with a fluent API.
     */
    @Deprecated
    public static class Builder {

        /**
         * Hide constructor.
         */
        private Builder() {
        }

        /**
         * Create a new instance of {@link UpdateManyOptions}.
         *
         * @param upsert upsert flag
         * @return new instance of {@link UpdateManyOptions}.
         */
        public static UpdateManyOptions upsert(boolean upsert) {
            return new UpdateManyOptions().upsert(upsert);
        }
    }

}
