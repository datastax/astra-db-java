package com.datastax.astra.client.core.paging;

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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.NoSuchElementException;

public class Cursor<T> implements Iterable<T> {

    private CursorState state;

    private final String keyspace;

    private final Collection collection;

    private final Map<String, Object> filter;

    private final Map<String, Object> projection;

    private final Map<String, Object> sort;

    private int limit;

    private int skip;

    private boolean includeSimilarity;

    private boolean includeSortVector;

    private Function<Document, T> mapFunction;

    /** Page. */
    private final List<Document> buffer;

    private int consumedCount;

    // Constructor
    public Cursor(String keyspace, Collection collection) {
        this.state = CursorState.IDLE;
        this.keyspace = keyspace;
        this.collection = collection;
        this.filter = new HashMap<>();
        this.projection = new HashMap<>();
        this.sort = new HashMap<>();
        this.limit = 0;
        this.skip = 0;
        this.includeSimilarity = false;
        this.includeSortVector = false;
        this.mapFunction = null;
        this.buffer = new ArrayList<>();
        this.consumedCount = 0;
    }

    // Private constructor for cloning
    private Cursor(Cursor<T> cursor) {
        this.state = CursorState.IDLE;
        this.keyspace = cursor.keyspace;
        this.collection = cursor.collection;
        this.filter = new HashMap<>(cursor.filter);
        this.projection = new HashMap<>(cursor.projection);
        this.sort = new HashMap<>(cursor.sort);
        this.limit = cursor.limit;
        this.skip = cursor.skip;
        this.includeSimilarity = cursor.includeSimilarity;
        this.includeSortVector = cursor.includeSortVector;
        this.mapFunction = cursor.mapFunction;
        this.buffer = new ArrayList<>();
        this.consumedCount = 0;
    }

    // Immutable methods that return a new Cursor<T> instance
    public Cursor<T> filter(Map<String, Object> newFilter) {
        checkIdleState();
        Cursor<T> newCursor = this.clone();
        newCursor.filter.putAll(newFilter);
        return newCursor;
    }

    public Cursor<T> project(Map<String, Object> newProjection) {
        checkIdleState();
        if (this.mapFunction != null) {
            throw new CursorException("Cannot call project() after map().", state.toString());
        }
        Cursor<T> newCursor = this.clone();
        newCursor.projection.putAll(newProjection);
        return newCursor;
    }

    public Cursor<T> sort(Map<String, Object> newSort) {
        checkIdleState();
        Cursor<T> newCursor = this.clone();
        newCursor.sort.putAll(newSort);
        return newCursor;
    }

    public Cursor<T> limit(int newLimit) {
        checkIdleState();
        Cursor<T> newCursor = this.clone();
        newCursor.limit = newLimit;
        return newCursor;
    }

    public Cursor<T> skip(int newSkip) {
        checkIdleState();
        Cursor<T> newCursor = this.clone();
        newCursor.skip = newSkip;
        return newCursor;
    }

    public Cursor<T> includeSimilarity() {
        checkIdleState();
        Cursor<T> newCursor = this.clone();
        newCursor.includeSimilarity = true;
        return newCursor;
    }

    public Cursor<T> includeSortVector() {
        checkIdleState();
        Cursor<T> newCursor = this.clone();
        newCursor.includeSortVector = true;
        return newCursor;
    }

    public <R> Cursor<R> map(Function<Document, R> newMapFunction) {
        checkIdleState();
        if (this.mapFunction != null) {
            throw new CursorException("Cannot call map() after map().", state.toString());
        }
        Cursor<R> newCursor = new Cursor<>(this.keyspace, this.collection);
        newCursor.state = this.state;
        newCursor.filter.putAll(this.filter);
        newCursor.projection.putAll(this.projection);
        newCursor.sort.putAll(this.sort);
        newCursor.limit = this.limit;
        newCursor.skip = this.skip;
        newCursor.includeSimilarity = this.includeSimilarity;
        newCursor.includeSortVector = this.includeSortVector;
        newCursor.mapFunction = newMapFunction;
        return newCursor;
    }

    /** {@inheritDoc} */
    @Override
    public Cursor<T> clone() {
        return new Cursor<>(this);
    }

    // State-changing methods
    public void close() {
        this.state = CursorState.CLOSED;
        // Close any open resources here
    }

    public void rewind() {
        this.state = CursorState.IDLE;
        this.buffer.clear();
        this.consumedCount = 0;
        // Reset any other stateful components here
    }

    // Buffer consumption
    public List<Document> consumeBuffer(int n) {
        if (state == CursorState.CLOSED || state == CursorState.IDLE) {
            return Collections.emptyList();
        }
        List<Document> result = new ArrayList<>();
        int count = 0;
        while (!buffer.isEmpty() && count < n) {
            result.add(buffer.remove(0));
            count++;
        }
        return result;
    }

    /**
     * Validate that the cursor is in the IDLE state.
     */
    private void checkIdleState() {
        if (state != CursorState.IDLE) {
            throw new CursorException("Cannot modify cursor after it has been started.", state.toString());
        }
    }

    // Iterator implementation
    @Override
    public Iterator<T> iterator() {
        return new CursorIterator();
    }

    private class CursorIterator implements Iterator<T> {
        @Override
        public boolean hasNext() {
            if (state == CursorState.CLOSED) {
                return false;
            }
            if (state == CursorState.IDLE) {
                state = CursorState.STARTED;
            }
            if (!buffer.isEmpty()) {
                return true;
            }
            // Fetch next batch of documents into buffer
            fetchNextBatch();
            return !buffer.isEmpty();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Document rawDoc = buffer.remove(0);
            consumedCount++;
            if (mapFunction != null) {
                return mapFunction.apply(rawDoc);
            } else {
                // Unsafe cast; in practice, handle this properly
                return (T) rawDoc;
            }
        }
    }

    // Fetch next batch of documents
    private void fetchNextBatch() {
        // Implement logic to fetch next batch from database
        // and add to buffer. This is where you interact with the DB.
    }

    // Additional methods
    public boolean hasNext() {
        return iterator().hasNext();
    }

    public void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        try {
            for (T t : this) {
                action.accept(t);
            }
        } finally {
            close();
        }
    }

    public List<T> toList() {
        List<T> result = new ArrayList<>();
        try {
            forEach(result::add);
        } finally {
            close();
        }
        return result;
    }

    public int getBufferedCount() {
        return buffer.size();
    }

    public int getConsumedCount() {
        return consumedCount;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public Collection getCollection() {
        return collection;
    }

    public String getDataSource() {
        // Implement as needed
        return null;
    }

    // Exception class
    public static class CursorException extends RuntimeException {
        private final String state;

        public CursorException(String message, String state) {
            super(message);
            this.state = state;
        }

        public String getState() {
            return state;
        }
    }

    // Placeholder for Document class
    public static class Document {
        // Implement document fields and methods
    }

    // Placeholder for Collection class
    public static class Collection {
        // Implement collection fields and methods
    }
}
