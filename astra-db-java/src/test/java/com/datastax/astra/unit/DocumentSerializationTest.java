package com.datastax.astra.unit;

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
public class DocumentSerializationTest {
    @Test
    public void shouldSerializeAsJson() {
        String json = "{\"hello\":\"world\"}";
        assertThat(new Document().append("hello", "world").toJson()).isEqualTo(json);
        Document doc1 = Document.parse(json);
        assertThat(doc1.getString("hello")).isEqualTo("world");
    }

    @Test
    public void shouldSerializeCommand() {
        Command ccc = Command.create("createCollection")
                .append("name", "demo")
                .withOptions(CollectionOptions.builder()
                    .withVectorDimension(14)
                    .withVectorSimilarityMetric(SimilarityMetric.cosine)
                    .build());
        System.out.println(JsonUtils.marshallForDataApi(ccc));
    }

    @Test
    public void shouldSerializeCommand2() {
        assertThat(JsonUtils.marshallForDataApi(new Object())).isEqualTo("{}");
    }
}
