package com.datastax.astra.client.model.insert;

import com.datastax.astra.client.DataAPIClientOptions;
import lombok.Builder;
import lombok.Data;

/**
 * Options for InsertMany
 */
@Data @Builder
public class InsertManyOptions {

    /**
     * If the flag is set to true the command is failing on first error
     */
    @Builder.Default
    private boolean ordered = false;

    /**
     * If the flag is set to true the command is failing on first error
     */
    @Builder.Default
    private int concurrency = 1;

    /**
     * If the flag is set to true the command is failing on first error
     */
    @Builder.Default
    private int chunkSize = DataAPIClientOptions.getMaxDocumentsInInsert();

    /**
     * If the flag is set to true the command is failing on first error
     */
    @Builder.Default
    private int timeout = DataAPIClientOptions.DEFAULT_REQUEST_TIMEOUT_SECONDS * 1000;




}
