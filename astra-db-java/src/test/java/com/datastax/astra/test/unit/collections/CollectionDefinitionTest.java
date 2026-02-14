package com.datastax.astra.test.unit.collections;

import com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for CollectionDefinition fluent builder.
 */
class CollectionDefinitionTest {

    // --------------------------------------------------
    // Default ID
    // --------------------------------------------------

    @Test
    void shouldSetDefaultIdObjectId() {
        CollectionDefinition def = new CollectionDefinition()
                .defaultId(CollectionDefaultIdTypes.OBJECT_ID);
        assertThat(def.getDefaultId()).isNotNull();
        assertThat(def.getDefaultId().getType()).isEqualTo(CollectionDefaultIdTypes.OBJECT_ID);
    }

    @Test
    void shouldSetDefaultIdUuidV6() {
        CollectionDefinition def = new CollectionDefinition()
                .defaultId(CollectionDefaultIdTypes.UUIDV6);
        assertThat(def.getDefaultId().getType()).isEqualTo(CollectionDefaultIdTypes.UUIDV6);
    }

    @Test
    void shouldSetDefaultIdUuidV7() {
        CollectionDefinition def = new CollectionDefinition()
                .defaultId(CollectionDefaultIdTypes.UUIDV7);
        assertThat(def.getDefaultId().getType()).isEqualTo(CollectionDefaultIdTypes.UUIDV7);
    }

    @Test
    void shouldSetDefaultIdUuid() {
        CollectionDefinition def = new CollectionDefinition()
                .defaultId(CollectionDefaultIdTypes.UUID);
        assertThat(def.getDefaultId().getType()).isEqualTo(CollectionDefaultIdTypes.UUID);
    }

    // --------------------------------------------------
    // CollectionDefaultIdTypes enum
    // --------------------------------------------------

    @Test
    void shouldHaveExpectedIdTypeValues() {
        assertThat(CollectionDefaultIdTypes.OBJECT_ID.getValue()).isEqualTo("objectId");
        assertThat(CollectionDefaultIdTypes.UUIDV6.getValue()).isEqualTo("uuidv6");
        assertThat(CollectionDefaultIdTypes.UUIDV7.getValue()).isEqualTo("uuidv7");
        assertThat(CollectionDefaultIdTypes.UUID.getValue()).isEqualTo("uuid");
    }

    @Test
    void shouldParseFromValue() {
        assertThat(CollectionDefaultIdTypes.fromValue("objectId")).isEqualTo(CollectionDefaultIdTypes.OBJECT_ID);
        assertThat(CollectionDefaultIdTypes.fromValue("uuidv7")).isEqualTo(CollectionDefaultIdTypes.UUIDV7);
    }

    @Test
    void shouldThrowOnUnknownIdType() {
        assertThatThrownBy(() -> CollectionDefaultIdTypes.fromValue("unknown"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --------------------------------------------------
    // Vector options
    // --------------------------------------------------

    @Test
    void shouldSetVectorDimension() {
        CollectionDefinition def = new CollectionDefinition().vectorDimension(1536);
        assertThat(def.getVector()).isNotNull();
        assertThat(def.getVector().getDimension()).isEqualTo(1536);
    }

    @Test
    void shouldSetVectorSimilarity() {
        CollectionDefinition def = new CollectionDefinition()
                .vectorSimilarity(SimilarityMetric.COSINE);
        assertThat(def.getVector()).isNotNull();
        assertThat(def.getVector().getMetric()).isEqualTo("cosine");
    }

    @Test
    void shouldSetVectorDimensionAndSimilarity() {
        CollectionDefinition def = new CollectionDefinition()
                .vector(1536, SimilarityMetric.DOT_PRODUCT);
        assertThat(def.getVector().getDimension()).isEqualTo(1536);
        assertThat(def.getVector().getMetric()).isEqualTo("dot_product");
    }

    @Test
    void shouldSetVectorize() {
        CollectionDefinition def = new CollectionDefinition()
                .vectorDimension(1536)
                .vectorize("openai", "text-embedding-3-small");
        assertThat(def.getVector().getService()).isNotNull();
        assertThat(def.getVector().getService().getProvider()).isEqualTo("openai");
        assertThat(def.getVector().getService().getModelName()).isEqualTo("text-embedding-3-small");
    }

    @Test
    void shouldSetVectorizeWithSharedKey() {
        CollectionDefinition def = new CollectionDefinition()
                .vectorDimension(1536)
                .vectorize("openai", "text-embedding-3-small", "my_key");
        assertThat(def.getVector().getService().getAuthentication())
                .containsEntry("providerKey", "my_key");
    }

    // --------------------------------------------------
    // Indexing options
    // --------------------------------------------------

    @Test
    void shouldSetIndexingAllow() {
        CollectionDefinition def = new CollectionDefinition()
                .indexingAllow("name", "email");
        assertThat(def.getIndexing()).isNotNull();
        assertThat(def.getIndexing().getAllow()).containsExactly("name", "email");
    }

    @Test
    void shouldSetIndexingDeny() {
        CollectionDefinition def = new CollectionDefinition()
                .indexingDeny("blob_field", "large_text");
        assertThat(def.getIndexing()).isNotNull();
        assertThat(def.getIndexing().getDeny()).containsExactly("blob_field", "large_text");
    }

    @Test
    void shouldRejectAllowAfterDeny() {
        CollectionDefinition def = new CollectionDefinition()
                .indexingDeny("field1");
        assertThatThrownBy(() -> def.indexingAllow("field2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mutually exclusive");
    }

    @Test
    void shouldRejectDenyAfterAllow() {
        CollectionDefinition def = new CollectionDefinition()
                .indexingAllow("field1");
        assertThatThrownBy(() -> def.indexingDeny("field2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mutually exclusive");
    }

    // --------------------------------------------------
    // Lexical options
    // --------------------------------------------------

    @Test
    void shouldDisableLexical() {
        CollectionDefinition def = new CollectionDefinition().disableLexical();
        assertThat(def.getLexical()).isNotNull();
    }

    // --------------------------------------------------
    // Rerank options
    // --------------------------------------------------

    @Test
    void shouldSetRerankProvider() {
        CollectionDefinition def = new CollectionDefinition()
                .rerank("nvidia", "rerank-model");
        assertThat(def.getRerank()).isNotNull();
        assertThat(def.getRerank().getService()).isNotNull();
        assertThat(def.getRerank().getService().getProvider()).isEqualTo("nvidia");
        assertThat(def.getRerank().getService().getModelName()).isEqualTo("rerank-model");
    }

    @Test
    void shouldDisableRerank() {
        CollectionDefinition def = new CollectionDefinition().disableRerank();
        assertThat(def.getRerank()).isNotNull();
    }

    // --------------------------------------------------
    // Full builder composition
    // --------------------------------------------------

    @Test
    void shouldBuildCompleteDefinition() {
        CollectionDefinition def = new CollectionDefinition()
                .defaultId(CollectionDefaultIdTypes.UUIDV7)
                .vector(1536, SimilarityMetric.COSINE)
                .vectorize("openai", "text-embedding-3-small")
                .indexingAllow("name", "content")
                .rerank("nvidia", "rerank-v1");

        assertThat(def.getDefaultId().getType()).isEqualTo(CollectionDefaultIdTypes.UUIDV7);
        assertThat(def.getVector().getDimension()).isEqualTo(1536);
        assertThat(def.getVector().getService()).isNotNull();
        assertThat(def.getIndexing().getAllow()).hasSize(2);
        assertThat(def.getRerank().getService()).isNotNull();
    }
}
