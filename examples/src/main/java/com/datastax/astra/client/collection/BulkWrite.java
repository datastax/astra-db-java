package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.BulkWriteOptions;
import com.datastax.astra.client.model.BulkWriteResult;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.internal.api.ApiResponse;

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
        for(ApiResponse res : result.getResponses()) {
            System.out.println(res.getData());
        }
    }

}
