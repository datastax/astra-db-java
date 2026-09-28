package com.datastax.astra.test.unit.core;

import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.core.opensearch.OpenSearchQuery;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.collections.definition.documents.types.ObjectId;
import com.datastax.astra.internal.serdes.collections.DocumentSerializer;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FiltersTest {

    @Test
    void testFiltersSerializations() {
        Filter f = Filters.eq("hello", 3);
        assertThat(new DocumentSerializer().marshall(f)).isEqualTo("{\"hello\":3}");
    }

    @Test
    void shouldBuilderProjections() {
        CollectionFindOptions options = new CollectionFindOptions().projection(
                Projection.exclude(
                        DataAPIKeywords.ID.getKeyword(),
                        DataAPIKeywords.VECTOR.getKeyword()));
        assertThat(options.projection()).isNotNull();
    }

    @Test
    void workWithObjectId() {
        ObjectId oid1 = new ObjectId();
        ObjectId oid2 = new ObjectId(new Date());
        assertThat(oid1).isNotEqualTo(oid2);
        assertThat(oid1.hashCode()).isNotZero();
        assertThat(oid1.toString()).isNotNull();
        assertThat(oid1).isNotEqualByComparingTo(oid2);
    }

    @Test
    public void should_build_match_request() {
        System.out.println(Filters.match("tree hill"));
        System.out.println(new Filter(Map.of("$lexical", Map.of("$match", "tree hill"))));
    }

    @Test
    public void should_serialize_search_match_all() {
        Filter f = Filters.search(OpenSearchQuery.matchAll());
        String json = new DocumentSerializer().marshall(f);
        assertThat(json).isEqualTo("{\"$search\":{\"match_all\":{}}}");
    }

    @Test
    public void should_serialize_search_match_field() {
        Filter f = Filters.search(OpenSearchQuery.match("firstName", "Alice"));
        String json = new DocumentSerializer().marshall(f);
        assertThat(json).isEqualTo("{\"$search\":{\"match\":{\"firstName\":\"Alice\"}}}");
    }

    @Test
    public void should_serialize_search_bool_query() {
        Filter f = Filters.search(OpenSearchQuery.bool()
                .must(OpenSearchQuery.match("firstName", "Alice"))
                .filter(OpenSearchQuery.term("city", "London"))
                .build());
        String json = new DocumentSerializer().marshall(f);
        assertThat(json).contains("\"$search\"");
        assertThat(json).contains("\"bool\"");
        assertThat(json).contains("\"must\"");
        assertThat(json).contains("\"filter\"");
    }

    @Test
    public void should_serialize_search_raw_map() {
        Filter f = Filters.search(Map.of("match_all", Map.of()));
        String json = new DocumentSerializer().marshall(f);
        assertThat(json).isEqualTo("{\"$search\":{\"match_all\":{}}}");
    }

}
