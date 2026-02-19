package com.datastax.astra.samples.tables;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.CreateTypeOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeDefinition;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

import static com.datastax.astra.client.tables.commands.options.CreateTableOptions.IF_NOT_EXISTS;

/**
 * Demonstrates UDT (User-Defined Type) with nested POJO mapping:
 * {@code PersonBean} containing {@code PersonAddress}, insert with Map and typed POJO.
 *
 * @see TableUserDefinedTypeDefinition
 */
@SuppressWarnings("unused")
public class SampleTableUdtObjectMapping {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PersonBean {
        private String name;
        private PersonAddress address;
        @JsonProperty("address_list")
        private List<PersonAddress> addressList;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PersonAddress {
        private String city;
        private int zipcode;
    }

    /** Helper to create an HCD database connection. */
    static Database getHCDDatabase(String url, String username, String password, String keyspace) {
        String authToken = new UsernamePasswordTokenProvider(username, password).getToken();
        DataAPIClientOptions options = new DataAPIClientOptions()
                .destination(DataAPIDestination.HCD)
                .enableFeatureFlagTables()
                .logRequests();
        return new DataAPIClient(authToken, options).getDatabase(url, keyspace);
    }

    /** Create UDT, table, then insert with Map and POJO. */
    static void udtInsert() {
        Database database = getHCDDatabase(
                "http://localhost:8181", "cassandra", "cassandra", "quickstart_keyspace");

        // Create the UDT
        database.createType("udt_address", new TableUserDefinedTypeDefinition()
                .addFieldText("city")
                .addFieldInt("zipcode"), CreateTypeOptions.IF_NOT_EXISTS);

        // Create a table using the UDT
        database.createTable("person", new TableDefinition()
                .addColumnText("name")
                .addColumnUserDefinedType("address", "udt_address")
                .addColumnListUserDefinedType("address_list", "udt_address")
                .addPartitionBy("name"), IF_NOT_EXISTS);

        // Insert with raw Map
        database.getTable("person")
                .insertOne(new Row()
                        .add("name", "cedrick")
                        .add("address", Map.of("zipcode", 12345, "city", "Paris")));

        // Insert with typed POJO
        PersonBean sara = new PersonBean("sara",
                new PersonAddress("Paris", 75018),
                List.of(
                        new PersonAddress("San Francisco", 12345),
                        new PersonAddress("New York", 55555)
                ));
        database.getTable("person", PersonBean.class).insertOne(sara);
    }
}
