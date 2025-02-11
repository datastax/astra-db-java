package com.datastax.astra.tool.loader.csv;

import lombok.Builder;

@Builder
public class CsvLoaderSettings {

    private static final int BATCH_SIZE = 20;

    private static final int THREAD_POOL_SIZE = 5;

    private static final int TIMEOUT = 1800;

    @Builder.Default
    int batchSize = BATCH_SIZE;;

    @Builder.Default
    int threadPoolSize = THREAD_POOL_SIZE;;

    @Builder.Default
    int timeoutSeconds = TIMEOUT;
}
