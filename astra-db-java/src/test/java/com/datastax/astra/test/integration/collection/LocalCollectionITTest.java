package com.datastax.astra.test.integration.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.exception.DataApiResponseException;
import com.datastax.astra.client.exception.TooManyDocumentsToCountException;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.InsertManyResult;
import com.datastax.astra.client.model.UpdateResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.datastax.astra.client.model.Filters.and;
import static com.datastax.astra.client.model.Filters.eq;
import static com.datastax.astra.client.model.Filters.exists;
import static com.datastax.astra.client.model.Filters.gt;
import static com.datastax.astra.client.model.Filters.gte;
import static com.datastax.astra.client.model.Filters.hasSize;
import static com.datastax.astra.client.model.Filters.in;
import static com.datastax.astra.client.model.Filters.lt;
import static com.datastax.astra.client.model.Filters.lte;
import static com.datastax.astra.client.model.Filters.ne;
import static com.datastax.astra.client.model.Filters.nin;
import static com.datastax.astra.client.model.InsertManyOptions.Builder.ordered;
import static com.datastax.astra.client.model.UpdateManyOptions.Builder.upsert;
import static com.datastax.astra.client.model.Updates.set;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Allow to test Collection information.
 */
class LocalCollectionITTest extends AbstractCollectionITTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        return DataAPIClients.createDefaultLocalDatabase();
    }

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
    public void shouldTestUpdateMany() {
        getCollectionSimple().deleteAll();
        // Given 2 documents in the collection
        Document doc1 = new Document().id(1).append("a", "a").append("b", "c");
        Document doc2 = new Document().id(2).append("a", "a").append("b", "b");
        getCollectionSimple().insertMany(List.of(doc1, doc2));

        // When updated many
        UpdateResult updateResult = getCollectionSimple()
                .updateMany(eq("a", "a"), set("b", "updated"), upsert(true));

        // Should update 2 documents
        assertThat(updateResult.getMatchedCount()).isEqualTo(2);
        assertThat(updateResult.getModifiedCount()).isEqualTo(2);
        assertThat(updateResult.getUpsertedId()).isNull();
    }

    @Test
    public void shouldTestInsertMany() {
        getCollectionSimple().deleteAll();
        InsertManyResult res = getCollectionSimple().insertMany(FRENCH_SOCCER_TEAM, ordered(true));
        assertThat(res.getInsertedIds()).hasSize(FRENCH_SOCCER_TEAM.size());
        assertThat(res.getInsertedIds().get(0)).isEqualTo(1);
    }

    @Test
    public void shouldInsertManyWithDuplicatesOrder() {
        getCollectionSimple().deleteAll();
        List<Document> players = new ArrayList<>(FRENCH_SOCCER_TEAM.subList(0, 5));
        // duplicate
        players.add(FRENCH_SOCCER_TEAM.get(4));
        players.addAll(FRENCH_SOCCER_TEAM.subList(5,7));

        try {
            InsertManyResult res = getCollectionSimple().insertMany(players, ordered(true));
        } catch (DataApiResponseException res) {
            assertThat(res.getCommandsList()).hasSize(1);
            assertThat(res.getCommandsList().get(0).getResponse().getErrors()).hasSize(1);
            List<Integer> insertedIds = res.getCommandsList().get(0).getResponse().getStatus().getList("insertedIds", Integer.class);
            assertThat(insertedIds).hasSize(5);
        }

        try {
            getCollectionSimple().deleteAll();
            InsertManyResult res = getCollectionSimple().insertMany(players, ordered(false));
        } catch (DataApiResponseException res) {
            assertThat(res.getCommandsList()).hasSize(1);
            assertThat(res.getCommandsList().get(0).getResponse().getErrors()).hasSize(1);
            List<Integer> insertedIds = res.getCommandsList().get(0).getResponse().getStatus().getList("insertedIds", Integer.class);
            assertThat(insertedIds).hasSize(7);
        }

        try {
            getCollectionSimple().deleteAll();
            InsertManyResult res = getCollectionSimple().insertMany(players,
                    ordered(false).timeout(10000).chunkSize(10).concurrency(1));
        } catch (DataApiResponseException res) {
            assertThat(res.getCommandsList()).hasSize(1);
            assertThat(res.getCommandsList().get(0).getResponse().getErrors()).hasSize(1);
            List<Integer> insertedIds = res.getCommandsList().get(0).getResponse().getStatus().getList("insertedIds", Integer.class);
            assertThat(insertedIds).hasSize(7);
        }
    }

    @Test
    public void shouldInsertManyChunkedParallel() throws TooManyDocumentsToCountException {
        List<Document> documents = new ArrayList<>();
        long start = System.currentTimeMillis();
        int nbDocs = 999;
        for (int i = 0; i < nbDocs; i++) {
            documents.add(new Document().id(i).append("idx", i));
        }
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertMany(documents, ordered(false).concurrency(5));
        assertThat(getCollectionSimple().countDocuments(1000)).isEqualTo(nbDocs);

        getCollectionSimple().deleteMany(gt("idx", 500));
        assertThat(getCollectionSimple().countDocuments(1000)).isEqualTo(501);
    }

    @Test
    public void shouldTestFindWithFilters() {
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertMany(FRENCH_SOCCER_TEAM);
        assertThat(getCollectionSimple().find(gte("_id", 20))
                .all().size()).isEqualTo(4);
        assertThat(getCollectionSimple().find(gt("_id", 20))
                .all().size()).isEqualTo(3);
        assertThat(getCollectionSimple().find(lt("_id", 3))
                .all().size()).isEqualTo(2);
        assertThat(getCollectionSimple().find(lte("_id", 3))
                .all().size()).isEqualTo(3);
        assertThat(getCollectionSimple().find(ne("_id", 20))
                .all().size()).isEqualTo(22);
        assertThat(getCollectionSimple().find(exists("firstName"))
                .all().size()).isEqualTo(23);
        assertThat(getCollectionSimple().find(and(exists("firstName"), gte("_id", 20)))
                .all().size()).isEqualTo(4);
    }

    @Test
    public void shouldFindWithExtraOptions() {
        getCollectionSimple().deleteAll();
        getCollectionSimple().insertOne(COMPLETE_DOCUMENT);

        assertThat(getCollectionSimple().find(new Filter()
                .where("metadata_string")
                .isInArray(new String[]{"hello", "world"})).all().size()).isEqualTo(1);
        assertThat(getCollectionSimple().find(in("metadata_string", "hello", "world"))
                .all().size()).isEqualTo(1);

        assertThat(getCollectionSimple().find(new Filter().where("metadata_string")
                .isNotInArray(new String[]{"Hallo", "Welt"}))
                .all().size()).isEqualTo(1);
        assertThat(getCollectionSimple().find(nin("metadata_string", "Hallo", "Welt"))
                .all().size()).isEqualTo(1);

        assertThat(getCollectionSimple().find(new Filter().where("metadata_boolean_array")
                .hasSize(3))
                .all().size()).isEqualTo(1);
        assertThat(getCollectionSimple().find(hasSize("metadata_boolean_array", 3))
                .all().size()).isEqualTo(1);

        assertThat(getCollectionSimple().find(new Filter()
                        .where("metadata_instant")
                        .isLessThan(Instant.now()))
                .all().size()).isEqualTo(1);
    }

    @Test
    public void shouldDoSemanticSearch() {
       getCollectionVector();
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
        float[] embeddings = new float[] {1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
        Filter metadataFilter = new Filter().where("product_price").isEqualsTo(9.99);
        List<Document> docs = collectionVectorRaw.find(null, embeddings, 2).all();
        assertThat(docs).hasSize(2);
    }

}
