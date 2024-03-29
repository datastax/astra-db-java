package com.datastax.astra.test.unit;

import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.Filters;
import com.datastax.astra.internal.utils.JsonUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FiltersTest {

    @Test
    void testFiltersSerializations() {
        Filter f = Filters.eq("hello", 3);
        assertThat(JsonUtils.marshallForDataApi(f)).isEqualTo("{\"hello\":3}");
    }

}
