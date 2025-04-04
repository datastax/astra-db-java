package com.datastax.astra.test.unit;

import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.internal.serdes.collections.DocumentSerializer;
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
                .withOptions(new CollectionDefinition()
                    .vector(14, SimilarityMetric.COSINE));
        assertThat(new DocumentSerializer().marshall(ccc)).contains(SimilarityMetric.COSINE.getValue());
    }

    @Test
    void shouldSerializeCommand2() {
        assertThat(new DocumentSerializer().marshall(new Object())).isEqualTo("{}");
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
