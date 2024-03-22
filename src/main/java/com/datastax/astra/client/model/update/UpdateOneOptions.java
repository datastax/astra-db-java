package com.datastax.astra.client.model.update;

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

import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.find.SortOrder;
import com.datastax.astra.internal.utils.Assert;
import lombok.Data;

/**
 * Options for the updateOne operation

 */
@Data
public class UpdateOneOptions {

    /**
     * if upsert is selected
     */
    private Boolean upsert;

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Default constructor.
     */
    public UpdateOneOptions() {}

    /**
     * Builder Pattern, update the upsert flag
     *
     * @param upsert
     *     upsert flag
     * @return
     *      self reference
     */
    public UpdateOneOptions upsert(Boolean upsert) {
        Assert.notNull(upsert, "upsert");
        this.upsert = upsert;
        return this;
    }

    /**
     * Builder Pattern, update the sort clause
     *
     * @param pSort
     *      sort clause of the command
     * @return
     *      self reference
     */
    public UpdateOneOptions sortingBy(Document pSort) {
        Assert.notNull(pSort, "sort");
        if (this.sort == null) {
            sort = new Document();
        }
        this.sort.putAll(pSort);
        return this;
    }

    /**
     * Add a sort clause to the current field.
     *
     * @param fieldName
     *      field name
     * @param ordering
     *      field ordering
     * @return
     *      current reference  find
     */
    public UpdateOneOptions sortingBy(String fieldName, SortOrder ordering) {
        return sortingBy(new Document().append(fieldName, ordering.getOrder()));
    }
}
