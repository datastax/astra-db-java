package com.datastax.astra.internal.utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utilities to work with Async functions.
 */
public class CompletableFutures {

    /**
     * Hide constructor in utilities.
     */
    private CompletableFutures() {}

    /**
     * Merge multiple CompletionStage in a single one
     * @param inputs
     *      list of completion stages
     * @return
     *      the merged stage
     * @param <T>
     *      generic used with stages
     */
    public static <T> CompletionStage<Void> allDone(List<CompletionStage<T>> inputs) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        if (inputs.isEmpty()) {
            result.complete(null);
        } else {
            final int todo = inputs.size();
            final AtomicInteger done = new AtomicInteger();
            for (CompletionStage<?> input : inputs) {
                input.whenComplete(
                        (v, error) -> {
                            if (done.incrementAndGet() == todo) {
                                result.complete(null);
                            }
                        });
            }
        }
        return result;
    }
}