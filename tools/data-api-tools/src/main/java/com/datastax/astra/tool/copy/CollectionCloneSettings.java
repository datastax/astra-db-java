package com.datastax.astra.tool.copy;

import com.datastax.astra.internal.serdes.collections.DocumentMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Settings for collection cloning operations.
 * Optimized for streaming cursor consumption and parallel batch insertion.
 */
@Getter
@Builder
public class CollectionCloneSettings {
    
    /**
     * Number of documents to fetch per batch from source collection.
     * Larger batches reduce API calls but increase memory usage.
     * Default: 100
     */
    @Builder.Default
    private final int batchSize = 100;
    
    /**
     * Number of parallel threads for inserting batches into target collection.
     * More threads = faster insertion but more resource usage.
     * Default: 10
     */
    @Builder.Default
    private final int insertThreadPoolSize = 10;
    
    /**
     * Maximum time in seconds to wait for all insertions to complete.
     * Default: 300 seconds (5 minutes)
     */
    @Builder.Default
    private final int timeoutSeconds = 300;
    
    /**
     * Whether to log progress during cloning.
     * Default: true
     */
    @Builder.Default
    private final boolean logProgress = true;
    
    /**
     * Optional document mapper to transform documents during cloning.
     * If not provided, documents are copied as-is without transformation.
     * Default: identity mapper (no transformation)
     */
    @NonNull
    @Builder.Default
    private final DocumentMapper documentMapper = DocumentMapper.identity();
}
