package com.datastax.astra.test.unit;

import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.collections.commands.DeleteOneOptions;
import com.datastax.astra.client.collections.commands.DeleteResult;
import com.datastax.astra.client.collections.commands.FindOneAndDeleteOptions;
import com.datastax.astra.client.collections.commands.FindOneAndReplaceOptions;
import com.datastax.astra.client.collections.commands.FindOneAndUpdateOptions;
import com.datastax.astra.client.collections.commands.FindOneOptions;
import com.datastax.astra.client.collections.commands.FindOptions;
import com.datastax.astra.client.collections.commands.InsertManyOptions;
import com.datastax.astra.client.collections.commands.InsertOneResult;
import com.datastax.astra.client.collections.commands.ReplaceOneOptions;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.collections.documents.Update;
import com.datastax.astra.client.collections.commands.UpdateOneOptions;
import com.datastax.astra.client.collections.documents.Updates;
import com.datastax.astra.client.core.vector.VectorOptions;
import com.datastax.astra.client.core.vector.VectorServiceOptions;
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

import static com.datastax.astra.client.DataAPIOptions.HEADER_FEATURE_FLAG_TABLES;
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
        assertThat(new InsertManyOptions().chunkSize(10)).isNotNull();
        assertThat(new InsertManyOptions().timeout(10)).isNotNull();
        assertThat(new InsertManyOptions().ordered(true).timeout(10)).isNotNull();
        assertThat(new InsertManyOptions().concurrency(2).timeout(10)).isNotNull();
    }

    @Test
    void shouldFailParsingCollectionIdTypes() {
        assertThatThrownBy(() -> CollectionIdTypes.fromValue("invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldInitializeFindOneAndDeleteOptions() {
        assertThat(new FindOneAndDeleteOptions().sort(Sorts.ascending("test"))).isNotNull();
        assertThat(new FindOneAndDeleteOptions().projection(Projections.include("test"))).isNotNull();
        assertThat(new FindOneAndDeleteOptions().sort(new float[]{})).isNotNull();
        assertThat(new FindOneAndDeleteOptions().sort("OK")
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
        assertThat(new FindOneAndReplaceOptions().sort(Sorts.ascending("test"))).isNotNull();
        assertThat(new FindOneAndReplaceOptions().returnDocumentAfter()).isNotNull();
        assertThat(new FindOneAndReplaceOptions().returnDocumentBefore()).isNotNull();
        assertThat(new FindOneAndReplaceOptions().projection(Projections.include("ok"))).isNotNull();
        assertThat(new FindOneAndReplaceOptions().upsert(true)).isNotNull();
        assertThat(new FindOneAndReplaceOptions().sort(new float[]{})).isNotNull();
        assertThat(new FindOneAndReplaceOptions().sort("OK")
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
        assertThat(new FindOneAndUpdateOptions().sort(Sorts.ascending("test"))).isNotNull();
        assertThat(new FindOneAndUpdateOptions().returnDocumentAfter()).isNotNull();
        assertThat(new FindOneAndUpdateOptions().returnDocumentBefore()).isNotNull();
        assertThat(new FindOneAndUpdateOptions().projection(Projections.include("ok"))).isNotNull();
        assertThat(new FindOneAndUpdateOptions().upsert(true)).isNotNull();
        assertThat(new FindOneAndUpdateOptions().sort(new float[]{})).isNotNull();
        assertThat(new FindOneAndUpdateOptions().sort("OK")
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
        assertThat(new FindOneOptions().sort(Sorts.ascending("test"))).isNotNull();
        assertThat(new FindOneOptions().projection(Projections.include("ok"))).isNotNull();
        assertThat(new FindOneOptions().includeSimilarity()).isNotNull();
        assertThat(new FindOneOptions().sort(new float[]{})).isNotNull();
        assertThat(new FindOneOptions().sort("OK")
                .sort(Sorts.ascending("test"))
                .includeSimilarity()
                .projection(Projections.include("ok"))
                .sort("ok")
                .sort(new float[]{}))
                .isNotNull();
    }

    @Test
    void shouldTestDeleteOneOptions() {
        assertThat(new DeleteOneOptions().sort(Sorts.ascending("test"))).isNotNull();
        assertThat(new DeleteOneOptions().sort(new float[]{})).isNotNull();
        assertThat(new DeleteOneOptions().sort("OK")
                .sort(Sorts.ascending("test"))
                .sort("ok")
                .sort(new float[]{}))
                .isNotNull();
    }

    @Test
    void shouldTestReplaceOneOptions() {
        assertThat(new ReplaceOneOptions().upsert(true)).isNotNull();
    }

    @Test
    void shouldTestInsertOneResult() {
        InsertOneResult ior = new InsertOneResult();
        ior.setInsertedId("OK");
        assertThat(ior.getInsertedId()).isEqualTo("OK");
    }

    @Test
    void shouldTestCollectionOptions() {
        CollectionOptions c = new CollectionOptions();

        VectorOptions v = new VectorOptions();

        VectorServiceOptions s = new VectorServiceOptions();
        s.setProvider("OK");
        s.setModelName("OK");

        VectorServiceOptions.Parameters p1 = new VectorServiceOptions.Parameters();
        p1.setHelp("sample parama");
        p1.setType("String");
        p1.setRequired(true);
        p1.setDefaultValue("OK");
        s.setParameters(Map.of("ok", p1));

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
        assertThat(new DeleteResult(20).getDeletedCount()).isEqualTo(20);
    }

    @Test
    void shouldTestFindOptions() {
        FindOptions fo = new FindOptions();
        assertThatThrownBy(() -> fo.limit(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> fo.skip(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThat(new FindOptions().sort("ok").includeSimilarity().pageState("ok")).isNotNull();
        assertThat(new FindOptions().includeSimilarity()).isNotNull();
        assertThat(new FindOptions().limit(10)).isNotNull();
        assertThat(new FindOptions().skip(10)).isNotNull();
    }

    @Test
    void shouldTOverrideMaximumLimits() {
        DataAPIOptions options = DataAPIOptions.builder()
                .withMaxDocumentsInInsert(100)
                .build();

        Projection p1 = new Projection("field1", true);
        Projection p2 = new Projection("field2", true);
        FindOptions options1 = new FindOptions().projection(p1,p2);
        FindOptions options2 = new FindOptions().projection(Projections.include("field1", "field2"));

        InsertManyOptions insertManyOptions = new InsertManyOptions().chunkSize(100);
        //DataAPIClient client = new DataAPIClient("token", options);

    }


}
