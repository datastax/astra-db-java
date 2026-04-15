package com.datastax.astra.client.collections.mapping;

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

import com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes;
import com.datastax.astra.client.core.lexical.AnalyzerTypes;
import com.datastax.astra.client.core.vector.SimilarityMetric;

import javax.lang.model.type.NullType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a collection and its configuration options.
 * <p>
 * This annotation allows you to specify collection-level settings including:
 * <ul>
 *   <li>Collection name</li>
 *   <li>Default ID type</li>
 *   <li>Indexing options (allow/deny lists)</li>
 *   <li>Vector configuration (dimension, similarity metric)</li>
 *   <li>Vectorization service settings</li>
 *   <li>Lexical search configuration</li>
 *   <li>Reranking service settings</li>
 * </ul>
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @DataApiCollection(
 *     value = "my_collection",
 *     defaultIdType = CollectionDefaultIdTypes.UUID,
 *     vectorDimension = 1536,
 *     vectorSimilarity = SimilarityMetric.COSINE,
 *     indexingDeny = {"internal_field", "temp_data"}
 * )
 * public class MyDocument {
 *     @DocumentId
 *     private String id;
 *     // ... other fields
 * }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataApiCollection {

    /**
     * Collection name. If not provided, the class name (lowercase) will be used.
     *
     * @return the collection name
     */
    String value() default "";

    // ---------------------
    // DefaultId Options
    // ---------------------

    /**
     * The default ID type for documents in this collection.
     * Defaults to UNDEFINED (no default ID type specified).
     *
     * @return the default ID type
     */
    String defaultIdType() default "";

    // ---------------------
    // Indexing Options
    // ---------------------

    /**
     * List of field names to exclude from indexing.
     * Mutually exclusive with {@link #indexingAllow()}.
     *
     * @return array of field names to deny indexing
     */
    String[] indexingDeny() default {};

    /**
     * List of field names to include in indexing (only these will be indexed).
     * Mutually exclusive with {@link #indexingDeny()}.
     *
     * @return array of field names to allow indexing
     */
    String[] indexingAllow() default {};

    // ---------------------
    // Vector Options
    // ---------------------

    /**
     * Vector dimension size. Must be greater than 0 to enable vector search.
     * Default is -1 (no vector configuration).
     *
     * @return the vector dimension
     */
    int vectorDimension() default -1;

    /**
     * Similarity metric for vector search.
     * Only used when {@link #vectorDimension()} is specified.
     *
     * @return the similarity metric
     */
    SimilarityMetric vectorSimilarity() default SimilarityMetric.COSINE;

    // ---------------------
    // Vectorize Options
    // ---------------------

    /**
     * Vectorization service provider (e.g., "openai", "huggingface").
     * When specified, enables automatic vectorization.
     *
     * @return the vectorization provider
     */
    String vectorizeProvider() default "";

    /**
     * Vectorization model name (e.g., "text-embedding-ada-002").
     * Required when {@link #vectorizeProvider()} is specified.
     *
     * @return the model name
     */
    String vectorizeModel() default "";

    /**
     * Shared secret key name for vectorization service authentication.
     * Optional, used when the provider requires authentication.
     *
     * @return the shared secret key name
     */
    String vectorizeSharedSecret() default "";

    // ---------------------
    // Lexical Options
    // ---------------------

    /**
     * Enable or disable lexical search for this collection.
     * Default is true (enabled).
     *
     * @return true to enable lexical search, false to disable
     */
    boolean lexicalEnabled() default true;

    /**
     * Analyzer type for lexical search.
     * Only used when {@link #lexicalEnabled()} is true.
     *
     * @return the analyzer type
     */
    AnalyzerTypes lexicalAnalyzer() default AnalyzerTypes.STANDARD;

    // ---------------------
    // Rerank Options
    // ---------------------

    /**
     * Enable or disable reranking for this collection.
     * Default is false (disabled).
     *
     * @return true to enable reranking, false to disable
     */
    boolean rerankEnabled() default false;

    /**
     * Reranking service provider (e.g., "cohere").
     * Required when {@link #rerankEnabled()} is true.
     *
     * @return the reranking provider
     */
    String rerankProvider() default "";

    /**
     * Reranking model name (e.g., "rerank-english-v2.0").
     * Required when {@link #rerankEnabled()} is true.
     *
     * @return the reranking model name
     */
    String rerankModel() default "";
}
