package com.datastax.astra.test.integration.utils;

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

import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.definition.documents.types.ObjectId;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.test.integration.model.ProductString;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;

import java.time.Instant;
import java.util.*;

/**
 * Test dataset constants and sample documents for integration tests.
 */
public final class TestDataset {

    private TestDataset() {
        // utility class
    }

    // Collection names
    public static final String COLLECTION_SIMPLE = "collection_simple";
    public static final String COLLECTION_OBJECT_ID = "collection_objectid";
    public static final String COLLECTION_UUID = "collection_uuid";
    public static final String COLLECTION_UUID_V6 = "collection_uuidv6";
    public static final String COLLECTION_UUID_V7 = "collection_uuidv7";
    public static final String COLLECTION_VECTOR = "collection_vector";
    public static final String COLLECTION_VECTORIZE = "c_vectorize";
    public static final String COLLECTION_DENY = "collection_deny";
    public static final String COLLECTION_ALLOW = "collection_allow";

    public static CollectionDefinition COLLECTION_VECTOR_DEF = new CollectionDefinition()
            .disableLexical()
            .disableRerank()
            .vector(14, SimilarityMetric.COSINE);

    public static CollectionDefinition COLLECTION_VECTORIZE_DEF = new CollectionDefinition()
            .disableLexical()
            .disableRerank()
            .vector(1024, SimilarityMetric.COSINE)
            .vectorize("nvidia","NV-Embed-QA");

    // Table names
    public static final String TABLE_SIMPLE = "table_simple";
    public static final String TABLE_COMPOSITE = "table_composite_pk";
    public static final String TABLE_TYPES = "table_types";
    public static final String TABLE_CASSIO = "table_cassio";
    public static final String TABLE_ALL_RETURNS = "table_all_returns";

    // Sample song documents for vector search testing
    public static final List<Document> DOCS_SONG_DIRE_STRAITS = List.of(
            new Document(1).vectorize("A lovestruck Romeo sings the streets a serenade"),
            new Document(2).vectorize("Finds a streetlight, steps out of the shade"),
            new Document(3).vectorize("Says something like, You and me babe, how about it?"),
            new Document(4).vectorize("Juliet says,Hey, it's Romeo, you nearly gimme a heart attack"),
            new Document(5).vectorize("He's underneath the window"),
            new Document(6).vectorize("She's singing, Hey la, my boyfriend's back"),
            new Document(7).vectorize("You shouldn't come around here singing up at people like that"),
            new Document(8).vectorize("Anyway, what you gonna do about it?")
    );

    /**
     * Create a complete document with various data types for testing serialization.
     */
    public static Document createCompleteDocument() {
        return new Document().id("1")
                .append("metadata_instant", Instant.now())
                .append("metadata_date", new Date())
                .append("metadata_calendar", Calendar.getInstance())
                .append("metadata_int", 1)
                .append("metadata_objectId", new ObjectId())
                .append("metadata_long", 1232123323L)
                .append("metadata_double", 1213.343243d)
                .append("metadata_float", 1.1232434543f)
                .append("metadata_string", "hello")
                .append("metadata_short", Short.valueOf("1"))
                .append("metadata_string_array", new String[]{"a", "b", "c"})
                .append("metadata_int_array", new Integer[]{1, 2, 3})
                .append("metadata_long_array", new Long[]{1L, 2L, 3L})
                .append("metadata_double_array", new Double[]{1d, 2d, 3d})
                .append("metadata_float_array", new Float[]{1f, 2f, 3f})
                .append("metadata_short_array", new Short[]{1, 2, 3})
                .append("metadata_boolean", true)
                .append("metadata_boolean_array", new Boolean[]{true, false, true})
                .append("metadata_uuid", UUID.randomUUID())
                .append("metadata_uuid_array", new UUID[]{UUID.randomUUID(), UUID.randomUUID()})
                .append("metadata_map", Map.of("key1", "value1", "key2", "value2"))
                .append("metadata_list", List.of("value1", "value2"))
                .append("metadata_byte", Byte.valueOf("1"))
                .append("metadata_character", 'c')
                .append("metadata_enum", AstraDBAdmin.FREE_TIER_CLOUD)
                .append("metadata_enum_array", new CloudProviderType[]{AstraDBAdmin.FREE_TIER_CLOUD, CloudProviderType.AWS})
                .append("metadata_object", new ProductString("p1", "name", 10.1));
    }

    // For backward compatibility with tests using static COMPLETE_DOCUMENT
    public static final Document COMPLETE_DOCUMENT = createCompleteDocument();

    // Sample documents for testing various data types and structures

    // Books
    public static Document BOOK_HIDDEN_SHADOW = new Document()
    .append("title", "Hidden Shadows of the Past")
    .append("number_of_pages", 481)
    .append("genres", Set.of("Biography", "Graphic Novel", "Dystopian", "Drama"))
    .append("metadata", Map.of(
        "isbn", "978-1-905585-40-3",
        "language", "French",
        "edition", "Anniversary Edition"
    ));

    public static Document BOOK_ECHOES_IRON_SKY = new Document()
        .append("title", "Echoes Beneath the Iron Sky")
        .append("number_of_pages", 392)
        .append("genres", Set.of("Science Fiction", "Dystopian", "Drama"))
        .append("metadata", Map.of(
            "isbn", "978-1-402835-11-7",
            "language", "English",
            "edition", "First Edition"
        ));

    public static Document BOOK_LAST_CARTOGRAPHER = new Document()
        .append("title", "The Last Cartographer")
        .append("number_of_pages", 527)
        .append("genres", Set.of("Fantasy", "Adventure", "Epic"))
        .append("metadata", Map.of(
            "isbn", "978-0-992341-88-2",
            "language", "English",
            "edition", "Collector's Edition"
        ));

    public static Document BOOK_FRAGMENTS_SILENT_WAR = new Document()
        .append("title", "Fragments of a Silent War")
        .append("number_of_pages", 445)
        .append("genres", Set.of("Historical Fiction", "Drama", "War"))
        .append("metadata", Map.of(
            "isbn", "978-2-756403-92-9",
            "language", "French",
            "edition", "Revised Edition"
        ));

    public static List<Document> BOOKS = List.of(
        BOOK_HIDDEN_SHADOW,
        BOOK_ECHOES_IRON_SKY,
        BOOK_LAST_CARTOGRAPHER,
        BOOK_FRAGMENTS_SILENT_WAR
    );



}
