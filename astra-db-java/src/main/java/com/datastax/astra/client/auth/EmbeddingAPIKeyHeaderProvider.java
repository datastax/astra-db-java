package com.datastax.astra.client.auth;

import java.util.Map;

/**
 * Default header when working with an embedding service provider.
 */
public class EmbeddingAPIKeyHeaderProvider implements EmbeddingHeadersProvider {

    /** Headers param with the embedding ApiKey when dealing with a embedding service provider. */
    public static String HEADER_EMBEDDING_API_KEY = "x-embedding-api-key";

    private final String apiKey;

    /**
     * Constructor.
     *
     * @param apiKey
     *      the apiKey to use for the embedding provider.
     */
    public EmbeddingAPIKeyHeaderProvider(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Build the Header for this provider.
     *
     * @return
     *      headers to use for the embedding provider.
     */
    @Override
    public Map<String, String> getHeaders() {
        return Map.of(HEADER_EMBEDDING_API_KEY, apiKey);
    }
}
