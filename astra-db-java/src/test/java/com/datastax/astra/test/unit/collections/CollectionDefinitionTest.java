package com.datastax.astra.test.unit.collections;

import com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vector.SourceModelTypes;
import com.datastax.astra.client.core.vector.VectorOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    // --------------------------------------------------
    // SourceModelTypes enum tests
    // --------------------------------------------------

    @Test
    void shouldHaveAllSourceModelTypes() {
        assertThat(SourceModelTypes.ADA002.getValue()).isEqualTo("ada002");
        assertThat(SourceModelTypes.BERT.getValue()).isEqualTo("bert");
        assertThat(SourceModelTypes.COHERE_V3.getValue()).isEqualTo("cohere-v3");
        assertThat(SourceModelTypes.GECKO.getValue()).isEqualTo("gecko");
        assertThat(SourceModelTypes.OTHER.getValue()).isEqualTo("other");
        assertThat(SourceModelTypes.NV_QA_4.getValue()).isEqualTo("nv-qa-4");
        assertThat(SourceModelTypes.OPENAI_V3_LARGE.getValue()).isEqualTo("openai-v3-large");
        assertThat(SourceModelTypes.OPENAI_V3_SMALL.getValue()).isEqualTo("openai-v3-small");
    }

    @Test
    void shouldParseSourceModelFromValue() {
        assertThat(SourceModelTypes.fromValue("ada002")).isEqualTo(SourceModelTypes.ADA002);
        assertThat(SourceModelTypes.fromValue("openai-v3-small")).isEqualTo(SourceModelTypes.OPENAI_V3_SMALL);
        assertThat(SourceModelTypes.fromValue("cohere-v3")).isEqualTo(SourceModelTypes.COHERE_V3);
        assertThat(SourceModelTypes.fromValue("nv-qa-4")).isEqualTo(SourceModelTypes.NV_QA_4);
    }

    @Test
    void shouldReturnNullForUnknownSourceModel() {
        assertThat(SourceModelTypes.fromValue("unknown-model")).isNull();
        assertThat(SourceModelTypes.fromValue(null)).isNull();
    }

    @Test
    void shouldConvertSourceModelToString() {
        assertThat(SourceModelTypes.OPENAI_V3_SMALL.toString()).isEqualTo("openai-v3-small");
        assertThat(SourceModelTypes.BERT.toString()).isEqualTo("bert");
    }

    // --------------------------------------------------
    // VectorOptions sourceModel tests
    // --------------------------------------------------

    @Test
    void shouldSetSourceModelWithString() {
        VectorOptions opts = new VectorOptions().sourceModel("openai-v3-small");
        assertThat(opts.getSourceModel()).isEqualTo("openai-v3-small");
    }

    @Test
    void shouldSetSourceModelWithEnum() {
        VectorOptions opts = new VectorOptions().sourceModel(SourceModelTypes.OPENAI_V3_SMALL);
        assertThat(opts.getSourceModel()).isEqualTo("openai-v3-small");
        assertThat(opts.getSourceModelType()).isEqualTo(SourceModelTypes.OPENAI_V3_SMALL);
    }

    @Test
    void shouldGetSourceModelTypeFromString() {
        VectorOptions opts = new VectorOptions().sourceModel("ada002");
        assertThat(opts.getSourceModelType()).isEqualTo(SourceModelTypes.ADA002);
    }

    @Test
    void shouldReturnNullForUnknownSourceModelType() {
        VectorOptions opts = new VectorOptions().sourceModel("unknown-model");
        assertThat(opts.getSourceModelType()).isNull();
    }

    @Test
    void shouldHandleNullSourceModelEnum() {
        VectorOptions opts = new VectorOptions().sourceModel((SourceModelTypes) null);
        assertThat(opts.getSourceModel()).isNull();
    }

    // --------------------------------------------------
    // CollectionDefinition sourceModel tests
    // --------------------------------------------------

    @Test
    void shouldSetVectorSourceModelWithString() {
        CollectionDefinition def = new CollectionDefinition()
                .vectorDimension(1536)
                .vectorSourceModel("openai-v3-small");
        assertThat(def.getVector().getSourceModel()).isEqualTo("openai-v3-small");
    }

    @Test
    void shouldSetVectorSourceModelWithEnum() {
        CollectionDefinition def = new CollectionDefinition()
                .vectorDimension(1536)
                .vectorSourceModel(SourceModelTypes.OPENAI_V3_LARGE);
        assertThat(def.getVector().getSourceModel()).isEqualTo("openai-v3-large");
        assertThat(def.getVector().getSourceModelType()).isEqualTo(SourceModelTypes.OPENAI_V3_LARGE);
    }

    @Test
    void shouldCreateVectorOptionsWhenSettingSourceModel() {
        CollectionDefinition def = new CollectionDefinition()
                .vectorSourceModel(SourceModelTypes.BERT);
        assertThat(def.getVector()).isNotNull();
        assertThat(def.getVector().getSourceModel()).isEqualTo("bert");
    }

    // --------------------------------------------------
    // JSON serialization tests
    // --------------------------------------------------

    @Test
    void shouldSerializeSourceModelAsSnakeCase() throws Exception {
        VectorOptions opts = new VectorOptions()
                .dimension(1536)
                .metric("cosine")
                .sourceModel(SourceModelTypes.OPENAI_V3_SMALL);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(opts);

        assertThat(json).contains("\"source_model\":\"openai-v3-small\"");
        assertThat(json).doesNotContain("\"sourceModel\"");
    }

    @Test
    void shouldDeserializeSourceModelFromSnakeCase() throws Exception {
        String json = "{\"dimension\":1536,\"metric\":\"cosine\",\"source_model\":\"openai-v3-small\"}";

        ObjectMapper mapper = new ObjectMapper();
        VectorOptions opts = mapper.readValue(json, VectorOptions.class);

        assertThat(opts.getSourceModel()).isEqualTo("openai-v3-small");
        assertThat(opts.getSourceModelType()).isEqualTo(SourceModelTypes.OPENAI_V3_SMALL);
    }

    @Test
    void shouldBuildCompleteDefinitionWithSourceModel() {
        CollectionDefinition def = new CollectionDefinition()
                .defaultId(CollectionDefaultIdTypes.UUIDV7)
                .vector(1536, SimilarityMetric.COSINE)
                .vectorSourceModel(SourceModelTypes.OPENAI_V3_SMALL)
                .vectorize("openai", "text-embedding-3-small")
                .indexingAllow("name", "content");

        assertThat(def.getVector().getDimension()).isEqualTo(1536);
        assertThat(def.getVector().getMetric()).isEqualTo("cosine");
        assertThat(def.getVector().getSourceModel()).isEqualTo("openai-v3-small");
        assertThat(def.getVector().getService()).isNotNull();
    }
}
