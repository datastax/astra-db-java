package com.datastax.astra.client.auth;

import java.util.Map;

/**
 * To use the interface embeddings API the user needs to be authenticated against the embedding provider.
 * - You can set the header variable `x-embedding-api-key` to authenticate against the embedding provider.
 * - You can set multiple headers to authenticate against multiple embedding providers.
 */
public interface EmbeddingHeadersProvider {

    /**
     * Build the Header to authenticate against embeddings provider.
     *
     * @return
     *      headers to use for the embedding provider.
     */
    Map<String, String > getHeaders();
}
