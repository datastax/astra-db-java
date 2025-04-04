package com.datastax.astra.client;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.options.TimeoutOptions;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.tables.commands.options.TableFindOneOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;

import java.time.Duration;

public class DataAPIVectorDemo {
    public static void main(String[] args) {

        // Disable binary encoding
        DataAPIClientOptions.getSerdesOptions().disableEncodeDataApiVectorsAsBase64();

        // Enable binary encoding
        DataAPIClientOptions.getSerdesOptions().encodeDataApiVectorsAsBase64(true);

        float[] embeddings = new float[] {.1f, .2f};
        DataAPIVector vector = new DataAPIVector(embeddings);
        Collection<Document> collec = new Collection<>(null, null,null,null);

        // preferred way
        collec.insertOne(new Document().vector(vector));

        // Still possible
        collec.insertOne(new Document().vector(embeddings));


        collec.findOne(Filters.eq("_id" ,"1"));

        // Using longs to initialize the values
        TimeoutOptions timeoutOptions = new TimeoutOptions()
                .generalMethodTimeoutMillis(1000L)
                .requestTimeoutMillis(1000L)
                .connectTimeoutMillis(1000L)
                .databaseAdminTimeoutMillis(1000L)
                .collectionAdminTimeoutMillis(1000L)
                .tableAdminTimeoutMillis(1000L);

        // Using durations to initialize the values
        TimeoutOptions timeoutOptions2 = new TimeoutOptions()
                .generalMethodTimeout(Duration.ofSeconds(1))
                .requestTimeoutMillis(1000L)
                .connectTimeout(Duration.ofSeconds(1))
                .databaseAdminTimeout(Duration.ofSeconds(1))
                .collectionAdminTimeout(Duration.ofSeconds(1))
                .tableAdminTimeout(Duration.ofSeconds(1));

        new DataAPIClientOptions().timeoutOptions(timeoutOptions);

        new TableFindOneOptions().timeoutOptions(timeoutOptions);
        new TableFindOptions().timeout(Duration.ofSeconds(1));

    }
}
