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

    /** Of present there is a next page. */
    private final String pageState;

    /** Sort vector retrieved in the status. */
    protected float[] sortVector;

    /** list of results matching the request. */
    private final List< R > results;

    /**
     * Default constructor.
     *
     * @param pageState String
     * @param results List
     */
    public Page(String pageState, List<R> results, float[] sortVector) {
        this.pageState  = pageState;
        this.results    = results;
        this.sortVector = sortVector;
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

    /**
     * If the sort Vector has been retrieved in the status, it will be available here.
     *
     * @return
     *      sort vector if available
     */
    public Optional<float[]> getSortVector() {
        return Optional.ofNullable(sortVector);
    }
}
