package com.datastax.astra.client.model.delete;

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
import com.datastax.astra.client.internal.utils.Assert;
import lombok.Getter;

/**
 * Options to delete One document.
 */
@Getter
public class DeleteOneOptions {

    /**
     * Default constructor.
     */
    public DeleteOneOptions() {
    }

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Fluent api.
     *
     * @param pSort
     *      add a filter
     * @return
     *      current command.
     */
    public DeleteOneOptions sortingBy(Document pSort) {
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
    public DeleteOneOptions sortingBy(String fieldName, SortOrder ordering) {
        return sortingBy(new Document().append(fieldName, ordering.getOrder()));
    }
}
