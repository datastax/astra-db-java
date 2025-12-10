package com.datastax.astra.test;

/**
 * Group Embeddings Models
 */
public enum EmbeddingModelType {

    NVIDIA_NEMO("nvidia", "NV-Embed-QA", 1024),

    // OpenAI models
    OPENAI_ADA002("openai", "text-embedding-ada-002", 1536),
    OPENAI_3_SMALL("openai", "text-embedding-3-small", 1536),
    OPENAI_3_LARGE("openai", "text-embedding-3-large", 3072),

    AZURE_OPENAI_SMALL("azure_openai", "text-embedding-3-small", 512),
    AZURE_OPENAI_LARGE("azure_openai", "text-embedding-3-large", 1024),
    AZURE_OPENAI_ADA002("azure_openai", "text-embedding-ada-002", 1536),

    HF_MINI_LM_L6("huggingface", "sentence-transformers/all-MiniLM-L6-v2", 384),
    //HF_SNOWFLAKE_ARCTIC("huggingface", "Snowflake/snowflake-arctic-embed-m", 768),
    //HF_ALL_MPNET("huggingface", "sentence-transformers/all-mpnet-base-v2", 768),
    //HF_MINI_LM_L12("huggingface", "paraphrase-multilingual-MiniLM-L12-v2", 384),

    VERTEX_AI_GECKO_003("vertexai", "textembedding-gecko@003", 768),

    JINA_AI_EMBEDDINGS_V2_EN("jinaai", "jina-embeddings-v2-base-en", 768),
    JINA_AI_EMBEDDINGS_V2_DE("jinaai", "jina-embeddings-v2-base-de", 768),
    JINA_AI_EMBEDDINGS_V2_ES("jinaai", "jina-embeddings-v2-base-es", 768),
    JINA_AI_EMBEDDINGS_V2_ZH("jinaai", "jina-embeddings-v2-base-zh", 768),
    JINA_AI_EMBEDDINGS_V2_CODE("jinaai", "jina-embeddings-v2-base-code", 768),

    MISTRAL_AI("mistralai", "mistral-embed", 1024),

    VOYAGE_AI_2("voyageai", " voyage-2", 1024),
    VOYAGE_AI_LAW_2("voyageai", " voyage-law-2", 1024),
    VOYAGE_AI_CODE_2("voyageai", " voyage-code-2", 1536),
    VOYAGE_AI_LARGE_2("voyageai", " voyage-large-2", 1536),
    VOYAGE_AI_LITE_INSTRUCT("voyageai", "voyage-lite-02-instruct", 1024),

    UPSTAGE_AI_SOLAR_MINI_1_QUERY("upstageai", "solar-1-mini-embedding-query", 4096),
    UPSTAGE_AI_SOLAR_MINI_1_PASSAGE("upstageai", "solar-1-mini-embedding-passage", 4096),

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
