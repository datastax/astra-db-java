package com.datastax.astra.sara;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.headers.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.headers.EmbeddingHeadersProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vector.VectorOptions;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.definition.keyspaces.KeyspaceDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.tools.javac.Main;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static com.datastax.astra.client.databases.commands.options.CreateKeyspaceOptions.IF_NOT_EXISTS;

public class QuickstartConnect {

    public static String endpoint  = "http://localhost:8181";
    public static String username  = "cassandra";
    public static String password  = "cassandra";

    public static void main(String[] args) throws Exception {
        // Client
        DataAPIClient client = DataAPIClients.clientHCD(username, password);

        // Getting DB
        Database database =  client.getDatabase(endpoint);
        System.out.println("Connected to database.");

        // Creating ks
        database.getDatabaseAdmin()
          .createKeyspace(new KeyspaceDefinition()
            .name("quickstart_keyspace")
            .simpleStrategy(1), IF_NOT_EXISTS);

        // Access Database on secondary keyspace
        // bug
        //database = database.useKeyspace("quickstart_keyspace");

        database = client.getDatabase(endpoint, "quickstart_keyspace");

        // Create Collection (if needed, vectorize with openAI)
        VectorOptions vectorOptions = new VectorOptions()
                       .dimension(1536)
                       .metric(SimilarityMetric.COSINE.getValue());
        Collection<Document> collection = database
          .createCollection("quickstart_collection2", new CollectionDefinition()
                  .vector(vectorOptions));

        // Load Json
        File jsonFile = new File(Main.class.getClassLoader()
          .getResource("quickstart_dataset.json")
          .toURI());

        List<Document> docs = new ObjectMapper()
           .readValue(new FileInputStream(jsonFile), new TypeReference<>() {});
        System.out.println("Loaded " + docs.size() + " documents from JSON file.");

        List<Document> documentsWithVectors = docs.stream()
          .peek(doc -> {
              if (doc.containsKey("summary_genre")) {
                  doc.remove("summary_genre");
                  doc.put("$vector", doc.get("summary"));
              }
          }).toList();

        // Vectorize with API KEY
        collection.insertMany(documentsWithVectors);
    }

    public static Database connectToDatabase() {
        DataAPIClientOptions clientOptions = new DataAPIClientOptions()
                .destination(DataAPIDestination.HCD);
        String token = new UsernamePasswordTokenProvider(username, password)
                .getTokenAsString();
        DataAPIClient client = new DataAPIClient(token, clientOptions);
        return client.getDatabase(endpoint);
    }


}