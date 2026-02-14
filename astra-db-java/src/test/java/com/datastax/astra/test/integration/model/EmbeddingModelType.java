package com.datastax.astra.test.integration.model;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Enumeration of embedding model types used for vectorize testing.
 * Each entry defines the provider, model name, and vector dimension.
 */
public enum EmbeddingModelType {

    // Nvidia models
    NVIDIA_NEMO("nvidia", "NV-Embed-QA", 1024),

    // OpenAI models
    OPENAI_ADA002("openai", "text-embedding-ada-002", 1536),
    OPENAI_3_SMALL("openai", "text-embedding-3-small", 1536),
    OPENAI_3_LARGE("openai", "text-embedding-3-large", 3072),

    // Azure OpenAI models
    AZURE_OPENAI_SMALL("azure_openai", "text-embedding-3-small", 512),
    AZURE_OPENAI_LARGE("azure_openai", "text-embedding-3-large", 1024),
    AZURE_OPENAI_ADA002("azure_openai", "text-embedding-ada-002", 1536),

    // HuggingFace models
    HF_MINI_LM_L6("huggingface", "sentence-transformers/all-MiniLM-L6-v2", 384),

    // Vertex AI models
    VERTEX_AI_GECKO_003("vertexai", "textembedding-gecko@003", 768),

    // JinaAI models
    JINA_AI_EMBEDDINGS_V2_EN("jinaai", "jina-embeddings-v2-base-en", 768),
    JINA_AI_EMBEDDINGS_V2_DE("jinaai", "jina-embeddings-v2-base-de", 768),
    JINA_AI_EMBEDDINGS_V2_ES("jinaai", "jina-embeddings-v2-base-es", 768),
    JINA_AI_EMBEDDINGS_V2_ZH("jinaai", "jina-embeddings-v2-base-zh", 768),
    JINA_AI_EMBEDDINGS_V2_CODE("jinaai", "jina-embeddings-v2-base-code", 768),

    // MistralAI models
    MISTRAL_AI("mistralai", "mistral-embed", 1024),

    // VoyageAI models
    VOYAGE_AI_2("voyageai", "voyage-2", 1024),
    VOYAGE_AI_LAW_2("voyageai", "voyage-law-2", 1024),
    VOYAGE_AI_CODE_2("voyageai", "voyage-code-2", 1536),
    VOYAGE_AI_LARGE_2("voyageai", "voyage-large-2", 1536),
    VOYAGE_AI_LITE_INSTRUCT("voyageai", "voyage-lite-02-instruct", 1024),

    // UpstageAI models
    UPSTAGE_AI_SOLAR_MINI_1_QUERY("upstageai", "solar-1-mini-embedding-query", 4096),
    UPSTAGE_AI_SOLAR_MINI_1_PASSAGE("upstageai", "solar-1-mini-embedding-passage", 4096),

    // Cohere models
    COHERE_EMBED_ENGLISH_V2("cohere", "embed-english-v2.0", 4096),
    COHERE_EMBED_ENGLISH_V3("cohere", "embed-english-v3.0", 1024);

    private final String provider;
    private final String name;
    private final int dimension;

    EmbeddingModelType(String provider, String name, int dimension) {
        this.provider = provider;
        this.name = name;
        this.dimension = dimension;
    }

    /**
     * Gets the provider ID.
     *
     * @return value of provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Gets the model name.
     *
     * @return value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the vector dimension.
     *
     * @return value of dimension
     */
    public int getDimension() {
        return dimension;
    }
}
