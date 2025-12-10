package com.datastax.astra.test.demo;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.internal.serdes.collections.DocumentSerializer;
import com.datastax.astra.test.demo.dto.PersonUdtEntity;
import com.datastax.astra.test.demo.dto.Player;
import com.datastax.astra.test.demo.dto.TableGroupEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/*
 * Flight DECK for Astra DEV
 *
 * https://flightdeck.dev.cloud-tools.datastax.com/database/f9754177-52e2-4b66-935e-78cfd0be0042
 */
public class DemoAnnotations {

    // ------------------------------
    // --         Data Set         --
    // ------------------------------

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

    static final List<Player> FRENCH_SOCCER_TEAM_2 = List.of(
            new Player(1, "Lucas", "Hernandez"),
            new Player(2, "Antoine", "Griezmann"),
            new Player(3, "N'Golo", "Kanté"),
            new Player(4, "Tanguy", "Ndombele"),
            new Player(5, "Raphaël", "Varane"),
            new Player(6, "Hugo", "Lloris"),
            new Player(7, "Olivier", "Giroud"),
            new Player(8, "Benjamin", "Pavard"),
            new Player(9, "Kylian", "Mbappé"),
            new Player(10, "Blaise", "Matuidi"),
            new Player(11, "Samuel", "Umtiti"),
            new Player(12, "Thomas", "Lemar"),
            new Player(13, "Ousmane", "Dembélé"),
            new Player(14, "Karim", "Benzema"),
            new Player(15, "Adrien", "Rabiot"),
            new Player(16, "Kingsley", "Coman"),
            new Player(17, "Moussa", "Sissoko"),
            new Player(18, "Lucas", "Digne"),
            new Player(19, "Steve", "Mandanda"),
            new Player(20, "Presnel", "Kimpembe"),
            new Player(21, "Clement", "Lenglet"),
            new Player(22, "Leo", "Dubois"),
            new Player(23, "Kurt", "Zouma")
    );

    // ------------------------------
    // --  CONNECTIVITY STUFF     ---
    // ------------------------------

    public static final String ASTRA_DB_TOKEN =
        System.getenv("ASTRA_DB_APPLICATION_TOKEN");
    public static final String DB_URL_VECTOR =
        "https://c4086b46-dcb0-4180-a1e3-d7483fcce01b-us-east1.apps.astra.datastax.com";

    private DataAPIClient getAstraDevDataApiClient() {
        return new DataAPIClient(ASTRA_DB_TOKEN, new DataAPIClientOptions().logRequests());
    }
    private Database getDatabase() {
        return getAstraDevDataApiClient().getDatabase(DB_URL_VECTOR);
    }

    @Test
    public void should_cleanup_keyspace()  {
        getDatabase().dropCollection("collection");
        //getDatabase().dropCollection("collection_players");
       //getDatabase().dropTable("table_group");
    }

    // ------------------------------
    // --       TEST CASES        ---
    // ------------------------------

    @Test
    public void should_serialize_documents()  {

        Document demo = new Document()
                .id(UUID.randomUUID())
                .append("today", Instant.now())
                .append("name", "Cedrick")
                .append("tags", List.of("A", "B", "C"))
                .vector(new float [] {.1f, .2f, .3f});

        DocumentSerializer serializer = new DocumentSerializer();

        //System.out.println(serializer.marshall(demo));

        System.out.println(serializer.marshall(new Player(31, "cedrick", "clu")));


    }


    @Test
    public void should_create_collection_1()  {
        Collection<Document> collection = getDatabase()
                .createCollection("collection_players");
        collection.insertMany(FRENCH_SOCCER_TEAM, new CollectionInsertManyOptions().ordered(true));
    }

    @Test
    public void should_create_collection_2()  {
        Collection<Player> collectionPlayer = getDatabase()
                .getCollection("collection_players", Player.class);

        collectionPlayer
                .findAll().toList()
                .stream()
                .map(Player::getLastName)
                .forEach(System.out::println);
    }

    @Test
    public void should_create_table_with_composite_udt() {

//        getDatabase()
//                .createType(PersonUdtEntity.class, CreateTypeOptions.IF_NOT_EXISTS);
//        getDatabase()
//                .createTable(TableGroupEntity.class, IF_NOT_EXISTS);



        Table<TableGroupEntity> myGroup = getDatabase().getTable(TableGroupEntity.class);

        TableGroupEntity group = new TableGroupEntity();
        group.setId(UUID.randomUUID());
        group.setGroupLeader(new PersonUdtEntity("cedrick", 20));
        group.setGroupMembers(Set.of(
                new PersonUdtEntity("cedrick", 20),
                new PersonUdtEntity("stefano", 21)
        ));


        myGroup.insertOne(group);

        myGroup.findAll().stream().forEach(demo ->
                System.out.println(demo.getGroupLeader().getAge())
        );




    }


}
