package com.datastax.astra.tool.loader.json;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class JsonDocumentLoader {

    /**
     * Distributed import of CSV file into Astra.
     *
     * @throws Exception
     *      exception in processing CSV
     */
    public static void load(String fileName, Collection<Document> collection, JsonRecordMapper processor) throws Exception {
        load(fileName, JsonLoaderSettings.builder().build(), collection, processor);
    }

    /**
     * Distributed import of CSV file into Astra.
     *
     * @throws Exception
     *      exception in processing CSV
     */
    public static void load(String fileName, JsonLoaderSettings settings, Collection<Document> collection) throws Exception {
        load(fileName, settings, collection, doc -> doc);
    }

    /**
     * Distributed import of a JSON file into Astra.
     *
     * @param settings
     *     settings
     * @param collection
     *     collection
     * @param processor
     *     processor
     * @throws Exception
     *      exception in processing CSV
     */
    public static void load(String fileName,
                            JsonLoaderSettings settings,
                            Collection<Document> collection,
                            JsonRecordMapper processor)
    throws Exception {
        AtomicInteger counter = new AtomicInteger();
        long top = System.currentTimeMillis();
        // Multithreaded executor to process the CSV file
        long startTime = System.currentTimeMillis();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Document> batch = new ArrayList<>(settings.batchSize);
        ExecutorService executor = Executors.newFixedThreadPool(settings.threadPoolSize);

        try (JsonParser parser = new JsonFactory().createParser(new File(fileName))) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new RuntimeException("JSON file must contain an array at the root");
            }
            while (parser.nextToken() == JsonToken.START_OBJECT) {
                Document doc = objectMapper.readValue(parser, Document.class);
                //Document doc = new Document();
                //doc.putAll();
                counter.incrementAndGet();
                batch.add(processor.map(doc));
                if (batch.size() == settings.batchSize) {
                    final List<Document> batchToInsert = new ArrayList<>(batch);
                    log.info("Enqueuing " + batch.size() + " rows into collection...");
                    executor.submit(() -> collection.insertMany(batchToInsert));
                    batch.clear();
                }
            }

            // Process remaining batch
            if (!batch.isEmpty()) {
                executor.submit(() -> collection.insertMany(batch));
            }
        } finally {
            executor.shutdown();
            log.info(counter.get() + " rows enqueued from " + collection + " in " + (System.currentTimeMillis() - startTime) + " ms");
            try {
                if (!executor.awaitTermination(settings.timeoutSeconds, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    log.info(counter.get() + " rows processed in " + (System.currentTimeMillis() - startTime) + " ms");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
        }
    }
}
