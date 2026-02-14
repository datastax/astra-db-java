package com.datastax.astra.test.unit.core;

import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.FilterOperator;
import com.datastax.astra.client.core.query.Filters;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Filters utility and FilterOperator enum.
 */
class FilterOperatorsTest {

    // --------------------------------------------------
    // FilterOperator enum
    // --------------------------------------------------

    @Test
    void shouldHaveExpectedOperatorValues() {
        assertThat(FilterOperator.GREATER_THAN.getOperator()).isEqualTo("$gt");
        assertThat(FilterOperator.GREATER_THAN_OR_EQUALS_TO.getOperator()).isEqualTo("$gte");
        assertThat(FilterOperator.LESS_THAN.getOperator()).isEqualTo("$lt");
        assertThat(FilterOperator.LESS_THAN_OR_EQUALS_TO.getOperator()).isEqualTo("$lte");
        assertThat(FilterOperator.EQUALS_TO.getOperator()).isEqualTo("$eq");
        assertThat(FilterOperator.NOT_EQUALS_TO.getOperator()).isEqualTo("$ne");
        assertThat(FilterOperator.IN.getOperator()).isEqualTo("$in");
        assertThat(FilterOperator.NOT_IN.getOperator()).isEqualTo("$nin");
        assertThat(FilterOperator.EXISTS.getOperator()).isEqualTo("$exists");
        assertThat(FilterOperator.CONTAINS.getOperator()).isEqualTo("$contains");
        assertThat(FilterOperator.CONTAIN_KEY.getOperator()).isEqualTo("$containsKey");
        assertThat(FilterOperator.CONTAIN_ENTRY.getOperator()).isEqualTo("$containsEntry");
        assertThat(FilterOperator.MATCH.getOperator()).isEqualTo("$match");
    }

    @Test
    void shouldCoverAllOperators() {
        assertThat(FilterOperator.values()).hasSize(13);
    }

    // --------------------------------------------------
    // Filters.eq
    // --------------------------------------------------

    @Test
    void shouldCreateEqFilter() {
        Filter filter = Filters.eq("name", "Alice");
        assertThat(filter).isNotNull();
        assertThat(filter.get("name")).isEqualTo("Alice");
    }

    @Test
    void shouldCreateEqFilterForId() {
        Filter filter = Filters.eq("test-id");
        assertThat(filter).isNotNull();
        assertThat(filter.get("_id")).isEqualTo("test-id");
    }

    @Test
    void shouldCreateIdFilter() {
        Filter filter = Filters.id("my-id");
        assertThat(filter.get("_id")).isEqualTo("my-id");
    }

    // --------------------------------------------------
    // Filters.ne
    // --------------------------------------------------

    @Test
    void shouldCreateNeFilter() {
        Filter filter = Filters.ne("status", "deleted");
        assertThat(filter).isNotNull();
    }

    // --------------------------------------------------
    // Filters.gt / gte / lt / lte
    // --------------------------------------------------

    @Test
    void shouldCreateGtFilter() {
        Filter filter = Filters.gt("age", 18);
        assertThat(filter).isNotNull();
    }

    @Test
    void shouldCreateGtFilterWithInstant() {
        Instant now = Instant.now();
        Filter filter = Filters.gt("ts", now);
        assertThat(filter).isNotNull();
    }

    @Test
    void shouldCreateGteFilter() {
        Filter filter = Filters.gte("score", 90);
        assertThat(filter).isNotNull();
    }

    @Test
    void shouldCreateGteFilterWithInstant() {
        Instant now = Instant.now();
        Filter filter = Filters.gte("ts", now);
        assertThat(filter).isNotNull();
    }

    @Test
    void shouldCreateLtFilter() {
        Filter filter = Filters.lt("price", 100);
        assertThat(filter).isNotNull();
    }

    @Test
    void shouldCreateLtFilterWithInstant() {
        Instant now = Instant.now();
        Filter filter = Filters.lt("ts", now);
        assertThat(filter).isNotNull();
    }

    @Test
    void shouldCreateLteFilter() {
        Filter filter = Filters.lte("count", 50);
        assertThat(filter).isNotNull();
    }

    @Test
    void shouldCreateLteFilterWithInstant() {
        Instant now = Instant.now();
        Filter filter = Filters.lte("ts", now);
        assertThat(filter).isNotNull();
    }

    // --------------------------------------------------
    // Filters.in / nin
    // --------------------------------------------------

    @Test
    void shouldCreateInFilter() {
        Filter filter = Filters.in("status", "active", "pending");
        assertThat(filter).isNotNull();
    }

    @Test
    void shouldCreateInFilterWithoutField() {
        Filter filter = Filters.in("a", "b", "c");
        assertThat(filter).isNotNull();
    }

    @Test
    void shouldCreateNinFilter() {
        Filter filter = Filters.nin("color", "red", "blue");
        assertThat(filter).isNotNull();
    }

    // --------------------------------------------------
    // Filters.exists / all / hasSize
    // --------------------------------------------------

    @Test
    void shouldCreateExistsFilter() {
        Filter filter = Filters.exists("email");
        assertThat(filter).isNotNull();
    }

    @Test
    void shouldCreateAllFilter() {
        Filter filter = Filters.all("tags", "java", "sdk");
        assertThat(filter).isNotNull();
    }

    @Test
    void shouldCreateHasSizeFilter() {
        Filter filter = Filters.hasSize("items", 3);
        assertThat(filter).isNotNull();
    }

    // --------------------------------------------------
    // Filters.match
    // --------------------------------------------------

    @Test
    void shouldCreateMatchFilter() {
        Filter filter = Filters.match("search text");
        assertThat(filter).isNotNull();
    }

    @Test
    void shouldCreateMatchFilterWithField() {
        Filter filter = Filters.match("content", "hello world");
        assertThat(filter).isNotNull();
    }

    // --------------------------------------------------
    // Logical operators: and, or, not
    // --------------------------------------------------

    @Test
    void shouldCreateAndFilter() {
        Filter filter = Filters.and(
                Filters.eq("name", "Alice"),
                Filters.gt("age", 18));
        assertThat(filter).isNotNull();
        assertThat(filter.getDocumentMap()).containsKey("$and");
    }

    @Test
    void shouldCreateOrFilter() {
        Filter filter = Filters.or(
                Filters.eq("status", "active"),
                Filters.eq("status", "pending"));
        assertThat(filter).isNotNull();
        assertThat(filter.getDocumentMap()).containsKey("$or");
    }

    @Test
    void shouldCreateNotFilter() {
        Filter filter = Filters.not(Filters.eq("deleted", true));
        assertThat(filter).isNotNull();
        assertThat(filter.getDocumentMap()).containsKey("$not");
    }

    // --------------------------------------------------
    // Filters.values
    // --------------------------------------------------

    @Test
    void shouldCreateValuesFilter() {
        Filter filter = Filters.values("meta", Map.of("k1", "v1"));
        assertThat(filter).isNotNull();
    }

    // --------------------------------------------------
    // Complex filter composition
    // --------------------------------------------------

    @Test
    void shouldComposeComplexFilter() {
        Filter filter = Filters.and(
                Filters.eq("type", "user"),
                Filters.or(
                        Filters.gt("age", 18),
                        Filters.exists("parent_consent")),
                Filters.ne("status", "banned"));
        assertThat(filter).isNotNull();
        assertThat(filter.getDocumentMap()).containsKey("$and");
    }
}
