package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.cursor.CollectionFindCursor;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.commands.options.CollectionUpdateManyOptions;
import com.datastax.astra.client.collections.commands.options.ListCollectionOptions;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.collections.commands.results.CollectionUpdateResult;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.exceptions.TooManyDocumentsToCountException;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.paging.Page;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.exceptions.CursorException;
import com.datastax.astra.client.exceptions.DataAPIResponseException;
import com.datastax.astra.test.integration.AbstractCollectionITTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.datastax.astra.client.collections.commands.Updates.set;
import static com.datastax.astra.client.core.query.Filters.and;
import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.core.query.Filters.exists;
import static com.datastax.astra.client.core.query.Filters.gt;
import static com.datastax.astra.client.core.query.Filters.gte;
import static com.datastax.astra.client.core.query.Filters.hasSize;
import static com.datastax.astra.client.core.query.Filters.in;
import static com.datastax.astra.client.core.query.Filters.lt;
import static com.datastax.astra.client.core.query.Filters.lte;
import static com.datastax.astra.client.core.query.Filters.ne;
import static com.datastax.astra.client.core.query.Filters.nin;
import static com.datastax.astra.test.model.TestDataset.COLLECTION_SIMPLE;
import static com.datastax.astra.test.model.TestDataset.COLLECTION_VECTOR;
import static com.datastax.astra.test.model.TestDataset.COMPLETE_DOCUMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Allow to test Collection information.
 */
@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "local")
class Local_10_Collection_ITTest extends AbstractCollectionITTest {

    static final List<Document> FRENCH_SOCCER_TEAM = List.of(
            new Document().id(1).append("firstName", "Lucas").append("lastName", "Hernandez"),
            new Document().id(2).append("firstName", "Antoine").append("lastName", "Griezmann"),
            new Document().id(3).append("firstName", "N'Golo").append("lastName", "Kanté"),
            new Document().id(4).append("firstName", "Tanguy").append("lastName", "Ndombele"),
            new Document().id(5).append("firstName", "Raphaël").append("lastName", "Varane"),
            new Document().id(6).append("firstName", "Hugo").append("lastName", "Lloris"),
            new Document().id(7).append("firstName", "Olivier").append("lastName", "Giroud"),
            new Document().id(8).append("firstName", "Benjamin").append("lastName", "Pavard"),
            new Document().id(9).append("firstName", "Kylian").append("lastName", "Mbappé"),
            new Document().id(10).append("firstName", "Blaise").append("lastName", "Matuidi"),
            new Document().id(11).append("firstName", "Samuel").append("lastName", "Umtiti"),
            new Document().id(12).append("firstName", "Thomas").append("lastName", "Lemar"),
            new Document().id(13).append("firstName", "Ousmane").append("lastName", "Dembélé"),
            new Document().id(14).append("firstName", "Karim").append("lastName", "Benzema"),
            new Document().id(15).append("firstName", "Adrien").append("lastName", "Rabiot"),
            new Document().id(16).append("firstName", "Kingsley").append("lastName", "Coman"),
            new Document().id(17).append("firstName", "Moussa").append("lastName", "Sissoko"),
            new Document().id(18).append("firstName", "Lucas").append("lastName", "Digne"),
            new Document().id(19).append("firstName", "Steve").append("lastName", "Mandanda"),
            new Document().id(20).append("firstName", "Presnel").append("lastName", "Kimpembe"),
            new Document().id(21).append("firstName", "Clement").append("lastName", "Lenglet"),
            new Document().id(22).append("firstName", "Leo").append("lastName", "Dubois"),
            new Document().id(23).append("firstName", "Kurt").append("lastName", "Zouma")
            );

    @Test
    void shouldTestUpdateMany() {
        getCollectionSimple().deleteAll();
        // Given 2 documents in the collection
        Document doc1 = new Document().id(1).append("a", "a").append("b", "c");
        Document doc2 = new Document().id(2).append("a", "a").append("b", "b");
        getCollectionSimple().insertMany(List.of(doc1, doc2));

        // When updated many
        CollectionUpdateResult updateResult = getCollectionSimple()
                .updateMany(eq("a", "a"), set("b", "updated"),
                        new CollectionUpdateManyOptions().upsert(true));

        // Should update 2 documents
        assertThat(updateResult.getMatchedCount()).isEqualTo(2);
        assertThat(updateResult.getModifiedCount()).isEqualTo(2);
        assertThat(updateResult.getUpsertedId()).isNull();
    }

    @Test
    void shouldTestInsertMany() {
        getCollectionSimple().deleteAll();
        CollectionInsertManyResult res = getCollectionSimple().insertMany(FRENCH_SOCCER_TEAM,
                new CollectionInsertManyOptions().ordered(true));
        assertThat(res.getInsertedIds()).hasSize(FRENCH_SOCCER_TEAM.size());
        assertThat(res.getInsertedIds().get(0)).isEqualTo(1);
    }

    @Test
    void shouldInsertManyWithDuplicatesOrder() {
        getDatabase().dropCollection(COLLECTION_SIMPLE);
        Collection<Document> collectionSimple = getDatabase().createCollection(COLLECTION_SIMPLE);
        collectionSimple.deleteAll();
        List<Document> players = new ArrayList<>(FRENCH_SOCCER_TEAM.subList(0, 5));
        // duplicate
        players.add(FRENCH_SOCCER_TEAM.get(4));
        players.addAll(FRENCH_SOCCER_TEAM.subList(5,7));

        getDatabase().listCollectionNames(new ListCollectionOptions().timeout(1000));

        try {
            CollectionInsertManyResult res = collectionSimple.insertMany(players,
                    new CollectionInsertManyOptions().ordered(true));
        } catch (DataAPIResponseException res) {
            assertThat(res.getCommandsList()).hasSize(1);
            assertThat(res.getCommandsList().get(0).getResponse().getErrors()).hasSize(1);
            List<Integer> insertedIds = res.getCommandsList().get(0)
                    .getResponse().getStatus().getInsertedIds(Integer.class);
            assertThat(insertedIds).hasSize(5);
        }

        try {
            collectionSimple.deleteAll();
            CollectionInsertManyResult res = collectionSimple.insertMany(players, new CollectionInsertManyOptions().ordered(false));
        } catch (DataAPIResponseException res) {
            assertThat(res.getCommandsList()).hasSize(1);
            assertThat(res.getCommandsList().get(0).getResponse().getErrors()).hasSize(1);
            List<Integer> insertedIds = res.getCommandsList().get(0)
                    .getResponse().getStatus().getInsertedIds(Integer.class);
            assertThat(insertedIds).hasSize(7);
        }

        try {
            collectionSimple.deleteAll();
            CollectionInsertManyResult res = collectionSimple.insertMany(players,
                    new CollectionInsertManyOptions().ordered(false).chunkSize(10).concurrency(1));
        } catch (DataAPIResponseException res) {
            assertThat(res.getCommandsList()).hasSize(1);
            assertThat(res.getCommandsList().get(0).getResponse().getErrors()).hasSize(1);
            List<Integer> insertedIds = res.getCommandsList().get(0).getResponse()
                    .getStatus().getInsertedIds(Integer.class);
            assertThat(insertedIds).hasSize(7);
        }
    }

    @Test
    void shouldInsertManyChunkedParallel() throws TooManyDocumentsToCountException {
        List<Document> documents = new ArrayList<>();
        long start = System.currentTimeMillis();
        int nbDocs = 999;
        for (int i = 0; i < nbDocs; i++) {
            documents.add(new Document().id(i).append("idx", i));
        }
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertMany(documents, new CollectionInsertManyOptions().ordered(false).concurrency(5));
        assertThat(getCollectionSimple().countDocuments(1000)).isEqualTo(nbDocs);

        getCollectionSimple().deleteMany(gt("idx", 500));
        assertThat(getCollectionSimple().countDocuments(1000)).isEqualTo(501);
    }

    @Test
    void shouldTestFindWithFilters() {
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertMany(FRENCH_SOCCER_TEAM);
        assertThat(getCollectionSimple().find(gte("_id", 20)).toList()).hasSize(4);
        assertThat(getCollectionSimple().find(gt("_id", 20)).toList()).hasSize(3);

        assertThat(getCollectionSimple().find(lt("_id", 3)).toList()).hasSize(2);
        assertThat(getCollectionSimple().find(lte("_id", 3)).toList()).hasSize(3);
        assertThat(getCollectionSimple().find(ne("_id", 20)).toList()).hasSize(22);
        assertThat(getCollectionSimple().find(exists("firstName")).toList()).hasSize(23);
        assertThat(getCollectionSimple().find(and(
                exists("firstName"),
                gte("_id", 20)))
                .toList()).hasSize(4);
    }

    @Test
    void shouldFindWithExtraOptions() {
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertOne(COMPLETE_DOCUMENT);

        assertThat(getCollectionSimple().find(new Filter()
                .where("metadata_string")
                .isInArray(new String[]{"hello", "world"})).toList())
                .hasSize(1);
        assertThat(getCollectionSimple().find(in("metadata_string", "hello", "world"))
                .toList()).hasSize(1);

        assertThat(getCollectionSimple().find(new Filter().where("metadata_string")
                .isNotInArray(new String[]{"Hallo", "Welt"}))
                .toList()).hasSize(1);
        assertThat(getCollectionSimple().find(nin("metadata_string", "Hallo", "Welt"))
                .toList()).hasSize(1);

        assertThat(getCollectionSimple().find(new Filter().where("metadata_boolean_array")
                .hasSize(3))
                .toList()).hasSize(1);
        assertThat(getCollectionSimple().find(hasSize("metadata_boolean_array", 3))
                .toList()).hasSize(1);

        assertThat(getCollectionSimple().find(new Filter()
                        .where("metadata_instant")
                        .isLessThan(Instant.now()))
                .toList()).hasSize(1);
    }

    @Test
    void shouldDoSemanticSearch() {
       getCollectionVector();

       // Made at environment level for Serializers
       DataAPIClientOptions.getSerdesOptions().disableEncodeDataApiVectorsAsBase64();

       Collection<Document> collectionVectorRaw = getDatabase().getCollection(COLLECTION_VECTOR);
       collectionVectorRaw.deleteAll();
       collectionVectorRaw.insertMany(List.of(
                new Document()
                        .id("doc1") // generated if not set
                        .vector(new float[]{1f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f})
                        .append("product_name", "HealthyFresh - Beef raw dog food")
                        .append("product_price", 12.99),
                new Document()
                        .id("doc2")
                        .vector(new float[]{1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f})
                        .append("product_name", "HealthyFresh - Chicken raw dog food")
                        .append("product_price", 9.99),
                new Document()
                        .id("doc3")
                        .vector(new float[]{1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f})
                        .append("product_name", "product_name, HealthyFresh - Chicken raw dog food")
                        .append("product_price", 9.99),
                new Document()
                        .id("doc4")
                        .vector(new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f})
                        .append("product_name", "HealthyFresh - Chicken raw dog food")
                        .append("product_price", 9.99)));

        // Perform a similarity search
        Filter metadataFilter = new Filter().where("product_price").isEqualsTo(9.99);
        CollectionFindOptions options = new CollectionFindOptions()
                .sort(Sort.vector(new float[] {1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f}))
                .limit(2);
        CollectionFindCursor<Document, Document> docs = collectionVectorRaw.find(metadataFilter, options);
        assertThat(docs.toList()).hasSize(2);
    }

    @Test
    void shouldFindOneAndDelete() {
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertOne(COMPLETE_DOCUMENT);
        Optional<Document> doc = getCollectionSimple().findOneAndDelete(eq("1"));
        assertThat(doc).isPresent();

        getCollectionSimple().insertOneAsync(COMPLETE_DOCUMENT).thenApply(doc1 -> {
            Optional<Document> doc2 = getCollectionSimple().findOneAndDeleteAsync(eq("1")).join();
            assertThat(doc2).isPresent();
            return doc1;
        });
    }

    @Test
    void shouldFindPage() {
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertOne(COMPLETE_DOCUMENT);
        Page<Document> p1 = getCollectionSimple().findPage(eq("1"), new CollectionFindOptions());
        assertThat(p1).isNotNull();
        assertThat(p1.one()).isNotNull();
        Page<Document> p2 = getCollectionSimple().findPage(eq("1"), new CollectionFindOptions());
        assertThat(p2).isNotNull();
        assertThat(p2.findFirst()).isPresent();
    }

    @Test
    void shouldFindIterable() {
        getCollectionSimple().deleteAll();
        Document doc1 = new Document().id(1).append("a", "a").append("b", "c");
        Document doc2 = new Document().id(2).append("a", "a").append("b", "b");
        getCollectionSimple().insertMany(List.of(doc1, doc2));

        CollectionFindCursor<Document, Document> iter = getCollectionSimple().findAll();;
        iter.toList();
        assertThatThrownBy(iter::toList).isInstanceOf(CursorException.class);

        CollectionFindCursor<Document, Document> iter2 = getCollectionSimple().findAll();;
        iter2.iterator().next();
        assertThatThrownBy(iter2::toList).isInstanceOf(CursorException.class);
    }


}
