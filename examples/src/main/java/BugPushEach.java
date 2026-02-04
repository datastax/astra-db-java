import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.Update;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class BugPushEach {

    public static final String ASTRA_TOKEN    = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
    public static final String ASTRA_ENDPOINT = "https://844c28ee-7824-40a5-9b48-f4ca160c2b7b-us-east-2.apps.astra.datastax.com";
    public static final String KEYSPACE = "ks2";

    public static final String COL_BASIC     = "c_basic";

    public static void main(String[] args) throws Exception {

        // connect
        DataAPIClient client = new DataAPIClient(ASTRA_TOKEN, new DataAPIClientOptions());
        Database db = client.getDatabase(ASTRA_ENDPOINT, KEYSPACE);
        Collection<Document> collection = db.createCollection(COL_BASIC);

        Document document = new Document()
                .id("d1")
                .append("title", "hello world")
                .append("genres", Arrays.asList("Comedy", "Science"));
        collection.insertOne(document);

        Filter docD1 = Filters.id("d1");

        System.out.println("Before");
        collection.findOne(docD1).ifPresent(doc ->
          System.out.println(doc.getList("genres", String.class))
        );

        collection.updateOne(docD1, Update
                .create()
                .pushEach("genres", Arrays.asList("Mystery", "Fiction"), null));

        System.out.println("After");
        collection.findOne(docD1).ifPresent(doc ->
            System.out.println(doc.getList("genres", String.class))
        );

    }
}
