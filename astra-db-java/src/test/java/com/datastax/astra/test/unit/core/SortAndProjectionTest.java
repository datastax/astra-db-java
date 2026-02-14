package com.datastax.astra.test.unit.core;

import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.query.SortOrder;
import com.datastax.astra.client.core.vector.DataAPIVector;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Sort and Projection query utilities.
 */
class SortAndProjectionTest {

    // --------------------------------------------------
    // Sort — ascending / descending
    // --------------------------------------------------

    @Test
    void shouldCreateAscendingSort() {
        Sort sort = Sort.ascending("name");
        assertThat(sort.getField()).isEqualTo("name");
        assertThat(sort.getOrder()).isEqualTo(SortOrder.ASCENDING);
        assertThat(sort.getValue()).isEqualTo(1);
    }

    @Test
    void shouldCreateDescendingSort() {
        Sort sort = Sort.descending("created_at");
        assertThat(sort.getField()).isEqualTo("created_at");
        assertThat(sort.getOrder()).isEqualTo(SortOrder.DESCENDING);
        assertThat(sort.getValue()).isEqualTo(-1);
    }

    @Test
    void shouldCreateAscendingSortFromSegments() {
        Sort sort = Sort.ascending(new String[]{"a", "b"});
        assertThat(sort.getField()).isNotNull();
        assertThat(sort.getOrder()).isEqualTo(SortOrder.ASCENDING);
    }

    @Test
    void shouldCreateDescendingSortFromSegments() {
        Sort sort = Sort.descending(new String[]{"x", "y"});
        assertThat(sort.getField()).isNotNull();
        assertThat(sort.getOrder()).isEqualTo(SortOrder.DESCENDING);
    }

    // --------------------------------------------------
    // Sort — vector
    // --------------------------------------------------

    @Test
    void shouldCreateVectorSort() {
        Sort sort = Sort.vector(new float[]{0.1f, 0.2f, 0.3f});
        assertThat(sort.getField()).isEqualTo("$vector");
        assertThat(sort.getVector()).isNotNull();
        assertThat(sort.getVector().getEmbeddings()).containsExactly(0.1f, 0.2f, 0.3f);
    }

    @Test
    void shouldCreateVectorSortWithFieldName() {
        Sort sort = Sort.vector("my_vec", new float[]{1.0f, 2.0f});
        assertThat(sort.getField()).isEqualTo("my_vec");
        assertThat(sort.getVector()).isNotNull();
    }

    @Test
    void shouldCreateVectorSortWithDataAPIVector() {
        DataAPIVector vec = new DataAPIVector(new float[]{0.5f, 0.6f});
        Sort sort = Sort.vector("col", vec);
        assertThat(sort.getField()).isEqualTo("col");
        assertThat(sort.getVector()).isSameAs(vec);
    }

    @Test
    void shouldReturnVectorAsValue() {
        Sort sort = Sort.vector(new float[]{1.0f});
        assertThat(sort.getValue()).isInstanceOf(DataAPIVector.class);
    }

    // --------------------------------------------------
    // Sort — vectorize
    // --------------------------------------------------

    @Test
    void shouldCreateVectorizeSort() {
        Sort sort = Sort.vectorize("some search text");
        assertThat(sort.getField()).isEqualTo("$vectorize");
        assertThat(sort.getPassage()).isEqualTo("some search text");
    }

    @Test
    void shouldCreateVectorizeSortWithFieldName() {
        Sort sort = Sort.vectorize("my_field", "query text");
        assertThat(sort.getField()).isEqualTo("my_field");
        assertThat(sort.getPassage()).isEqualTo("query text");
    }

    @Test
    void shouldReturnPassageAsValue() {
        Sort sort = Sort.vectorize("hello");
        assertThat(sort.getValue()).isEqualTo("hello");
    }

    // --------------------------------------------------
    // Sort — hybrid
    // --------------------------------------------------

    @Test
    void shouldCreateHybridSortFromString() {
        Sort sort = Sort.hybrid("search text");
        assertThat(sort.getField()).isEqualTo("$hybrid");
        assertThat(sort.getHybrid()).isNotNull();
    }

    @Test
    void shouldCreateHybridSortWithVectorizeAndLexical() {
        Sort sort = Sort.hybrid("vectorize text", "lexical text");
        assertThat(sort.getField()).isEqualTo("$hybrid");
        assertThat(sort.getHybrid()).isNotNull();
    }

    // --------------------------------------------------
    // Sort — lexical
    // --------------------------------------------------

    @Test
    void shouldCreateLexicalSort() {
        Sort sort = Sort.lexical("search query");
        assertThat(sort.getField()).isEqualTo("$lexical");
        assertThat(sort.getPassage()).isEqualTo("search query");
    }

    @Test
    void shouldCreateLexicalSortWithFieldName() {
        Sort sort = Sort.lexical("content_field", "full text search");
        assertThat(sort.getField()).isEqualTo("content_field");
        assertThat(sort.getPassage()).isEqualTo("full text search");
    }

    // --------------------------------------------------
    // Sort — validation
    // --------------------------------------------------

    @Test
    void shouldRejectSortWithNoOrderOrVector() {
        assertThatThrownBy(() -> Sort.internalBuilder().field("f").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("order");
    }

    // --------------------------------------------------
    // SortOrder enum
    // --------------------------------------------------

    @Test
    void shouldHaveCorrectSortOrderCodes() {
        assertThat(SortOrder.ASCENDING.getCode()).isEqualTo(1);
        assertThat(SortOrder.DESCENDING.getCode()).isEqualTo(-1);
    }

    // --------------------------------------------------
    // Projection — include
    // --------------------------------------------------

    @Test
    void shouldIncludeSingleField() {
        Projection[] projections = Projection.include("name");
        assertThat(projections).hasSize(1);
        assertThat(projections[0].getField()).isEqualTo("name");
        assertThat(projections[0].getPresent()).isTrue();
    }

    @Test
    void shouldIncludeMultipleFields() {
        Projection[] projections = Projection.include("name", "age", "email");
        assertThat(projections).hasSize(3);
        for (Projection p : projections) {
            assertThat(p.getPresent()).isTrue();
        }
    }

    // --------------------------------------------------
    // Projection — exclude
    // --------------------------------------------------

    @Test
    void shouldExcludeSingleField() {
        Projection[] projections = Projection.exclude("password");
        assertThat(projections).hasSize(1);
        assertThat(projections[0].getField()).isEqualTo("password");
        assertThat(projections[0].getPresent()).isFalse();
    }

    @Test
    void shouldExcludeMultipleFields() {
        Projection[] projections = Projection.exclude("secret", "token");
        assertThat(projections).hasSize(2);
        for (Projection p : projections) {
            assertThat(p.getPresent()).isFalse();
        }
    }

    // --------------------------------------------------
    // Projection — slice
    // --------------------------------------------------

    @Test
    void shouldCreateSliceProjection() {
        Projection proj = Projection.slice("tags", 0, 5);
        assertThat(proj.getField()).isEqualTo("tags");
        assertThat(proj.getSliceStart()).isEqualTo(0);
        assertThat(proj.getSliceEnd()).isEqualTo(5);
        assertThat(proj.getPresent()).isNull();
    }

    @Test
    void shouldCreateNegativeSliceProjection() {
        Projection proj = Projection.slice("items", -3, 2);
        assertThat(proj.getSliceStart()).isEqualTo(-3);
        assertThat(proj.getSliceEnd()).isEqualTo(2);
    }

    // --------------------------------------------------
    // Projection constructor
    // --------------------------------------------------

    @Test
    void shouldCreateProjectionWithConstructor() {
        Projection proj = new Projection("field", true);
        assertThat(proj.getField()).isEqualTo("field");
        assertThat(proj.getPresent()).isTrue();
        assertThat(proj.getSliceStart()).isNull();
        assertThat(proj.getSliceEnd()).isNull();
    }
}
