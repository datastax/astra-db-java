package com.datastax.astra.test.integration.collection_vectorize;

/**
 * Group Embeddings Models
 */
public enum EmbeddingModel {

    // OpenAI models
    OPENAI_ADA002("openai", "text-embedding-ada-002", 1536),
    OPENAI_3_SMALL("openai", "text-embedding-3-small", 1536),
    OPENAI_3_LARGE("openai", "text-embedding-3-large", 3072),

    AZURE_OPENAI_SMALL("azureOpenAI", "text-embedding-3-small", 512),
    AZURE_OPENAI_SMALL_SHARED("azureOpenAI", "text-embedding-3-small", 512),
    AZURE_OPENAI_LARGE("azureOpenAI", "text-embedding-3-large", 1024),
    AZURE_OPENAI_ADA002("azureOpenAI", "text-embedding-ada-002", 1536),

    NVIDIA_NEMO("nvidia", "NV-Embed-QA", 1024),

    HF_MINI_LM_L6("huggingface", "sentence-transformers/all-MiniLM-L6-v2", 384),
    HF_E5_LARGE("huggingface", "intfloat/multilingual-e5-large", 1024),
    HF_E5_LARGE_INSTRUCT("huggingface", "multilingual-e5-large-instruct", 1024),
    HF_BGE_SMALL("huggingface", "bge-small-en-v1.5", 384),
    HF_BGE_BASE("huggingface", "BAAI/bge-base-en-v1.5", 768),
    HF_BGE_LARGE("huggingface", "BAAI/bge-large-en-v1.5", 1024),

    VERTEX_AI_GECKO_003("vertexai", "textembedding-gecko@003", 768),

    JINA_AI_EMBEDDINGS_V2_EN("jinaai", "jina-embeddings-v2-base-en", 768),
    JINA_AI_EMBEDDINGS_V2_DE("jinaai", "jina-embeddings-v2-base-de", 768),
    JINA_AI_EMBEDDINGS_V2_ES("jinaai", "jina-embeddings-v2-base-es", 768),
    JINA_AI_EMBEDDINGS_V2_ZH("jinaai", "jina-embeddings-v2-base-zh", 768),
    JINA_AI_EMBEDDINGS_V2_CODE("jinaai", "jina-embeddings-v2-base-code", 768),

    MISTRAL_AI("mistral", "mistral-embed", 1024),

    VOYAGE_AI_2("voyageai", " voyage-2", 1024),
    VOYAGE_AI_LAW_2("voyageai", " voyage-law-2", 1024),
    VOYAGE_AI_CODE_2("voyageai", " voyage-code-2", 1536),
    VOYAGE_AI_LARGE_2("voyageai", " voyage-large-2", 1536),
    VOYAGE_AI_LITE_INSTRUCT("voyageai", "voyage-lite-02-instruct", 1024),

    UPSTAGE_AI_SOLAR_MINI_1_QUERY("upstageAI", "solar-1-mini-embedding", 4096),

    COHERE_EMBED_ENGLISH_V2("cohere", "embed-english-v2.0", 4096),
    COHERE_EMBED_ENGLISH_V3("cohere", "embed-english-v3.0", 1024);

    private final String provider;

    private final String name;

    private final int dimension;

    EmbeddingModel(String provider, String name, int dimension) {
        this.provider = provider;
        this.name = name;
        this.dimension = dimension;
    }

    /**
     * Gets provider
     *
     * @return value of provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Gets name
     *
     * @return value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets dimension
     *
     * @return value of dimension
     */
    public int getDimension() {
        return dimension;
    }
}
