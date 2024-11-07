package com.datastax.astra.client.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.commands.FindOneAndReplaceOptions;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Projections;
import com.datastax.astra.client.core.query.Sorts;

import java.util.Optional;

import static com.datastax.astra.client.core.query.Filters.lt;

public class FindOneAndReplace {
    public static void main(String[] args) {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Building a filter
        Filter filter = Filters.and(
                Filters.gt("field2", 10),
                lt("field3", 20),
                Filters.eq("field4", "value"));

        FindOneAndReplaceOptions options = new FindOneAndReplaceOptions()
                .projection(Projections.include("field1"))
                .sort(Sorts.ascending("field1"))
                .upsert(true)
                .returnDocumentAfter();

        Document docForReplacement = new Document()
                .append("field1", "value1")
                .append("field2", 20)
                .append("field3", 30)
                .append("field4", "value4");

        // It will return the document before deleting it
        Optional<Document> docBeforeReplace = collection
                .findOneAndReplace(filter, docForReplacement, options);
    }
}
