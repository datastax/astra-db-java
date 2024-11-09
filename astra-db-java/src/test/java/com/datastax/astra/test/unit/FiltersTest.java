package com.datastax.astra.test.unit;

import com.datastax.astra.client.collections.options.CollectionFindOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.types.DataAPIKeywords;
import com.datastax.astra.client.core.types.ObjectId;
import com.datastax.astra.internal.serdes.collections.DocumentSerializer;
import org.junit.jupiter.api.Test;

import java.util.Date;

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


}
