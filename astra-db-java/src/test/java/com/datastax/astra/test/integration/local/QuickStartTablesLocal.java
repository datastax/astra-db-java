package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.TableDefinition;
import com.datastax.astra.client.tables.results.TableInsertOneResult;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import com.datastax.astra.client.tables.row.Row;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.datastax.astra.client.core.options.DataAPIClientOptions.DEFAULT_KEYSPACE;
import static com.datastax.astra.client.tables.columns.ColumnTypes.INT;
import static com.datastax.astra.client.tables.columns.ColumnTypes.TEXT;
import static com.datastax.astra.client.tables.ddl.CreateTableOptions.IF_NOT_EXISTS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class QuickStartTablesLocal {

    @Test
    public void should_quickstart_tables_default() {
        // Connect to local and create default_keyspace
        Database localDb = DataAPIClients.localDbWithDefaultKeyspace();
        assertThat(localDb.getKeyspace()).isEqualTo(DEFAULT_KEYSPACE);

        // Create table (explicit definition)
        TableDefinition tableDefinition = new TableDefinition()
                .addColumnText("id")
                .addColumnInt("age")
                .addColumnText("name")
                .partitionKey("id");
        Table<Row> tablePersonDefault = localDb.createTable("person_default", tableDefinition, IF_NOT_EXISTS);
        assertThat(localDb.tableExists("person_default")).isTrue();

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
        assertThat(john).isNotEmpty();
    }

    // ---- Enhancement with the Object Mapping

    @Data
    @EntityTable("person")
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Person {

        @PartitionBy(0)
        @Column(value="id", type=TEXT)
        private String id;

        @PartitionBy(1)
        @Column(value="name", type=TEXT)
        private String name;

        @Column(value="age", type=INT)
        private int age;
    }

    @Test
    public void should_quickstart_tables_om() {
        // Connect to local and create default_keyspace
        Database localDb = DataAPIClients.localDbWithDefaultKeyspace();
        assertThat(localDb.getKeyspace()).isEqualTo(DEFAULT_KEYSPACE);

        // Create table (introspecting bean)
        Table<Person> tablePerson = localDb.createTable(Person.class, IF_NOT_EXISTS);
        assertThat(localDb.tableExists("person")).isTrue();

        // Inserting data
        tablePerson.insertOne(new Person("John", "Doe", 12));

        // Retrieve data
        Optional<Person> john = tablePerson.findOne(new Filter()
                .where("id").isEqualsTo( "John")
                .where("name").isEqualsTo( "Doe"));
        assertThat(john).isNotEmpty();

    }


}
