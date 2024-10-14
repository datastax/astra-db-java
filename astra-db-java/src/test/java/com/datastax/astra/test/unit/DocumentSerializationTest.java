package com.datastax.astra.test.unit;

import com.datastax.astra.client.model.collections.CollectionOptions;
import com.datastax.astra.client.model.command.Command;
import com.datastax.astra.client.model.collections.Document;
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
                    .vectorSimilarity(SimilarityMetric.COSINE)
                    .build());
        assertThat(JsonUtils.marshall(ccc)).contains(SimilarityMetric.COSINE.getValue());
    }

    @Test
    void shouldSerializeCommand2() {
        assertThat(JsonUtils.marshall(new Object())).isEqualTo("{}");
    }

    @Test
    void shouldSerializeDocument() {
        Document doc1 = new Document().append("hello", "world")
                .vector(new float[] {1.0f, 2.0f})
                .vectorize("hello");
        assertThat(doc1.getVector()).isPresent();
        assertThat(doc1.getVectorize()).isPresent();
        assertThat(doc1.getSimilarity()).isEmpty();
    }
}
