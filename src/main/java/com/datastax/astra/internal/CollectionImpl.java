package com.datastax.astra.internal;

import io.stargate.sdk.core.domain.Page;
import io.stargate.sdk.data.client.Collection;
import io.stargate.sdk.data.client.DataAPIClientOptions;
import io.stargate.sdk.data.client.Database;
import io.stargate.sdk.data.client.exception.DataApiException;
import io.stargate.sdk.data.client.exception.DataApiFaultyResponseException;
import io.stargate.sdk.data.client.exception.TooManyDocumentsToCountException;
import io.stargate.sdk.data.client.model.ApiResponse;
import io.stargate.sdk.data.client.model.Command;
import io.stargate.sdk.data.client.model.Document;
import io.stargate.sdk.data.client.model.Filter;
import io.stargate.sdk.data.client.model.collections.CollectionDefinition;
import io.stargate.sdk.data.client.model.collections.CollectionOptions;
import io.stargate.sdk.data.client.model.delete.DeleteOneOptions;
import io.stargate.sdk.data.client.model.delete.DeleteResult;
import io.stargate.sdk.data.client.model.find.FindOneAndDeleteOptions;
import io.stargate.sdk.data.client.model.find.FindOneAndReplaceOptions;
import io.stargate.sdk.data.client.model.find.FindOneAndReplaceResult;
import io.stargate.sdk.data.client.model.find.FindOneAndUpdateOptions;
import io.stargate.sdk.data.client.model.find.FindOneOptions;
import io.stargate.sdk.data.client.model.find.FindOptions;
import io.stargate.sdk.data.client.model.insert.InsertManyOptions;
import io.stargate.sdk.data.client.model.insert.InsertManyResult;
import io.stargate.sdk.data.client.model.insert.InsertOneResult;
import io.stargate.sdk.data.client.model.iterable.DistinctIterable;
import io.stargate.sdk.data.client.model.iterable.FindIterable;
import io.stargate.sdk.data.client.model.misc.BulkWriteOptions;
import io.stargate.sdk.data.client.model.misc.BulkWriteResult;
import io.stargate.sdk.data.client.model.update.ReplaceOneOptions;
import io.stargate.sdk.data.client.model.update.Update;
import io.stargate.sdk.data.client.model.update.UpdateOneOptions;
import io.stargate.sdk.data.client.model.update.UpdateResult;
import io.stargate.sdk.http.LoadBalancedHttpClient;
import io.stargate.sdk.http.ServiceHttp;
import io.stargate.sdk.utils.Assert;
import io.stargate.sdk.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.stargate.sdk.data.client.DataAPIClientOptions.*;
import static io.stargate.sdk.utils.AnsiUtils.cyan;
import static io.stargate.sdk.utils.AnsiUtils.green;
import static io.stargate.sdk.utils.AnsiUtils.magenta;
import static io.stargate.sdk.utils.AnsiUtils.yellow;
import static io.stargate.sdk.utils.Assert.hasLength;
import static io.stargate.sdk.utils.Assert.notNull;

/**
 * Class representing a Data Api Collection.
 *
 * @param <DOC>
 *     working document
 */
@Slf4j
public class CollectionImpl<DOC> extends AbstractCommandRunner implements Collection<DOC> {

    /** Collection identifier. */
    @Getter
    private final String collectionName;

    /** Keep ref to the generic. */
    protected final Class<DOC> documentClass;

    /** keep reference to namespace client. */
    private final Database namespace;

    /** Resource collection. */
    public final Function<ServiceHttp, String> collectionResource;

    /**
     * Full constructor.
     *
     * @param databaseClient
     *      client namespace http
     * @param collectionName
     *      collection identifier
     */
    protected CollectionImpl(Database databaseClient, String collectionName, Class<DOC> clazz) {
        hasLength(collectionName, "collectionName");
        notNull(databaseClient, "namespace client");
        this.collectionName     = collectionName;
        this.namespace          = databaseClient;
        this.documentClass      = clazz;
        this.collectionResource = (node) -> databaseClient.lookup().apply(node) + "/" + collectionName;
    }

    // ----------------------------
    // --- Global Informations ----
    // ----------------------------

    /** {@inheritDoc} */
    @Override
    public Database getNamespace() {
        return namespace;
    }

    /** {@inheritDoc} */
    @Override
    public CollectionDefinition getDefinition() {
        return namespace
                .listCollections()
                .filter(col -> col.getName().equals(collectionName))
                .findFirst()
                .orElseThrow(() -> new DataApiException("[COLLECTION_NOT_EXIST] - Collection does not exist, " +
                        "collection name: '" + collectionName + "'", "COLLECTION_NOT_EXIST", null));
    }

    /** {@inheritDoc} */
    @Override
    public CollectionOptions getOptions() {
        return Optional
                .ofNullable(getDefinition()
                .getOptions())
                .orElse(new CollectionOptions());
    }

    /** {@inheritDoc} */
    @Override
    public Class<DOC> getDocumentClass() {
        return documentClass;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return collectionName;
    }

    // --------------------------
    // ---   Insert*         ----
    // --------------------------

    /** {@inheritDoc} */
    @Override
    public final InsertOneResult insertOne(DOC document, float[] embeddings) {
        Assert.notNull(document, "document");
        Assert.notNull(embeddings, "vectorize");
        return _insertOne(JsonUtils.convertValue(document, Document.class).vector(embeddings));
    }

    /** {@inheritDoc} */
    @Override
    public final InsertOneResult insertOne(DOC document, String vectorize) {
        Assert.notNull(document, "document");
        Assert.hasLength(vectorize, "vectorize");
        return _insertOne(JsonUtils.convertValue(document, Document.class).vectorize(vectorize));
    }

    /** {@inheritDoc} */
    @Override
    public final InsertOneResult insertOne(DOC document) {
        Assert.notNull(document, "document");
        return _insertOne(JsonUtils.convertValue(document, Document.class));
    }

    private final InsertOneResult _insertOne(Document document) {
        Assert.notNull(document, "document");
        Command insertOne = Command
                .create("insertOne")
                .withDocument(document);
        ApiResponse res = runCommand(insertOne);
        return new InsertOneResult(res
                .getStatusKeyAsList("insertedIds", Object.class)
                .get(0));
    }

    /** {@inheritDoc} */
    @Override
    public InsertManyResult insertMany(List<? extends DOC> documents) {
        return insertMany(documents, InsertManyOptions.builder().build());
    }

    /** {@inheritDoc} */
    @Override
    public InsertManyResult insertMany(List<? extends DOC> documents, InsertManyOptions options) {
        if (options.getConcurrency() > 1 && options.isOrdered()) {
            throw new IllegalArgumentException("Cannot run ordered insert_many concurrently.");
        }
        if (options.getChunkSize() > DataAPIClientOptions.getMaxDocumentsInInsert()) {
            throw new IllegalArgumentException("Cannot insert more than " + DataAPIClientOptions.getMaxDocumentsInInsert() + " at a time.");
        }
        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(options.getConcurrency());
        List<Future<InsertManyResult>> futures = new ArrayList<>();
        for (int i = 0; i < documents.size(); i += options.getChunkSize()) {
            futures.add(executor.submit(getInsertManyResultCallable(documents, options, i)));
        }
        executor.shutdown();

        // Grouping All Insert ids in the same list.
        InsertManyResult finalResult = new InsertManyResult();
        try {
            for (Future<InsertManyResult> future : futures) {
                finalResult.getInsertedIds().addAll(future.get().getInsertedIds());
            }

            if (executor.awaitTermination(options.getTimeout(), TimeUnit.MILLISECONDS)) {
                log.debug(magenta(".[total insertMany.responseTime]") + "=" + yellow("{}") + " millis.",
                        System.currentTimeMillis() - start);
            } else {
                throw new TimeoutException("Request did not complete withing ");
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot merge call results into a InsertManyResult", e);
        }
        return finalResult;
    }

    /**
     * Execute a 1 for 1 call to the Data API.
     *
     * @param documents
     *      list of documents to be inserted
     * @param options
     *      options for insert many (chunk size and insertion order).
     * @param start
     *      offset in global list
     * @return
     *      insert many result for a paged call
     */
    private Callable<InsertManyResult> getInsertManyResultCallable(List<? extends DOC> documents, InsertManyOptions options, int start) {
        int end = Math.min(start + options.getChunkSize(), documents.size());
        return () -> {
            log.debug("Insert block (" + cyan("size={}") + ") in collection {}", end - start, green(getCollectionName()));
            Command insertMany = new Command("insertMany")
                    .withDocuments(documents.subList(start, end))
                    .withOptions(new Document().append("ordered", options.isOrdered()));
            return new InsertManyResult(runCommand(insertMany).getStatusKeyAsList("insertedIds", Object.class));
        };
    }

    // --------------------------
    // ---   Find*           ----
    // --------------------------

    /** {@inheritDoc} */
    @Override
    public Optional<DOC> findOne(Filter filter, FindOneOptions options) {
        notNull(options, "options");
        Command findOne = Command
                .create("findOne")
                .withFilter(filter)
                .withSort(options.getSort())
                .withProjection(options.getProjection())
                .withOptions(new Document()
                        .appendIfNotNull("includeSimilarity", options.getIncludeSimilarity()));
        return Optional.ofNullable(
                runCommand(findOne)
                        .getData().getDocument()
                        .map(getDocumentClass()));
    }

    /** {@inheritDoc} */
    @Override
    public FindIterable<DOC> find(Filter filter, FindOptions options) {
        return new FindIterable<>(this, filter, options);
    }

    /** {@inheritDoc} */
    @Override
    public Page<DOC> findPage(Filter filter, FindOptions options) {
        Command findCommand = Command
                .create("find")
                .withFilter(filter)
                .withSort(options.getSort())
                .withProjection(options.getProjection())
                .withOptions(new Document()
                    .appendIfNotNull("skip", options.getSkip())
                    .appendIfNotNull("limit", options.getLimit())
                    .appendIfNotNull("pageState", options.getPageState())
                    .appendIfNotNull("includeSimilarity", options.getIncludeSimilarity()));

        ApiResponse apiResponse = runCommand(findCommand);

        return new Page<>(DataAPIClientOptions.getMaxPageSize(),
                apiResponse.getData().getNextPageState(),
                apiResponse.getData().getDocuments()
                        .stream()
                        .map(d -> d.map(getDocumentClass()))
                        .collect(Collectors.toList()));
    }

    /** {@inheritDoc} */
    @Override
    public <FIELD> DistinctIterable<DOC, FIELD> distinct(String fieldName, Filter filter, Class<FIELD> resultClass) {
        return new DistinctIterable<>(this, fieldName, filter, resultClass);
    }

    // ----------------------------
    // ---   Count Document    ----
    // ----------------------------

    /** {@inheritDoc} */
    @Override
    public int countDocuments(int upperBound) throws TooManyDocumentsToCountException {
        return countDocuments(null, upperBound);
    }

    /** {@inheritDoc} */
    @Override
    public int countDocuments(Filter filter, int upperBound) throws TooManyDocumentsToCountException {
        // Argument Validation
        if (upperBound<1 || upperBound> DataAPIClientOptions.getMaxDocumentCount()) {
            throw new IllegalArgumentException("UpperBound limit should be in between 1 and " + DataAPIClientOptions.getMaxDocumentCount());
        }
        // Build command
        Command command = new Command("countDocuments").withFilter(filter);
        // Run command
        ApiResponse response = runCommand(command);
        // Build Result
        Boolean moreData = response.getStatus().getBoolean("moreData");
        Integer count    = response.getStatus().getInteger("count");
        if (moreData != null && moreData) {
            throw new TooManyDocumentsToCountException();
        } else if (count > upperBound) {
            throw new TooManyDocumentsToCountException(upperBound);
        }
        return count;
    }

    // ----------------------------
    // ---   Delete            ----
    // ----------------------------

    public static final String DELETED_COUNT = "deletedCount";
    public static final String MATCHED_COUNT = "matchedCount";
    public static final String MODIFIED_COUNT = "modifiedCount";
    public static final String UPSERTED_ID = "upsertedId";
    public static final String MORE_DATA = "moreData";

    /** {@inheritDoc} */
    @Override
    public DeleteResult deleteOne(Filter filter, DeleteOneOptions deleteOneOptions) {
        Command deleteOne = Command
                .create("deleteOne")
                .withFilter(filter)
                .withSort(deleteOneOptions.getSort());

        ApiResponse apiResponse = runCommand(deleteOne);
        int deletedCount = apiResponse.getStatus().getInteger(DELETED_COUNT);
        return new DeleteResult(deletedCount);
    }

    /** {@inheritDoc} */
    @Override
    public DeleteResult deleteMany(Filter filter) {
        Assert.notNull(filter, "filter");
        AtomicInteger totalCount = new AtomicInteger(0);
        DeleteResult res;
        boolean moreData = false;
        do {
            Command deleteMany = Command
                    .create("deleteMany")
                    .withFilter(filter);

            ApiResponse apiResponse = runCommand(deleteMany);
            Document status = apiResponse.getStatus();
            if (status != null) {
                if (status.containsKey(DELETED_COUNT)) {
                    totalCount.addAndGet(status.getInteger(DELETED_COUNT));
                }
                if (status.containsKey(MORE_DATA)) {
                    moreData = status.getBoolean(MORE_DATA);
                }
            }
        } while(moreData);
        return new DeleteResult(totalCount.get());
    }

    /** {@inheritDoc} */
    @Override
    public DeleteResult deleteAll() {
        return deleteMany(new Filter());
    }

    /** {@inheritDoc} */
    @Override
    public void drop() {
        getNamespace().dropCollection(collectionName);
    }

    // ----------------------------
    // ---  Update             ----
    // ----------------------------

    /** {@inheritDoc} */
    @Override
    public Optional<DOC> findOneAndReplace(Filter filter, DOC replacement, FindOneAndReplaceOptions options) {

        Command findOneAndReplace = Command
                .create("findOneAndReplace")
                .withFilter(filter)
                .withReplacement(replacement)
                .withSort(options.getSort())
                .withProjection(options.getProjection())
                .withOptions(new Document()
                  .appendIfNotNull("upsert", options.getUpsert())
                  .appendIfNotNull("returnDocument", options.getReturnDocument().name())
                );

        ApiResponse res = runCommand(findOneAndReplace);
        if (res.getData()!= null && res.getData().getDocument() != null) {
            return Optional.ofNullable(res
                    .getData()
                    .getDocument()
                    .map(getDocumentClass()));
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */@Override
    public UpdateResult replaceOne(Filter filter, DOC replacement, ReplaceOneOptions replaceOneOptions) {

        Command findOneAndReplace = Command
                .create("findOneAndReplace")
                .withFilter(filter)
                .withReplacement(replacement)
                .withOptions(new Document()
                        .appendIfNotNull("upsert", replaceOneOptions.getUpsert())
                        .append("returnDocument", FindOneAndReplaceOptions.ReturnDocument.before.name())
                );

        // Execute the `findOneAndReplace`
        FindOneAndReplaceResult<DOC> res = executeFindOneAndReplace(findOneAndReplace);

        // Parse the result for a replace one
        UpdateResult result = new UpdateResult();
        result.setMatchedCount(res.getMatchedCount());
        result.setModifiedCount(res.getModifiedCount());
        if (res.getDocument() != null) {
            Document doc = JsonUtils.convertValueForDataApi(res.getDocument(), Document.class);
            if (doc.getId(Object.class) != null) {
                result.setUpsertedId(doc.getId(Object.class));
            }
        }
        return result;
    }

    /**
     * Mutualisation of the code for replaceOne() and findOneAndReplaceOne().
     *
     * @param cmd
     *      command
     * @return
     *      command result
     */
    private FindOneAndReplaceResult<DOC> executeFindOneAndReplace(Command cmd) {
        // Run Command
        ApiResponse apiResponse = runCommand(cmd);
        // Parse Command Result
        FindOneAndReplaceResult<DOC> result = new FindOneAndReplaceResult<>();
        if (apiResponse.getData() == null) {
            throw new DataApiFaultyResponseException(cmd, apiResponse,"Faulty response from find_one_and_replace API command.");
        }
        if (apiResponse.getData().getDocument() != null) {
            result.setDocument(apiResponse
                    .getData()
                    .getDocument()
                    .map(getDocumentClass()));
        }
        Document status = apiResponse.getStatus();
        if (status != null) {
            if (status.containsKey(MATCHED_COUNT)) {
                result.setMatchedCount(status.getInteger(MATCHED_COUNT));
            }
            if (status.containsKey(MODIFIED_COUNT)) {
                result.setModifiedCount(status.getInteger(MODIFIED_COUNT));
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<DOC> findOneAndUpdate(Filter filter, Update update, FindOneAndUpdateOptions options) {
        notNull(update, "update");
        notNull(options, "options");
        Command cmd = Command
                .create("findOneAndUpdate")
                .withFilter(filter)
                .withUpdate(update)
                .withSort(options.getSort())
                .withProjection(options.getProjection())
                .withOptions(new Document()
                        .appendIfNotNull("upsert", options.getUpsert())
                        .append("returnDocument", options.getReturnDocument().name())
                );

        ApiResponse res = runCommand(cmd);
        if (res.getData()!= null && res.getData().getDocument() != null) {
            return Optional.ofNullable(res
                    .getData()
                    .getDocument()
                    .map(getDocumentClass()));
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public UpdateResult updateOne(Filter filter, Update update, UpdateOneOptions options) {
        notNull(update, "update");
        notNull(options, "options");
        Command cmd = Command
                .create("updateOne")
                .withFilter(filter)
                .withUpdate(update)
                .withSort(options.getSort())
                .withOptions(new Document()
                    .appendIfNotNull("upsert", options.getUpsert())
                );
        return getUpdateResult(runCommand(cmd));
    }

    private static UpdateResult getUpdateResult(ApiResponse apiResponse) {
        UpdateResult result = new UpdateResult();
        Document status = apiResponse.getStatus();
        if (status != null) {
            if (status.containsKey(MATCHED_COUNT)) {
                result.setMatchedCount(status.getInteger(MATCHED_COUNT));
            }
            if (status.containsKey(MODIFIED_COUNT)) {
                result.setModifiedCount(status.getInteger(MODIFIED_COUNT));
            }
            if (status.containsKey(UPSERTED_ID)) {
                result.setMatchedCount(status.getInteger(UPSERTED_ID));
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public UpdateResult updateMany(Filter filter, Update update, UpdateOneOptions options) {
        notNull(update, "update");
        notNull(options, "options");
        boolean moreData = true;
        String nextPageState = null;
        UpdateResult result = new UpdateResult();
        result.setMatchedCount(0);
        result.setModifiedCount(0);
        do {
            Command cmd = Command
                    .create("updateMany")
                    .withFilter(filter)
                    .withUpdate(update)
                    .withOptions(new Document()
                      .appendIfNotNull("upsert", options.getUpsert())
                      .appendIfNotNull("pageState", nextPageState));
            ApiResponse res = runCommand(cmd);
            // Data
            if (res.getData() != null) {
                nextPageState = res.getData().getNextPageState();
            }
            // Status
            Document status = res.getStatus();
            if (status.containsKey(MATCHED_COUNT)) {
                result.setMatchedCount(result.getMatchedCount() + status.getInteger(MATCHED_COUNT));
            }
            if (status.containsKey(MODIFIED_COUNT)) {
                result.setModifiedCount(result.getModifiedCount() + status.getInteger(MODIFIED_COUNT));
            }
            if (status.containsKey(UPSERTED_ID)) {
                result.setUpsertedId(status.getInteger(UPSERTED_ID));
            }
        } while(nextPageState != null);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<DOC> findOneAndDelete(Filter filter, FindOneAndDeleteOptions options) {
        Command findOneAndReplace = Command
                .create("findOneAndDelete")
                .withFilter(filter)
                .withSort(options.getSort())
                .withProjection(options.getProjection());

        ApiResponse res = runCommand(findOneAndReplace);
        if (res.getData()!= null && res.getData().getDocument() != null) {
            return Optional.ofNullable(res
                    .getData()
                    .getDocument()
                    .map(getDocumentClass()));
        }
        return Optional.empty();
    }

    // ----------------------------
    // ---   Bulk Write        ----
    // ----------------------------

    /** {@inheritDoc} */
    @Override
    public BulkWriteResult bulkWrite(List<Command> commands, BulkWriteOptions options) {
        notNull(commands, "commands");
        notNull(options, "options");
        if (options.getConcurrency() > 1 && options.isOrdered()) {
            throw new IllegalArgumentException("Cannot run ordered bulk_write concurrently.");
        }
        BulkWriteResult result = new BulkWriteResult(0);
        result = new BulkWriteResult(commands.size());
        if (options.isOrdered()) {
            result.setResponses(commands.stream().map(this::runCommand).collect(Collectors.toList()));
        } else {
            ExecutorService executor = Executors.newFixedThreadPool(options.getConcurrency());
            List<Future<ApiResponse>> futures = new ArrayList<>();
            commands.forEach(req -> futures.add(executor.submit(() -> runCommand(req))));
            executor.shutdown();
            try {
                for (Future<ApiResponse> future : futures) {
                    result.getResponses().add(future.get());
                }
            } catch(Exception ex) {
                throw new IllegalStateException("Cannot access command results", ex);
            }
        }
        return result;
    }

    // ------------------------------------------
    // ----           Lookup                 ----
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public Function<ServiceHttp, String> lookup() {
        return collectionResource;
    }

    /** {@inheritDoc} */
    @Override
    public LoadBalancedHttpClient getHttpClient() {
        return getNamespace().getHttpClient();
    }

}
