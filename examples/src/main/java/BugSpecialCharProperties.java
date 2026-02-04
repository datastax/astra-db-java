import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.ReturnDocument;
import com.datastax.astra.client.collections.commands.cursor.CollectionFindCursor;
import com.datastax.astra.client.collections.commands.options.CollectionReplaceOneOptions;
import com.datastax.astra.client.collections.commands.results.CollectionUpdateResult;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.internal.utils.EscapeUtils;

import java.util.UUID;

public class BugSpecialCharProperties {

    public static final String ASTRA_TOKEN    = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
    public static final String ASTRA_ENDPOINT = "https://844c28ee-7824-40a5-9b48-f4ca160c2b7b-us-east-2.apps.astra.datastax.com";
    public static final String COL_REPLACE   = "c_replace";

    public static void main(String[] args) throws Exception {

        // connect
        DataAPIClient client = new DataAPIClient(ASTRA_TOKEN, new DataAPIClientOptions().logRequests());
        Database db = client.getDatabase(ASTRA_ENDPOINT);
        System.out.println("Connected to Astra");
        Collection<Document> collection = db.getCollection(COL_REPLACE);
        collection.deleteAll();

        // Client would never do substitution
        collection.insertOne(new Document()
                .append("_id", "doca")
                .append("a.a", "a.a")
                .append("a&a", "a&a")
                .append("a..a", "a..a")
                .append(new String[] {"top", "lvl1", "lvl11"}, "11")
                .append(new String[] {"top", "lvl1", "lvl12"}, "12"));
        collection.insertOne(new Document()
                .append("_id", "docb")
                .append("a.a", "b.b")
                .append("a&a", "b&b")
                .append("a..a", "b..b")
                .append(new String[] {"top", "lvl1", "lvl11"}, "11")
                .append(new String[] {"top", "lvl1", "lvl12"}, "12"));

        // When Using find, no substitution
        /* Find documents */
       collection
          .findAll()
          .forEach(d -> {
            System.out.println(d.getString("a&a"));
        });
        Thread.sleep(1000);

        /*
        Filter filter = Filters.eq("_id", "doc1");
        Document newDocument = new Document()
                .append("_id", UUID.randomUUID().toString())
                .append("isCheckedOut", false)
                .append("borrower", "Brook Reed3");
        CollectionReplaceOneOptions options = new CollectionReplaceOneOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER);
        CollectionUpdateResult result = collection.replaceOne(filter, newDocument, options);
        System.out.println(result.getMatchedCount());
        System.out.println(result.getModifiedCount());
        System.out.println(result.getUpsertedId());*/
    }
}
