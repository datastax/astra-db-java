package com.datastax.astra.client.tables.bm25;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.lexical.Analyzer;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.definition.rows.Row;

import java.io.IOException;
import java.io.InputStream;

import static com.datastax.astra.client.core.lexical.AnalyzerTypes.STANDARD;

public class FindOneLexical {

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

    public static Table<Row> createTable(Database db) {
        // Create Table
        Table<Row> table = db.createTable("t_lexical_demo", new TableDefinition()
                .addColumnText("email")
                .addColumnText("name")
                .partitionKey("email"), CreateTableOptions.IF_NOT_EXISTS);

        // Create Text Index
        table.createTextIndex("idx_name", "name");
        return table;
    }

    public static void main(String[] args) throws IOException {

        Database database = getHCDDatabase(
                "http://localhost:8181",
                "cassandra",
                "cassandra",
                "quickstart_keyspace");

        // Create a collection with Lexical
        Table<Row> tLexical = createTable(database);

        // FindOne Match
        tLexical.findOne(Filters
          .match("name", "Fortune favours the bold"))
          .ifPresent(System.out::println);
    }
}