package com.datastax.astra.client.observer;

/**
 * By Registration a observer on a DataApiClient you can execute some treatment synchronously. It could be logging or pushing to a monitoring system.
 */
public interface CommandObserver {

    /**
     * Command Execution could lead to third party treatment run asynchronous.
     *
     * @param executionInfo
     *      command execution information
     *
     */
    void onCommand(ExecutionInfos executionInfo);
}
