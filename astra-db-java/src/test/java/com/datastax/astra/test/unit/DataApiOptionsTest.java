package com.datastax.astra.test.unit;

import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.model.CollectionIdTypes;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.DeleteOneOptions;
import com.datastax.astra.client.model.DeleteResult;
import com.datastax.astra.client.model.http.HttpProxy;
import com.datastax.astra.client.model.query.Filter;
import com.datastax.astra.client.model.query.FilterOperator;
import com.datastax.astra.client.model.FindOneAndDeleteOptions;
import com.datastax.astra.client.model.FindOneAndReplaceOptions;
import com.datastax.astra.client.model.FindOneAndUpdateOptions;
import com.datastax.astra.client.model.FindOneOptions;
import com.datastax.astra.client.model.FindOptions;
import com.datastax.astra.client.model.InsertManyOptions;
import com.datastax.astra.client.model.InsertOneResult;
import com.datastax.astra.client.model.NamespaceInformation;
import com.datastax.astra.client.model.NamespaceOptions;
import com.datastax.astra.client.model.query.Projection;
import com.datastax.astra.client.model.query.Projections;
import com.datastax.astra.client.model.ReplaceOneOptions;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.client.model.query.Sorts;
import com.datastax.astra.client.model.Update;
import com.datastax.astra.client.model.UpdateOneOptions;
import com.datastax.astra.client.model.Updates;
import com.datastax.astra.internal.utils.JsonUtils;
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
    void shouldTestNamespaceInformation() {
        NamespaceInformation ni1 = new NamespaceInformation();
        NamespaceOptions options = NamespaceOptions.simpleStrategy(1);
        ni1.setName("test");
        ni1.setOptions(options);
        assertThat(JsonUtils.marshall(ni1)).isNotNull();

        NamespaceInformation ni2 = new NamespaceInformation("test");
        ni2.setOptions(options);
        assertThat(JsonUtils.marshall(ni2)).isNotNull();

        assertThat(ni1.getName()).isEqualTo(ni2.getName());
        assertThat(ni1.getOptions()).isEqualTo(ni2.getOptions());
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

        CollectionOptions.VectorOptions v = new CollectionOptions.VectorOptions();

        CollectionOptions.Service s = new CollectionOptions.Service();
        s.setProvider("OK");
        s.setModelName("OK");

        CollectionOptions.Parameters p1 = new CollectionOptions.Parameters();
        p1.setHelp("sample parama");
        p1.setType("String");
        p1.setRequired(true);
        p1.setDefaultValue("OK");
        s.setParameters(Map.of("ok", p1));

        v.setService(s);
        c.setVector(v);
        System.out.println(JsonUtils.marshall(c));
        assertThat(JsonUtils.marshall(c)).isNotNull();
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
