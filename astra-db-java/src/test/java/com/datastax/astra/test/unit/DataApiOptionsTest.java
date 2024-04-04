package com.datastax.astra.test.unit;

import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.model.CollectionIdTypes;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.DeleteOneOptions;
import com.datastax.astra.client.model.DeleteResult;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.FilterOperator;
import com.datastax.astra.client.model.FindOneAndDeleteOptions;
import com.datastax.astra.client.model.FindOneAndReplaceOptions;
import com.datastax.astra.client.model.FindOneAndUpdateOptions;
import com.datastax.astra.client.model.FindOneOptions;
import com.datastax.astra.client.model.FindOptions;
import com.datastax.astra.client.model.InsertManyOptions;
import com.datastax.astra.client.model.InsertOneResult;
import com.datastax.astra.client.model.NamespaceInformation;
import com.datastax.astra.client.model.NamespaceOptions;
import com.datastax.astra.client.model.Projections;
import com.datastax.astra.client.model.ReplaceOneOptions;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.client.model.Sorts;
import com.datastax.astra.client.model.Update;
import com.datastax.astra.client.model.UpdateOneOptions;
import com.datastax.astra.client.model.Updates;
import com.datastax.astra.internal.utils.JsonUtils;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataApiOptionsTest {

    @Test
    void shouldPopulateOptions() {
        DataAPIOptions options = DataAPIOptions.builder()
                .withHttpProxy(new DataAPIOptions.HttpProxy("localhost", 8080))
                .withApiVersion("v1")
                .withHttpRedirect(HttpClient.Redirect.NORMAL)
                .withHttpRetryCount(5)
                .withHttpRetryDelayMillis(1000)
                .withDestination(DataAPIOptions.DataAPIDestination.DSE)
                .build();
        assertThat(options.getHttpClientOptions().getProxy().getHostname()).isEqualTo("localhost");
    }

    @Test
    void shouldInitializeInsertManyOptions() {
        assertThat(InsertManyOptions.Builder.chunkSize(10)).isNotNull();
        assertThat(InsertManyOptions.Builder.timeout(10)).isNotNull();
        assertThat(InsertManyOptions.Builder.ordered(true).timeout(10)).isNotNull();
        assertThat(InsertManyOptions.Builder.concurrency(2).timeout(10)).isNotNull();
    }

    @Test
    void shouldFailParsingCollectionIdTypes() {
        assertThatThrownBy(() -> CollectionIdTypes.fromValue("invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldInitializeFindOneAndDeleteOptions() {
        assertThat(FindOneAndDeleteOptions.Builder.sort(Sorts.ascending("test"))).isNotNull();
        assertThat(FindOneAndDeleteOptions.Builder.projection(Projections.include("test"))).isNotNull();
        assertThat(FindOneAndDeleteOptions.Builder.sort(new float[]{})).isNotNull();
        assertThat(FindOneAndDeleteOptions.Builder.sort("OK")
                .sort(Sorts.ascending("test"))
                .projection(Projections.include("test"))
                .sort("ok")
                .sort(new float[]{}))
                .isNotNull();
    }

    @Test
    void shouldInitializeUpdateOne() {
        assertThat(UpdateOneOptions.Builder.sort(Sorts.ascending("test"))).isNotNull();
        assertThat(UpdateOneOptions.Builder.upsert(true)).isNotNull();
        assertThat(UpdateOneOptions.Builder.vector(new float[]{})).isNotNull();
        assertThat(UpdateOneOptions.Builder.vectorize("OK")
                .sort(Sorts.ascending("test"))
                .upsert(true)
                .vectorize("ok")
                .vector(new float[]{}))
                .isNotNull();
    }

    @Test
    void shouldFindOneAndReplaceOptions() {
        assertThat(FindOneAndReplaceOptions.Builder.sort(Sorts.ascending("test"))).isNotNull();
        assertThat(FindOneAndReplaceOptions.Builder.returnDocumentAfter()).isNotNull();
        assertThat(FindOneAndReplaceOptions.Builder.returnDocumentBefore()).isNotNull();
        assertThat(FindOneAndReplaceOptions.Builder.projection(Projections.include("ok"))).isNotNull();
        assertThat(FindOneAndReplaceOptions.Builder.upsert(true)).isNotNull();
        assertThat(FindOneAndReplaceOptions.Builder.sort(new float[]{})).isNotNull();
        assertThat(FindOneAndReplaceOptions.Builder.sort("OK")
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
        assertThat(FindOneAndUpdateOptions.Builder.sort(Sorts.ascending("test"))).isNotNull();
        assertThat(FindOneAndUpdateOptions.Builder.returnDocumentAfter()).isNotNull();
        assertThat(FindOneAndUpdateOptions.Builder.returnDocumentBefore()).isNotNull();
        assertThat(FindOneAndUpdateOptions.Builder.projection(Projections.include("ok"))).isNotNull();
        assertThat(FindOneAndUpdateOptions.Builder.upsert(true)).isNotNull();
        assertThat(FindOneAndUpdateOptions.Builder.sort(new float[]{})).isNotNull();
        assertThat(FindOneAndUpdateOptions.Builder.sort("OK")
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
        assertThat(FindOneOptions.Builder.sort(Sorts.ascending("test"))).isNotNull();
        assertThat(FindOneOptions.Builder.projection(Projections.include("ok"))).isNotNull();
        assertThat(FindOneOptions.Builder.includeSimilarity()).isNotNull();
        assertThat(FindOneOptions.Builder.vector(new float[]{})).isNotNull();
        assertThat(FindOneOptions.Builder.vectorize("OK")
                .sort(Sorts.ascending("test"))
                .includeSimilarity()
                .projection(Projections.include("ok"))
                .sort("ok")
                .sort(new float[]{}))
                .isNotNull();
    }

    @Test
    void shouldTestDeleteOneOptions() {
        assertThat(DeleteOneOptions.Builder.sort(Sorts.ascending("test"))).isNotNull();
        assertThat(DeleteOneOptions.Builder.vector(new float[]{})).isNotNull();
        assertThat(DeleteOneOptions.Builder.vectorize("OK")
                .sort(Sorts.ascending("test"))
                .vectorize("ok")
                .vector(new float[]{}))
                .isNotNull();
    }

    @Test
    void shouldTestReplaceOneOptions() {
        assertThat(ReplaceOneOptions.Builder.upsert(true)).isNotNull();
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

        CollectionOptions.Authentication a = new CollectionOptions.Authentication();
        a.setSecretName("secret");
        a.setType(List.of("type"));
        s.setAuthentication(a);

        CollectionOptions.Parameters p1 = new CollectionOptions.Parameters();
        p1.setHelp("sample parama");
        p1.setType("String");
        p1.setRequired(true);
        p1.setDefaultValue("OK");
        s.setParameters(Map.of("ok", p1));

        v.setService(s);
        c.setVector(v);
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
        assertThat(FindOptions.Builder.sort("ok").includeSimilarity().pageState("ok")).isNotNull();
        assertThat(FindOptions.Builder.includeSimilarity()).isNotNull();
        assertThat(FindOptions.Builder.limit(10)).isNotNull();
        assertThat(FindOptions.Builder.skip(10)).isNotNull();
    }


}
