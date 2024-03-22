package com.datastax.astra.client.model.iterable;

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

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.model.filter.Filter;
import com.datastax.astra.client.model.find.FindOptions;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @param <DOC>
 */
@Slf4j
@Getter
public class FindIterable<DOC> extends PageableIterable<DOC> implements Iterable<DOC> {

    /**
     * Iterator on documents.
     */
    protected FindIterator<DOC> currentPageIterator;

    /**
     * Constructor for a cursor over the elements of the find.
     * @param collection
     *      source collection client, use to fetch next pages
     * @param filter
     *      original filter used to renew the query
     * @param options
     *      list of options like the pageState, limit of skip
     */
    public FindIterable(Collection<DOC> collection, Filter filter, FindOptions options) {
        this.collection  = collection;
        this.filter       = filter;
        this.options      = options;
    }

    /**
     * Trigger a specialized Api call with proper 'skip' and 'limit' to only collect the item that is missing.
     *
     * @param offset
     *      offset of the required items
     * @return
     *     tem if it exists
     */
    public Optional<DOC> getItem(int offset) {
        FindOptions singleResultOptions = new FindOptions();
        singleResultOptions.skip(offset);
        singleResultOptions.limit(1);
        if (options.getIncludeSimilarity()) {
            singleResultOptions.includeSimilarity();
        }
        FindIterable<DOC> sub = new FindIterable<>(collection, filter, singleResultOptions);
        if (sub.fetchNextPage() && sub.getCurrentPage() != null && !sub.getCurrentPage().getResults().isEmpty()) {
            return Optional.ofNullable(sub.getCurrentPage().getResults().get(0));
        }
        return Optional.empty();
    }

    /**
     * Helper to return the first item in the iterator or null.
     *
     * @return T the first item or null.
     */
     public Optional<DOC> first() {
         return getItem(0);
     }

    /** {@inheritDoc} */
    @Override @NonNull
    public FindIterator<DOC> iterator() {
        if (currentPageIterator == null) {
            active = fetchNextPage();
            this.currentPageIterator = new FindIterator<>(this);
        }
        return currentPageIterator;
    }

    /**
     * Will exhaust the list and put all value in memory.
     *
     * @return
     *      all values of the iterable
     */
     public List<DOC> all() {
         if (exhausted) throw new IllegalStateException("Iterable is already exhauted.");
         if (active)    throw new IllegalStateException("Iterable has already been started");
         List<DOC> results = new ArrayList<>();
         for (DOC doc : this) results.add(doc);
         return results;
     }

}
