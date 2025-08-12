package com.datastax.astra.client.collections.bm25;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.cursor.CollectionFindCursor;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.commands.options.CreateCollectionOptions;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.hybrid.Hybrid;
import com.datastax.astra.client.core.lexical.Analyzer;
import com.datastax.astra.client.core.lexical.LexicalOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.databases.Database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    public static Collection<Document> createLexicalCollection(Database db) {
        return db.createCollection("c_lexical_demo",
                new CollectionDefinition().lexical(new Analyzer(STANDARD)));
    }

    public static void populateLexicalCollection(Collection<Document> col) throws IOException {
        InputStream in = FindOneLexical.class.getResourceAsStream("/philosopher-quotes.csv");
        if (in == null) {
            throw new IllegalStateException("File not found in resources");
        }
        ;
        col.insertMany(new String(in.readAllBytes()).lines().map(line -> {
             String[] chunks = line.split(",");
             String quote = chunks[1].replace("\"", "");
             return new Document().append("author", chunks[0]).append("quote", quote).lexical(quote);
         }).toList(),
         new CollectionInsertManyOptions().concurrency(3).chunkSize(10)
        );
    }


    public static void main(String[] args) throws IOException {

        Database database = getHCDDatabase(
                "http://localhost:8181",
                "cassandra",
                "cassandra",
                "quickstart_keyspace");

        // Create a collection with Lexical
        Collection<Document> cLexical = createLexicalCollection(database);
        populateLexicalCollection(cLexical);

        // FindOne Match
        cLexical.findOne(Filters
          .match("Fortune favours the bold"))
          .ifPresent(System.out::println);
    }
}