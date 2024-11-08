package com.datastax.astra.test.unit;

import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.options.DataAPIOptions;
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
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.collections.documents.Update;
import com.datastax.astra.client.collections.options.UpdateOneOptions;
import com.datastax.astra.client.collections.documents.Updates;
import com.datastax.astra.client.core.vector.VectorOptions;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.collections.CollectionIdTypes;
import com.datastax.astra.client.collections.CollectionOptions;
import com.datastax.astra.client.core.http.HttpProxy;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.FilterOperator;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Projections;
import com.datastax.astra.client.core.query.Sorts;
import com.datastax.astra.internal.serdes.collections.DocumentSerializer;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.datastax.astra.client.core.options.DataAPIOptions.HEADER_FEATURE_FLAG_TABLES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataApiOptionsTest {

    @Test
    void shouldPopulateOptions() {
        DataAPIOptions options = DataAPIOptions.builder()
                .withHttpProxy(new HttpProxy("localhost", 8080))
                .withApiVersion("v1")
                .withHttpRedirect(HttpClient.Redirect.NORMAL)
                .withHttpRetries(5, Duration.ofMillis(1000))
                .withDestination(DataAPIDestination.DSE)
                .enableFeatureFlagTables()
                // equivalent to:
                .addDatabaseAdditionalHeader(HEADER_FEATURE_FLAG_TABLES, "true")
                .build();
        assertThat(options.getHttpClientOptions().getProxy().getHostname()).isEqualTo("localhost");
    }

    @Test
    void shouldInitializeInsertManyOptions() {
        assertThat(new CollectionInsertManyOptions().chunkSize(10)).isNotNull();
        assertThat(new CollectionInsertManyOptions().timeout(10)).isNotNull();
        assertThat(new CollectionInsertManyOptions().ordered(true).timeout(10)).isNotNull();
        assertThat(new CollectionInsertManyOptions().concurrency(2).timeout(10)).isNotNull();
    }

    @Test
    void shouldFailParsingCollectionIdTypes() {
        assertThatThrownBy(() -> CollectionIdTypes.fromValue("invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldInitializeFindOneAndDeleteOptions() {
        assertThat(new CollectionFindOneAndDeleteOptions().sort(Sorts.ascending("test"))).isNotNull();
        assertThat(new CollectionFindOneAndDeleteOptions().projection(Projections.include("test"))).isNotNull();
        assertThat(new CollectionFindOneAndDeleteOptions().sort(new float[]{})).isNotNull();
        assertThat(new CollectionFindOneAndDeleteOptions().sort("OK")
                .sort(Sorts.ascending("test"))
                .projection(Projections.include("test"))
                .sort("ok")
                .sort(new float[]{}))
                .isNotNull();
    }

    @Test
    void shouldInitializeUpdateOne() {
        assertThat(new UpdateOneOptions().sort(Sorts.ascending("test"))).isNotNull();
        assertThat(new UpdateOneOptions().upsert(true)).isNotNull();
        assertThat(new UpdateOneOptions().vector(new float[]{})).isNotNull();
        assertThat(new UpdateOneOptions().vectorize("OK")
                .sort(Sorts.ascending("test"))
                .upsert(true)
                .vectorize("ok")
                .vector(new float[]{}))
                .isNotNull();
    }

    @Test
    void shouldFindOneAndReplaceOptions() {
        assertThat(new CollectionFindOneAndReplaceOptions().sort(Sorts.ascending("test"))).isNotNull();
        assertThat(new CollectionFindOneAndReplaceOptions().returnDocumentAfter()).isNotNull();
        assertThat(new CollectionFindOneAndReplaceOptions().returnDocumentBefore()).isNotNull();
        assertThat(new CollectionFindOneAndReplaceOptions().projection(Projections.include("ok"))).isNotNull();
        assertThat(new CollectionFindOneAndReplaceOptions().upsert(true)).isNotNull();
        assertThat(new CollectionFindOneAndReplaceOptions().sort(new float[]{})).isNotNull();
        assertThat(new CollectionFindOneAndReplaceOptions().sort("OK")
                .sort(Sorts.ascending("test"))
                .upsert(true)
                .projection(Projections.include("ok"))
                .returnDocumentAfter()
                .returnDocumentBefore()
                .sort("ok")
                .sort(new float[]{}))
                .isNotNull();
    }

    @Test
    void shouldFindOneAndUpdateOptions() {
        assertThat(new CollectionFindOneAndUpdateOptions().sort(Sorts.ascending("test"))).isNotNull();
        assertThat(new CollectionFindOneAndUpdateOptions().returnDocumentAfter()).isNotNull();
        assertThat(new CollectionFindOneAndUpdateOptions().returnDocumentBefore()).isNotNull();
        assertThat(new CollectionFindOneAndUpdateOptions().projection(Projections.include("ok"))).isNotNull();
        assertThat(new CollectionFindOneAndUpdateOptions().upsert(true)).isNotNull();
        assertThat(new CollectionFindOneAndUpdateOptions().sort(new float[]{})).isNotNull();
        assertThat(new CollectionFindOneAndUpdateOptions().sort("OK")
                .sort(Sorts.ascending("test"))
                .upsert(true)
                .projection(Projections.include("ok"))
                .returnDocumentAfter()
                .returnDocumentBefore()
                .sort("ok")
                .sort(new float[]{}))
                .isNotNull();
    }

    @Test
    void shouldTestFindOneOptions() {
        assertThat(new CollectionFindOneOptions().sort(Sorts.ascending("test"))).isNotNull();
        assertThat(new CollectionFindOneOptions().projection(Projections.include("ok"))).isNotNull();
        assertThat(new CollectionFindOneOptions().includeSimilarity()).isNotNull();
        assertThat(new CollectionFindOneOptions().sort(new float[]{})).isNotNull();
        assertThat(new CollectionFindOneOptions().sort("OK")
                .sort(Sorts.ascending("test"))
                .includeSimilarity()
                .projection(Projections.include("ok"))
                .sort("ok")
                .sort(new float[]{}))
                .isNotNull();
    }

    @Test
    void shouldTestDeleteOneOptions() {
        assertThat(new CollectionDeleteOneOptions().sort(Sorts.ascending("test"))).isNotNull();
        assertThat(new CollectionDeleteOneOptions().sort(new float[]{})).isNotNull();
        assertThat(new CollectionDeleteOneOptions().sort("OK")
                .sort(Sorts.ascending("test"))
                .sort("ok")
                .sort(new float[]{}))
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
        CollectionOptions c = new CollectionOptions();

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

        v.setService(s);
        c.setVector(v);
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
        assertThat(new CollectionFindOptions().sort("ok").includeSimilarity().pageState("ok")).isNotNull();
        assertThat(new CollectionFindOptions().includeSimilarity()).isNotNull();
        assertThat(new CollectionFindOptions().limit(10)).isNotNull();
        assertThat(new CollectionFindOptions().skip(10)).isNotNull();
    }

    @Test
    void shouldTOverrideMaximumLimits() {
        DataAPIOptions options = DataAPIOptions.builder()
                .withMaxDocumentsInInsert(100)
                .build();

        Projection p1 = new Projection("field1", true);
        Projection p2 = new Projection("field2", true);
        CollectionFindOptions options1 = new CollectionFindOptions().projection(p1,p2);
        CollectionFindOptions options2 = new CollectionFindOptions().projection(Projections.include("field1", "field2"));

        CollectionInsertManyOptions collectionInsertManyOptions = new CollectionInsertManyOptions().chunkSize(100);
        //DataAPIClient client = new DataAPIClient("token", options);

    }


}
