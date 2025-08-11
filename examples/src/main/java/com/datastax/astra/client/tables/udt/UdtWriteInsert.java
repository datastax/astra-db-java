package com.datastax.astra.client.tables.udt;

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

public class UdtWriteInsert {

    public static Database getHCDDatabase(String url, String username, String password, String keyspace) {
        String authToken = new UsernamePasswordTokenProvider(username, password)
                .getToken();
        DataAPIClientOptions options = new DataAPIClientOptions()
                .destination(DataAPIDestination.HCD)
                .enableFeatureFlagTables()
                .logRequests();
        DataAPIClient client = new DataAPIClient(authToken,options);
        return client.getDatabase(url, keyspace);
    }

    // Optional Beans for Object Mapping

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonBean{
        private String name;
        private PersonAddress address;
        @JsonProperty("address_list")
        private List<PersonAddress> addressList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonAddress{
        private String city;
        private int zipcode;
    }

    public static void main(String[] args) {

        Database database = getHCDDatabase(
                "http://localhost:8181",
                "cassandra",
                "cassandra",
                "quickstart_keyspace");

        database.createType("udt_address", new TableUserDefinedTypeDefinition()
                .addFieldText("city")
                .addFieldInt("zipcode"), CreateTypeOptions.IF_NOT_EXISTS);

        database.createTable("person", new TableDefinition()
                .addColumnText("name")
                .addColumnUserDefinedType("address", "udt_address")
                .addColumnListUserDefinedType("address_list", "udt_address")
                .addPartitionBy("name"), IF_NOT_EXISTS);

        database.getTable("person")
                .insertOne(new Row()
                        .add("name", "cedrick")
                        .add("address", Map.of("zipcode", 12345, "city", "Paris")));

        PersonBean sara = new PersonBean("sara",
         new PersonAddress("Paris", 75018),
         List.of(
          new PersonAddress("San franscico", 12345),
          new PersonAddress("New York", 55555)
         ));

        database.getTable("person", PersonBean.class).insertOne(sara);

    }
}
