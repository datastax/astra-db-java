package com.datastax.astra.internal.serdes.collections;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2026 DataStax
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

import com.datastax.astra.client.collections.definition.documents.Document;

/**
 * Functional interface for transforming documents during collection cloning.
 * Allows custom transformation logic to be applied to each document as it's copied
 * from the source collection to the target collection.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * // Add a timestamp to each document
 * DocumentMapper addTimestamp = doc -> {
 *     doc.append("clonedAt", Instant.now());
 *     return doc;
 * };
 * 
 * // Remove sensitive fields
 * DocumentMapper removeSensitive = doc -> {
 *     doc.remove("password");
 *     doc.remove("ssn");
 *     return doc;
 * };
 * 
 * // Transform field values
 * DocumentMapper normalizeEmail = doc -> {
 *     if (doc.containsKey("email")) {
 *         doc.put("email", doc.getString("email").toLowerCase());
 *     }
 *     return doc;
 * };
 * 
 * CollectionCloneSettings settings = CollectionCloneSettings.builder()
 *     .documentMapper(addTimestamp)
 *     .build();
 * }</pre>
 */
@FunctionalInterface
public interface DocumentMapper {
    
    /**
     * Transforms a document during the cloning process.
     * 
     * @param document the source document to transform
     * @return the transformed document to be inserted into the target collection
     */
    Document map(Document document);
    
    /**
     * Returns a mapper that applies this mapper followed by the after mapper.
     * 
     * @param after the mapper to apply after this mapper
     * @return a composed mapper that applies both transformations in sequence
     */
    default DocumentMapper andThen(DocumentMapper after) {
        return doc -> after.map(this.map(doc));
    }
    
    /**
     * Returns an identity mapper that returns the document unchanged.
     * 
     * @return a mapper that performs no transformation
     */
    static DocumentMapper identity() {
        return doc -> doc;
    }
}
