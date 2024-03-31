package com.datastax.astra.test.unit;

import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.internal.utils.JsonUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test different use case of client serialization.
 */
class DocumentSerializationTest {
    @Test
    void shouldSerializeAsJson() {
        String json = "{\"hello\":\"world\"}";
        assertThat(new Document().append("hello", "world").toJson()).isEqualTo(json);
        Document doc1 = Document.parse(json);
        assertThat(doc1.getString("hello")).isEqualTo("world");
    }

    @Test
    void shouldSerializeCommand() {
        Command ccc = Command.create("createCollection")
                .append("name", "demo")
                .withOptions(CollectionOptions.builder()
                    .vectorDimension(14)
                    .vectorSimilarity(SimilarityMetric.cosine)
                    .build());
        System.out.println(JsonUtils.marshall(ccc));
    }

    @Test
    void shouldSerializeCommand2() {
        assertThat(JsonUtils.marshall(new Object())).isEqualTo("{}");
    }
}
