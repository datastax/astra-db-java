package com.datastax.astra.tool.copy;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.internal.serdes.collections.DocumentMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for cloning collections with parallel batch insertion.
 * Uses cursor-based streaming for memory efficiency and ExecutorService for concurrent batch insertion.
 */
@Slf4j
public class CollectionCloner {

    /**
     * Clone a collection with default settings.
     *
     * @param source source collection to clone from
     * @param target target collection to clone to
     * @throws Exception if cloning fails
     */
    public static void clone(Collection<Document> source, Collection<Document> target) throws Exception {
        clone(source, target, CollectionCloneSettings.builder().build());
    }

    /**
     * Clone a collection with custom settings for batch size, concurrency, cursor page size, and timeout.
     *
     * @param source source collection to clone from
     * @param target target collection to clone to
     * @param settings settings for batch size, thread pool, cursor page size, and timeout
     * @throws Exception if cloning fails
     */
    public static void clone(Collection<Document> source, 
                            Collection<Document> target,
                            CollectionCloneSettings settings) throws Exception {
        AtomicInteger counter = new AtomicInteger();
        AtomicInteger batchCounter = new AtomicInteger();
        long startTime = System.currentTimeMillis();
        
        List<Document> batch = new ArrayList<>(settings.getBatchSize());
        ExecutorService executor = Executors.newFixedThreadPool(settings.getInsertThreadPoolSize());

        try {
            if (settings.isLogProgress()) {
                log.info("Starting collection cloning from '{}' to '{}'", 
                        source.getCollectionName(), target.getCollectionName());
                log.info("Settings: batchSize={}, insertThreads={}, timeout={}s",
                        settings.getBatchSize(), settings.getInsertThreadPoolSize(), 
                        settings.getTimeoutSeconds());
            }
            
            // Use cursor iterator for memory-efficient streaming
            var cursor = source.find(null, new CollectionFindOptions());
            
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                // Apply document mapper transformation
                Document transformedDoc = settings.getDocumentMapper().map(doc);
                int currentCount = counter.incrementAndGet();
                batch.add(transformedDoc);
                
                // Log progress every 10,000 documents
                if (settings.isLogProgress() && currentCount % 10000 == 0) {
                    log.info("Processed {} documents so far...", currentCount);
                }
                
                // When batch is full, submit for async insertion
                if (batch.size() >= settings.getBatchSize()) {
                    final List<Document> batchToInsert = new ArrayList<>(batch);
                    final int currentBatch = batchCounter.incrementAndGet();
                    final int docsProcessed = currentCount;
                    if (settings.isLogProgress()) {
                        log.info("Enqueuing batch #{} ({} documents) - Total processed: {}", 
                                currentBatch, batchToInsert.size(), docsProcessed);
                    }
                    executor.submit(() -> {
                        target.insertMany(batchToInsert);
                        if (settings.isLogProgress()) {
                            log.info("Batch #{} inserted successfully", currentBatch);
                        }
                    });
                    batch.clear();
                }
            }

            // Process remaining batch
            if (!batch.isEmpty()) {
                final List<Document> finalBatch = new ArrayList<>(batch);
                final int finalBatchNum = batchCounter.incrementAndGet();
                if (settings.isLogProgress()) {
                    log.info("Enqueuing final batch #{} with {} documents for insertion...", 
                            finalBatchNum, finalBatch.size());
                }
                executor.submit(() -> {
                    target.insertMany(finalBatch);
                    if (settings.isLogProgress()) {
                        log.info("Final batch #{} inserted successfully", finalBatchNum);
                    }
                });
            }
            
        } finally {
            executor.shutdown();
            long enqueuedTime = System.currentTimeMillis() - startTime;
            if (settings.isLogProgress()) {
                log.info("{} documents enqueued for cloning in {} ms", 
                        counter.get(), enqueuedTime);
            }
            
            try {
                if (!executor.awaitTermination(settings.getTimeoutSeconds(), TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (settings.isLogProgress()) {
                        log.warn("Cloning timed out after {} seconds", settings.getTimeoutSeconds());
                    }
                } else {
                    long totalTime = System.currentTimeMillis() - startTime;
                    int totalDocs = counter.get();
                    long throughput = totalDocs * 1000L / totalTime;
                    if (settings.isLogProgress()) {
                        log.info("{} documents successfully cloned in {} ms (Throughput: {} docs/sec)", 
                                totalDocs, totalTime, throughput);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
                throw new RuntimeException("Collection cloning was interrupted", e);
            }
        }
    }
}
