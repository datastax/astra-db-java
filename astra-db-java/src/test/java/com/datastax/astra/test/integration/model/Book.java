package com.datastax.astra.test.integration.model;

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

import com.datastax.astra.client.collections.mapping.DataApiCollection;
import com.datastax.astra.client.collections.mapping.DocumentId;
import com.datastax.astra.client.collections.mapping.Lexical;
import com.datastax.astra.client.collections.mapping.Vectorize;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

import static com.datastax.astra.client.core.lexical.AnalyzerTypes.STANDARD;

/**
 * Test model class representing a Book document with advanced features:
 * - Vector search with vectorization
 * - Lexical search
 * - Reranking capabilities
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DataApiCollection(
    name = "c_book_auto",
    defaultIdType = "",
    // Vector
    vectorDimension = 1024,
    vectorSimilarity = SimilarityMetric.COSINE,
    // Vectorize
    vectorizeModel = "NV-Embed-QA",
    vectorizeProvider = "nvidia",
    // Lexical
    lexicalEnabled = true,
    lexicalAnalyzer = STANDARD,
    // Rerank
    rerankEnabled = true,
    rerankProvider = "nvidia",
    rerankModel = "nvidia/llama-3.2-nv-rerankqa-1b-v2"
)
public class Book {

    @DocumentId
    String id;

    String title;

    String author;

    boolean is_checked_out;

    @Vectorize
    String vectorize;

    @Lexical
    String lexical;

    @JsonProperty("number_of_pages")
    Integer numberOfPages;

    String genre;

    String description;

    Set<String> genres;

    Map<String, String> metadata;

    // Fluent interface methods
    public Book id(String id) {
        this.id = id;
        return this;
    }

    public Book title(String title) {
        this.title = title;
        return this;
    }

    public Book author(String author) {
        this.author = author;
        return this;
    }

    public Book isCheckedOut(boolean isCheckedOut) {
        this.is_checked_out = isCheckedOut;
        return this;
    }

    public Book vectorize(String vectorize) {
        this.vectorize = vectorize;
        return this;
    }

    public Book lexical(String lexical) {
        this.lexical = lexical;
        return this;
    }

    public Book numberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
        return this;
    }

    public Book genre(String genre) {
        this.genre = genre;
        return this;
    }

    public Book description(String description) {
        this.description = description;
        return this;
    }

    public Book genres(Set<String> genres) {
        this.genres = genres;
        return this;
    }

    public Book metadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }
}
