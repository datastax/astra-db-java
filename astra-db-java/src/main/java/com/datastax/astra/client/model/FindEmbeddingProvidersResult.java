package com.datastax.astra.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * Wrapper to get information about the findEmbeddingProviders.
 */
@Data @AllArgsConstructor
public class FindEmbeddingProvidersResult {

    Map<String, EmbeddingProvider> embeddingProviders;

}
