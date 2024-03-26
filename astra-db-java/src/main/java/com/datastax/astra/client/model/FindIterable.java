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

import com.datastax.astra.client.Collection;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents the result of a 'find' command executed on a collection, providing an iterable interface to navigate
 * through the result set. This class extends {@code PageableIterable} to offer efficient, page-by-page iteration
 * over the results, which is particularly useful for handling large datasets.
 * <p>
 * Utilizing a lazy-loading approach, {@code FindIterable} allows for seamless navigation through the result set
 * without the necessity of loading all documents into memory at once. This makes it an ideal choice for applications
 * that need to process or display large amounts of data with minimal memory footprint. As users iterate over the
 * collection, subsequent pages of results are fetched as needed, ensuring efficient use of resources.
 * </p>
 * <p>
 * This approach is advantageous in scenarios where the full result set might be too large to store in memory
 * comfortably or when only a portion of the results is needed by the application at any given time. By iterating
 * over the results with {@code FindIterable}, applications can maintain lower memory usage while still having
 * the flexibility to access the entire result set.
 * </p>
 *
 * @param <DOC> The type of documents contained in the collection. This generic type allows {@code FindIterable}
 *              to be used with any type of document, making it a flexible solution for a wide variety of data models.
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
        FindOptions.FindOptionsBuilder builder = FindOptions.builder().skip(offset).limit(1);
        if (options.getIncludeSimilarity()) {
            builder.withIncludeSimilarity();
        }
        FindIterable<DOC> sub = new FindIterable<>(collection, filter, builder.build());
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
