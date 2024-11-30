package com.datastax.astra.test.unit;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.collections.documents.ReturnDocument;
import com.datastax.astra.client.core.http.HttpClientOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.collections.options.CollectionDeleteOneOptions;
import com.datastax.astra.client.collections.results.CollectionDeleteResult;
import com.datastax.astra.client.collections.options.CollectionFindOneAndDeleteOptions;
import com.datastax.astra.client.collections.options.CollectionFindOneAndReplaceOptions;
import com.datastax.astra.client.collections.options.CollectionFindOneAndUpdateOptions;
import com.datastax.astra.client.collections.options.CollectionFindOneOptions;
import com.datastax.astra.client.collections.options.CollectionFindOptions;
import com.datastax.astra.client.collections.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.results.CollectionInsertOneResult;
import com.datastax.astra.client.collections.options.CollectionReplaceOneOptions;
import com.datastax.astra.client.core.options.TimeoutOptions;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.collections.documents.Update;
import com.datastax.astra.client.collections.options.UpdateOneOptions;
import com.datastax.astra.client.collections.documents.Updates;
import com.datastax.astra.client.core.vector.VectorOptions;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.collections.CollectionDefaultIdTypes;
import com.datastax.astra.client.collections.CollectionDefinition;
import com.datastax.astra.client.core.http.HttpProxy;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.FilterOperator;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.internal.serdes.collections.DocumentSerializer;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.datastax.astra.client.core.options.DataAPIClientOptions.HEADER_FEATURE_FLAG_TABLES;
import static com.datastax.astra.client.core.options.TimeoutOptions.DEFAULT_GENERAL_METHOD_TIMEOUT_MILLIS;
import static com.datastax.astra.client.core.options.TimeoutOptions.DEFAULT_REQUEST_TIMEOUT_MILLIS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataApiOptionsTest {

    @Test
    void shouldPopulateOptions() {
        DataAPIClientOptions options = new DataAPIClientOptions()
                // Setup defaults based on destinations
                .destination(DataAPIDestination.DSE)
                // Overriding HTTP
                .httpClientOptions(new HttpClientOptions()
                        .httpRedirect(HttpClient.Redirect.NORMAL)
                        .httpProxy(new HttpProxy("localhost", 8080))
                        .httpRetries(1, Duration.ofSeconds(10)))
                // Overriding Timeouts
                .timeoutOptions(new TimeoutOptions()
                        .requestTimeoutMillis(1000))
                // Headers
                .enableFeatureFlagTables()
                .addDatabaseAdditionalHeader(HEADER_FEATURE_FLAG_TABLES, "true");
        DataAPIClient client = new DataAPIClient("token", options);
        client.getDatabase("https://<id>-<region>.apps.astra.datastax.com");
        assertThat(options.getHttpClientOptions().getHttpProxy().getHostname()).isEqualTo("localhost");

        DatabaseOptions optionss = new DatabaseOptions()
                .keyspace("sample")
                .token("another")
                .timeout(Duration.ofSeconds(10));



    }

    @Test
    void shouldInitializeInsertManyOptions() {
        assertThat(new CollectionInsertManyOptions().chunkSize(10)).isNotNull();
        assertThat(new CollectionInsertManyOptions().
                timeoutOptions(new TimeoutOptions()
                        .requestTimeoutMillis(DEFAULT_REQUEST_TIMEOUT_MILLIS)
                        .generalMethodTimeoutMillis(DEFAULT_GENERAL_METHOD_TIMEOUT_MILLIS)))
                .isNotNull();
        assertThat(new CollectionInsertManyOptions().ordered(true)).isNotNull();
        assertThat(new CollectionInsertManyOptions().concurrency(2)).isNotNull();
    }

    @Test
    void shouldFailParsingCollectionIdTypes() {
        assertThatThrownBy(() -> CollectionDefaultIdTypes.fromValue("invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldInitializeFindOneAndDeleteOptions() {
        assertThat(new CollectionFindOneAndDeleteOptions().sort(Sort.ascending("test"))).isNotNull();
        assertThat(new CollectionFindOneAndDeleteOptions().projection(Projection.include("test"))).isNotNull();
        assertThat(new CollectionFindOneAndDeleteOptions().sort(Sort.vector(new float[]{}))).isNotNull();
        assertThat(new CollectionFindOneAndDeleteOptions().sort(Sort.vectorize("OK"))
                .sort(Sort.ascending("test"))
                .projection(Projection.include("test"))
                .sort(Sort.vectorize("OK"))
                .sort(Sort.vector(new float[]{})))
                .isNotNull();
    }

    @Test
    void shouldInitializeUpdateOne() {
        assertThat(new UpdateOneOptions().sort(Sort.ascending("test"))).isNotNull();
        assertThat(new UpdateOneOptions().upsert(true)).isNotNull();
        assertThat(new UpdateOneOptions().sort(Sort.vector(new float[]{}))).isNotNull();
        assertThat(new UpdateOneOptions().sort(Sort.vectorize("OK"))
                .sort(Sort.ascending("test"))
                .upsert(true)
                .sort(Sort.vectorize("OK"))
                .sort(Sort.vector(new float[]{})))
                .isNotNull();
    }

    @Test
    void shouldFindOneAndReplaceOptions() {
        assertThat(new CollectionFindOneAndReplaceOptions().sort(Sort.ascending("test"))).isNotNull();
        assertThat(new CollectionFindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER)).isNotNull();
        assertThat(new CollectionFindOneAndReplaceOptions().returnDocument(ReturnDocument.BEFORE)).isNotNull();
        assertThat(new CollectionFindOneAndReplaceOptions().projection(Projection.include("ok"))).isNotNull();
        assertThat(new CollectionFindOneAndReplaceOptions().upsert(true)).isNotNull();
        assertThat(new CollectionFindOneAndReplaceOptions().sort(Sort.vector(new float[]{}))).isNotNull();
        assertThat(new CollectionFindOneAndReplaceOptions().sort(Sort.vectorize("OK"))
                .sort(Sort.ascending("test"))
                .upsert(true)
                .projection(Projection.include("ok"))
                .returnDocument(ReturnDocument.AFTER)
                .returnDocument(ReturnDocument.BEFORE)
                .sort(Sort.vectorize("OK"))
                .sort(Sort.vector(new float[]{})))
                .isNotNull();
    }

    @Test
    void shouldFindOneAndUpdateOptions() {
        assertThat(new CollectionFindOneAndUpdateOptions().sort(Sort.ascending("test"))).isNotNull();
        assertThat(new CollectionFindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)).isNotNull();
        assertThat(new CollectionFindOneAndUpdateOptions().returnDocument(ReturnDocument.BEFORE)).isNotNull();
        assertThat(new CollectionFindOneAndUpdateOptions().projection(Projection.include("ok"))).isNotNull();
        assertThat(new CollectionFindOneAndUpdateOptions().upsert(true)).isNotNull();
        assertThat(new CollectionFindOneAndUpdateOptions().sort(Sort.vector(new float[]{}))).isNotNull();
        assertThat(new CollectionFindOneAndUpdateOptions().sort(Sort.vectorize("OK"))
                .sort(Sort.ascending("test"))
                .upsert(true)
                .projection(Projection.include("ok"))
                .returnDocument(ReturnDocument.AFTER)
                .returnDocument(ReturnDocument.BEFORE)
                .sort(Sort.vectorize("OK"))
                .sort(Sort.vector(new float[]{})))
                .isNotNull();
    }

    @Test
    void shouldTestFindOneOptions() {
        assertThat(new CollectionFindOneOptions().sort(Sort.ascending("test"))).isNotNull();
        assertThat(new CollectionFindOneOptions().projection(Projection.include("ok"))).isNotNull();
        assertThat(new CollectionFindOneOptions().includeSimilarity(true)).isNotNull();
        assertThat(new CollectionFindOneOptions().sort(Sort.vector(new float[]{}))).isNotNull();
        assertThat(new CollectionFindOneOptions().sort(Sort.vectorize("OK"))
                .sort(Sort.ascending("test"))
                .includeSimilarity(true)
                .projection(Projection.include("ok"))
                .sort(Sort.vectorize("OK"))
                .sort(Sort.vector(new float[]{})))
                .isNotNull();
    }

    @Test
    void shouldTestDeleteOneOptions() {
        assertThat(new CollectionDeleteOneOptions().sort(Sort.ascending("test"))).isNotNull();
        assertThat(new CollectionDeleteOneOptions().sort(Sort.vector(new float[]{}))).isNotNull();
        assertThat(new CollectionDeleteOneOptions().sort(Sort.vectorize("OK"))
                .sort(Sort.ascending("test"))
                .sort(Sort.vectorize("OK"))
                .sort(Sort.vector(new float[]{})))
                .isNotNull();
    }

    @Test
    void shouldTestReplaceOneOptions() {
        assertThat(new CollectionReplaceOneOptions().upsert(true)).isNotNull();
    }

    @Test
    void shouldTestInsertOneResult() {
        CollectionInsertOneResult ior = new CollectionInsertOneResult();
        ior.setInsertedId("OK");
        assertThat(ior.getInsertedId()).isEqualTo("OK");
    }

    @Test
    void shouldTestCollectionOptions() {
        CollectionDefinition c = new CollectionDefinition();

        VectorOptions v = new VectorOptions();

        VectorServiceOptions s = new VectorServiceOptions();
        s.provider("OK");
        s.modelName("OK");

        VectorServiceOptions.Parameters p1 = new VectorServiceOptions.Parameters();
        p1.help("sample parama");
        p1.type("String");
        p1.required(true);
        p1.defaultValue("OK");
        s.parameters(Map.of("ok", p1));

        v.service(s);
        c.vector(v);
        System.out.println(new DocumentSerializer().marshall(c));
        assertThat(new DocumentSerializer().marshall(c)).isNotNull();
    }

    @Test
    void shouldBuildUpdate() {
        Update u1 = Update.create()
                .inc("test", 1d)
                .set("test", "OK")
                .unset("test")
                .min("test", 1);
        Update u2 = new Update("{\"$inc\":{\"test\":1.0},\"$set\":{\"test\":\"OK\"},\"$unset\":{\"test\":\"\"},\"$min\":{\"test\":1}}");
        assertThat(u1.toJson()).isEqualTo(u2.toJson());
        Update u3 =  new Update()
                .rename("test", "test2")
                .updateSetOnInsert(Map.of("test", 1d))
                .updateCurrentDate("test")
                .addToSet("test", "OK")
                .push("test", "OK")
                .pushEach("list", List.of("OK"), 1)
                .pop("test", 1)
                .min("test", 1);
        assertThat(u3.toJson()).isNotNull();

        assertThat(Updates.inc("test", 1d)).isNotNull();
        assertThat(Updates.set("test", "OK")).isNotNull();
        assertThat(Updates.min("test", 1d)).isNotNull();
        assertThat(Updates.unset("field")).isNotNull();
        assertThat(Updates.rename("old", "new")).isNotNull();
        assertThat(Updates.updateSetOnInsert(Map.of("test", 1d))).isNotNull();
        assertThat(Updates.updateCurrentDate("test")).isNotNull();
        assertThat(Updates.addToSet("test", "OK")).isNotNull();
        assertThat(Updates.push("test", "OK")).isNotNull();
        assertThat(Updates.pushEach("list", List.of("OK"), 1)).isNotNull();
        assertThat(Updates.pop("test", 1)).isNotNull();
    }

    @Test
    void shouldBuildFilter() {
        assertThat(new Filter("_id", FilterOperator.EQUALS_TO, "OK").get("_id")).isNotNull();
        assertThat(Filter.findById("OK").get("_id")).isNotNull();
    }

    @Test
    void shouldBuildSimilarity() {
        assertThat(SimilarityMetric.fromValue("cosine")).isEqualTo(SimilarityMetric.COSINE);
        assertThatThrownBy(() -> SimilarityMetric.fromValue("invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldTestDeleteResult() {
        assertThat(new CollectionDeleteResult(20).getDeletedCount()).isEqualTo(20);
    }

    @Test
    void shouldTestFindOptions() {
        CollectionFindOptions fo = new CollectionFindOptions();
        assertThatThrownBy(() -> fo.limit(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> fo.skip(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThat(new CollectionFindOptions().sort(Sort.vectorize("ok")).includeSimilarity(true).pageState("ok")).isNotNull();
        assertThat(new CollectionFindOptions().includeSimilarity(true)).isNotNull();
        assertThat(new CollectionFindOptions().limit(10)).isNotNull();
        assertThat(new CollectionFindOptions().skip(10)).isNotNull();
    }
}
