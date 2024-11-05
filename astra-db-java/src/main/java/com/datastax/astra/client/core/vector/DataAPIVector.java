package com.datastax.astra.client.core.vector;

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


import com.datastax.astra.internal.utils.Assert;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DataAPIVector<T extends Number> implements Iterable<T>, Serializable {

    private final List<T> list;

    public static <V extends Number> DataAPIVector<V> of(V... vals) {
        return new DataAPIVector(Arrays.asList(vals));
    }

    public static <V extends Number> DataAPIVector<V> of(List<V> list) {
        return new DataAPIVector(list);
    }

    public DataAPIVector(List<T> list) {
        Assert.notNull(list, "Input list should not be null");
        Assert.isTrue(list.stream().noneMatch(Objects::isNull), "DataAPIVectors cannot contain null values");
        this.list = list;
    }

    public T get(int idx) {
        return this.list.get(idx);
    }

    public T set(int idx, T val) {
        return this.list.set(idx, val);
    }

    public int dimension() {
        return size();
    }

    public int size() {
        return this.list.size();
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public Iterator<T> iterator() {
        return this.list.iterator();
    }

    public Stream<T> stream() {
        return this.list.stream();
    }
}
