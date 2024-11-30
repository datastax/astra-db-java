package com.datastax.astra.client.tables.options;

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

import com.datastax.astra.client.core.commands.BaseOptions;
import com.datastax.astra.client.core.query.Sort;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Options for the updateOne operation

 */
@Getter @Setter
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
public class TableUpdateOneOptions extends BaseOptions<TableUpdateOneOptions> {

    /**
     * if upsert is selected
     */
    private Boolean upsert;

    /**
     * Order by.
     */
    private Sort[] sort;

    /**
     * Adding this on top of sort(Sort[] s) to allow for a more fluent API.
     * @param s
     *      sort options
     * @return
     *     current command
     */
    public TableUpdateOneOptions sort(Sort... s) {
        this.sort = s;
        return this;
    }

    /**
     * Get the sort options.
     *
     * @return
     *      sort options
     */
    public Sort[] getSortArray() {
        return sort;
    }
}
