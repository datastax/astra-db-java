package com.datastax.astra.docs;

import com.datastax.astra.Book;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.TableUpdateOperation;
import com.datastax.astra.client.tables.commands.options.TableFindOneOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.commands.results.TableInsertManyResult;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.internal.serdes.tables.RowMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.tools.javac.Main;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.datastax.astra.client.core.query.Projection.include;
import static com.datastax.astra.client.tables.commands.options.CreateIndexOptions.IF_NOT_EXISTS;
import static com.datastax.astra.internal.serdes.tables.RowMapper.mapAsRow;

public class UpdateOneExample2 {

    public static void main(String[] args) throws Exception {

        // Input
        String astraToken = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
        String astraApiEndpoint = "https://f9e3e87e-1d3e-4f63-ab59-07440e0e694b-us-east-2.apps.astra.datastax.com";

        // Initialize the client.
        DataAPIClient client = new DataAPIClient(astraToken, new DataAPIClientOptions().logRequests());
        Database database = client.getDatabase(astraApiEndpoint);

        // Create table and vector indices
        Table<Book> table = database.createTable(Book.class);
        table.createIndex("idx_rating", "rating", IF_NOT_EXISTS);
        table.createIndex("idx_number_of_pages", "numberOfPages", IF_NOT_EXISTS);

        // Load Json
        File file = new File(Main.class.getClassLoader().getResource("quickstart_dataset.json").toURI());
        List<Book> books = new ObjectMapper().readValue(new FileInputStream(file), new TypeReference<>() {});

        // Populate the Table
        /**
         * Ideally we would do
         * table.insertMany(books);
         *
         * But here we need to override 'summaryGenresVector' with
         * a String to use the "Vectorize" feature of Astra.
         */
        List<Row> rows = books.stream().map(book ->
                mapAsRow(book).put("summaryGenresVector", String.format(
                    "summary: %s | genres: %s", book.getSummary(),
                    String.join(", ", book.getGenres())))).toList();
        database.getTable("quickstart_table").insertMany(rows);

        // Next Search Table

    }

}
