package com.datastax.astra.test.integration.astra;

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

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.results.CollectionInsertOneResult;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.test.integration.AbstractCollectionFindAndRerankIT;
import com.datastax.astra.test.integration.model.Book;
import com.datastax.astra.test.integration.utils.EnabledIfAstra;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FindAndRerank integration tests against Astra DB.
 * <p>
 * Uses Nvidia NV-Embed-QA (built-in, no external API key) for embedding
 * and Nvidia reranking (uses the Astra token).
 */
@EnabledIfAstra
@DisplayName("Astra / Collections / Hybrid Search")
public class Astra_Collections_04_FindAndRerankIT extends AbstractCollectionFindAndRerankIT {

    @Override
    protected String getRerankingApiKey() {
        // Nvidia reranking uses the Astra token
        return getConfig().getAstraToken();
    }


    // ========== Annotation-based Collection Operations ==========

    @Test
    @Order(83)
    void should_createCollection_fromAnnotatedClass() {
        // Create collection from annotated class
        Collection<Book> bookCollection =
                getDatabase().createCollection(Book.class);

        assertThat(bookCollection).isNotNull();
        assertThat(bookCollection.getCollectionName()).isEqualTo("c_book_auto");

        // Clean up
        //getDatabase().dropCollection(Book.class);
    }

    @Test
    @Order(84)
    void should_getCollection_fromAnnotatedClass() {
        // Create collection first
        getDatabase().createCollection(Book.class);

        // Get collection using annotated class
        Collection<com.datastax.astra.test.integration.model.Book> bookCollection =
                getDatabase().getCollection(Book.class);

        assertThat(bookCollection).isNotNull();
        assertThat(bookCollection.getCollectionName()).isEqualTo("c_book_auto");

        // Insert a book
        Book book = new Book()
                .id("book1")
                .title("The Java Programming Language")
                .author("James Gosling")
                .numberOfPages(500)
                .isCheckedOut(false)
                .vectorize("A comprehensive guide to Java programming")
                .lexical("java programming language guide");

        CollectionInsertOneResult result = bookCollection.insertOne(book);
        assertThat(result).isNotNull();
        assertThat(result.getInsertedId()).isEqualTo("book1");

        // Find the book
        Optional<Book> foundBook = bookCollection.findById("book1");
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("The Java Programming Language");
        assertThat(foundBook.get().getAuthor()).isEqualTo("James Gosling");

        // Clean up
        getDatabase().dropCollection(com.datastax.astra.test.integration.model.Book.class);
    }

    @Test
    @Order(85)
    void should_getCollectionName_fromAnnotatedClass() {
        String collectionName = getDatabase().getCollectionName(com.datastax.astra.test.integration.model.Book.class);
        assertThat(collectionName).isEqualTo("c_book_auto");
    }

    @Test
    @Order(86)
    void should_getCollectionDefinition_fromAnnotatedClass() {
        CollectionDefinition definition = getDatabase().getCollectionDefinition(com.datastax.astra.test.integration.model.Book.class);
        assertThat(definition).isNotNull();
        // Verify the definition was created from the annotation
        assertThat(definition.getVector()).isNotNull();
    }

}
