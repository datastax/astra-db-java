/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datastax.astra.client.model.iterable;

import lombok.Getter;

import java.util.List;
import java.util.Optional;

/**
 * Hold results for paging
 *
 * @author Cedrick LUNVEN (@clunven)
 *
 * @param <R>
 *      document type
 */
@Getter
public class Page<R> {
 
    /** Size of the page */
    private final int pageSize;
    
    /** Of present there is a next page. */
    private final String pageState;
    
    /** list of results matching the request. */
    private final List< R > results;
    
    /**
     * Default Constructor.
     */
    public Page() {
        this(0, null, null);
    }

    /**
     * Default constructor.
     *
     * @param pageSize int
     * @param pageState String
     */
    public Page(int pageSize, String pageState) {
        this(pageSize, pageState, null);
    }

    /**
     * Default constructor.
     * 
     * @param pageSize int
     * @param pageState String
     * @param results List
     */
    public Page(int pageSize, String pageState, List<R> results) {
        this.pageState = pageState;
        this.results   = results;
        this.pageSize  = pageSize;
    }

    /**
     * Express if results is empty.
     *
     * @return
     *      return value
     */
    public boolean isEmpty() {
        return results== null || results.isEmpty();
    }


    /**
     * Expected from a stream of result.
     *
     * @return
     *      first result if exist
     */
    public Optional<R> findFirst() {
        if (!isEmpty()) return Optional.ofNullable(results.get(0));
        return Optional.empty();
    }

    /**
     * When the result is a singleton.
     *
     * @return
     *      result as a singleton
     */
    public R one() {
        if (getResults() == null || getResults().size() !=1) {
            throw new IllegalStateException("Current page does not contain a single record");
        }
        return getResults().get(0);
    }

    /**
     * Getter accessor for attribute 'pageState'.
     *
     * @return
     *       current value of 'pageState'
     */
    public Optional<String> getPageState() {
        return Optional.ofNullable(pageState);
    }
}
