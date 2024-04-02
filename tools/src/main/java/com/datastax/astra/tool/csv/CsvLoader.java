package com.datastax.astra.tool.csv;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.model.Document;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CSV Loader
 */
@Slf4j
public class CsvLoader {

    /**
     * Distributed import of CSV file into Astra.
     *
     * @throws Exception
     *      exception in processing CSV
     */
    public static void load(String fileName, Collection<Document> collection, CsvRowMapper processor) throws Exception {
        load(fileName, CsvLoaderSettings.builder().build(), collection, processor);
    }

    /**
     * Distributed import of CSV file into Astra.
     *
     * @throws Exception
     *      exception in processing CSV
     */
    public static void load(String fileName, CsvLoaderSettings settings, Collection<Document> collection) throws Exception {
        load(fileName, settings, collection, doc -> doc);
    }

    /**
     * Distributed import of CSV file into Astra.
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
    public static void load(String fileName, CsvLoaderSettings settings, Collection<Document> collection, CsvRowMapper processor) throws Exception {
        AtomicInteger counter = new AtomicInteger();
        long top = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(settings.threadPoolSize);
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            String[] headers = reader.readNext(); // Read the header row
            if (headers == null) {
                throw new RuntimeException("CSV file is empty or does not contain a header row.");
            }
            List<Document> batch = new ArrayList<>(settings.batchSize);
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                Document rowMap = new Document();
                for (int i = 0; i < headers.length; i++) {
                    rowMap.put(headers[i], nextLine.length > i ? nextLine[i] : null);
                }
                counter.incrementAndGet();
                batch.add(processor.map(rowMap));
                if (batch.size() == settings.batchSize) {
                    final List<Document> batchToInsert = new ArrayList<>(batch);
                    executor.submit(() -> collection.insertMany(batchToInsert));
                    batch.clear(); // Clear the batch for the next set of rows
                }
            }
            // Process any remaining rows which didn't fill the last batch
            if (!batch.isEmpty()) {
                executor.submit(() -> collection.insertMany(batch));
            }
        } finally {
            executor.shutdown();
            log.info("{} rows enqueues from " + collection + " in {} ms", counter.get(), System.currentTimeMillis()-top);
            try {
                if (!executor.awaitTermination(settings.timeoutSeconds, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    log.info("{} rows process in {} ms", counter.get(), System.currentTimeMillis()-top);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
        }
    }
}
