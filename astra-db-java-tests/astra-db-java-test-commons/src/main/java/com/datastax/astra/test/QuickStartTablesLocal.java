package com.datastax.astra.test;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.commands.results.TableInsertOneResult;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class QuickStartTablesLocal {

    @Test
    public void should_quickstart_tables_default() {
        // Connect to local and create default_keyspace
        Database localDb = DataAPIClients.localDbWithDefaultKeyspace();
        AssertionsForClassTypes.assertThat(localDb.getKeyspace()).isEqualTo(DataAPIClientOptions.DEFAULT_KEYSPACE);

        // Create table (explicit definition)
        TableDefinition tableDefinition = new TableDefinition()
                .addColumnText("id")
                .addColumnInt("age")
                .addColumnText("name")
                .partitionKey("id");
        Table<Row> tablePersonDefault = localDb.createTable("person_default", tableDefinition, CreateTableOptions.IF_NOT_EXISTS);
        AssertionsForClassTypes.assertThat(localDb.tableExists("person_default")).isTrue();

        // Inserting data
        Row row = new Row()
                .addInt("age", 42)
                .addText("name", "John")
                .addText("id",  "cedrick@datastax.com");
        TableInsertOneResult res = tablePersonDefault.insertOne(row);

        // Retrieve data
        Optional<Row> john = tablePersonDefault.findOne(new Filter()
                .where("id").isEqualsTo( "John")
                .where("name").isEqualsTo( "Doe"));
        AssertionsForClassTypes.assertThat(john).isNotEmpty();
    }

    // ---- Enhancement with the Object Mapping

    @Data
    @EntityTable("person")
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Person {

        @PartitionBy(0)
        @Column(name ="id", type= TableColumnTypes.TEXT)
        private String id;

        @PartitionBy(1)
        @Column(name ="name", type= TableColumnTypes.TEXT)
        private String name;

        @Column(name ="age", type= TableColumnTypes.INT)
        private int age;
    }

    @Test
    public void should_quickstart_tables_om() {
        // Connect to local and create default_keyspace
        Database localDb = DataAPIClients.localDbWithDefaultKeyspace();
        AssertionsForClassTypes.assertThat(localDb.getKeyspace()).isEqualTo(DataAPIClientOptions.DEFAULT_KEYSPACE);

        // Create table (introspecting bean)
        Table<Person> tablePerson = localDb.createTable(Person.class, CreateTableOptions.IF_NOT_EXISTS);
        AssertionsForClassTypes.assertThat(localDb.tableExists("person")).isTrue();

        // Inserting data
        tablePerson.insertOne(new Person("John", "Doe", 12));

        // Retrieve data
        Optional<Person> john = tablePerson.findOne(new Filter()
                .where("id").isEqualsTo( "John")
                .where("name").isEqualsTo( "Doe"));
        AssertionsForClassTypes.assertThat(john).isNotEmpty();

    }


}
