package com.datastax.astra.docs;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.TableUpdateOperation;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOneOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.commands.results.TableInsertManyResult;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.ColumnTypes;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;
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

public class UpdateOneExample {

    public static void main(String[] args) throws Exception {
        // Input
        String astraToken = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
        String astraApiEndpoint = "https://f9e3e87e-1d3e-4f63-ab59-07440e0e694b-us-east-2.apps.astra.datastax.com";

        // Initialize the client.
        DataAPIClient client = new DataAPIClient(astraToken, new DataAPIClientOptions().logRequests());
        Database database = client.getDatabase(astraApiEndpoint);
        createSchema(database);

        // populate date
        Table<Row> table = database.getTable("quickstartTable");
        //populateTable(table);
        //searchTable(table);

        //updateTable(table);
//        Filter filter = Filters.and(
//                Filters.eq("title", "Hidden Shadows of the Past"),
//                Filters.eq("author", "John Anthony"));
//        table.find(filter).toList().forEach(row -> System.out.println(row));
    }

    public static void createSchema(Database database) {
            // Create the table
            TableDefinition tableDefinition =
                    new TableDefinition()
                            // Define all of the columns in the table
                            .addColumnText("title")
                            .addColumnText("author")
                            .addColumnInt("numberOfPages")
                            .addColumn("rating", ColumnTypes.FLOAT)
                            .addColumnInt("publicationYear")
                            .addColumnText("summary")
                            .addColumnSet("genres", ColumnTypes.TEXT)
                            .addColumnMap("metadata", ColumnTypes.TEXT, ColumnTypes.TEXT)
                            .addColumnBoolean("isCheckedOut")
                            .addColumnText("borrower")
                            .addColumn("dueDate", ColumnTypes.DATE)
                            .addColumnVector(
                                    "summaryGenresVector",
                                    new ColumnDefinitionVector()
                                            .dimension(1024)
                                            .metric(SimilarityMetric.COSINE)
                                            .service(new VectorServiceOptions().provider("nvidia").modelName("NV-Embed-QA")))
                            // Define the primary key for the table.
                            // In this case, the table uses a composite primary key.
                            .addPartitionBy("title")
                            .addPartitionBy("author");

            VectorServiceOptions vectorServiceOptions = new VectorServiceOptions();


            // Default Table Creation
            Table<Row> table = database.createTable(
                    "quickstartTable",
                    tableDefinition,
                    CreateTableOptions.IF_NOT_EXISTS);
            System.out.println("Created table.");

            // Index any columns that you want to sort and filter on.
            table.createIndex("ratingIndex", "rating", IF_NOT_EXISTS);
            table.createIndex("numberOfPagesIndex", "numberOfPages", IF_NOT_EXISTS);
            TableVectorIndexDefinition definition =
                    new TableVectorIndexDefinition()
                            .column("summaryGenresVector")
                            .metric(SimilarityMetric.COSINE);
            table.createVectorIndex("summaryGenresVectorIndex", definition, CreateVectorIndexOptions.IF_NOT_EXISTS);
            System.out.println("Indexed columns.");
    }

    public static void populateTable(Table<Row> table) throws Exception {
        // Initialize Jackson ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(Main.class.getClassLoader().getResource("quickstart_dataset.json").toURI());

        try (FileInputStream stream = new FileInputStream(file)) {
            List<Row> rows = objectMapper.readValue(stream, new TypeReference<>() {});
            rows.forEach(
                    row -> {
                        // Deserialize the "genres" field into a HashSet
                        row.add("genres", new HashSet<>(row.getList("genres", String.class)));

                        // Deserialize the "metadata" field into a Map
                        Map<String, String> metadataMap =
                                objectMapper.convertValue(row.get("metadata"), new TypeReference<Map<String, String>>() {});
                        row.add("metadata", metadataMap);

                        // Deserialize the "dueDate" field into a Date or null
                        row.add("dueDate", parseDate(row.getText("dueDate")));

                        // Add a field of text to vectorize
                        String summary = row.getText("summary");
                        String genres = String.join(", ", row.getList("genres", String.class));
                        String summaryGenresVector =
                                String.format("summary: %s | genres: %s", summary, genres);
                        row.add("summaryGenresVector", summaryGenresVector);
                    });

            TableInsertManyResult result = table.insertMany(rows);
            System.out.println("Inserted " + result.getInsertedIds().size() + " items.");
        }
    }

    public static void searchTable(Table<Row> table) {
        // Find rows that match a filter
        System.out.println("\nFinding books with rating greater than 4.7...");

        Filter filter = Filters.gt("rating", 4.7);

        TableFindOptions options = new TableFindOptions()
                .limit(10)
                .projection(include("title", "rating"));

        table
                .find(filter, options)
                .forEach(
                        row -> {
                            System.out.println(row.get("title") + " is rated " + row.get("rating"));
                        });

        // Perform a vector search to find the closest match to a search string
        System.out.println("\nUsing vector search to find a single scary novel...");

        TableFindOneOptions options2 =
                new TableFindOneOptions()
                        .sort(Sort.vectorize("summaryGenresVector", "A scary novel"))
                        .projection(include("title"));

        table
                .findOne(options2)
                .ifPresent(
                        row -> {
                            System.out.println(row.get("title") + " is a scary novel");
                        });

        // Combine a filter, vector search, and projection to find the 3 books with
        // more than 400 pages that are the closest matches to a search string
        System.out.println(
                "\nUsing filters and vector search to find 3 books with more than 400 pages that are set in the arctic, returning just the title and author...");

        Filter filter3 = Filters.gt("numberOfPages", 400);

        TableFindOptions options3 =
                new TableFindOptions()
                        .limit(3)
                        .sort(Sort.vectorize("summaryGenresVector", "A book set in the arctic"))
                        .projection(include("title", "author"));

        table
                .find(filter3, options3)
                .forEach(
                        row -> {
                            System.out.println(row);
                        });
    }

    public static void updateTable(Table<Row> table) {
        // Update a row
        Filter filter = new Filter(Map.of(
                "title", "Hidden Shadows of the Past",
                "author", "John Anthony"));
        TableUpdateOperation update = new TableUpdateOperation()
                .set("rating", 4.5)
                .set("genres", Arrays.asList("Fiction", "Drama"))
                .unset("borrower");
        table.updateOne(filter, update);
    }


    private static Date parseDate(String date) {
        if (date == null) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
