package com.datastax.astra.client.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.core.BulkWriteOptions;
import com.datastax.astra.client.core.BulkWriteResult;
import com.datastax.astra.client.core.Command;
import com.datastax.astra.client.core.Document;
import com.datastax.astra.internal.api.DataAPIResponse;

import java.util.List;

public class BulkWrite {
    public static void main(String[] args) {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Set a couple of Commands
        Command cmd1 = Command.create("insertOne").withDocument(new Document().id(1).append("name", "hello"));
        Command cmd2 = Command.create("insertOne").withDocument(new Document().id(2).append("name", "hello"));

        // Set the options for the bulk write
        BulkWriteOptions options1 = BulkWriteOptions.Builder.ordered(false).concurrency(1);

        // Execute the queries
        BulkWriteResult result = collection.bulkWrite(List.of(cmd1, cmd2), options1);

        // Retrieve the LIST of responses
        for(DataAPIResponse res : result.getResponses()) {
            System.out.println(res.getData());
        }
    }

}
