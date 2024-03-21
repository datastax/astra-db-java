package com.datastax.astra.client.observer;

import io.stargate.sdk.data.client.model.ExecutionInfos;

/**
 * By Registration a observer on a DataApiClient you can execute some treatment synchronously. It could be logging or pushing to a monitoring system.
 */
public interface DataApiCommandObserver {

    /**
     * Command Execution could lead to third party treatment run asynchronous.
     *
     * @param executionInfo
     *      command execution information
     *
     */
    void onCommand(ExecutionInfos executionInfo);
}
