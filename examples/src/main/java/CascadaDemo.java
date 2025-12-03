import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.databases.Database;
import com.dtsx.astra.sdk.utils.JsonUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class CascadaDemo {

    public static void main(String[] args) {
        String astraToken       = "";
        String astraApiEndpoint = "";

        String cedrickToken = "";

        String demo_annotation_db = "";
        String db_aws_test = "";

        // Initialize the sourceClient.
        DataAPIClient sourceClient = new DataAPIClient(astraToken, new DataAPIClientOptions().logRequests());
        DataAPIClient destinationClient = new DataAPIClient(cedrickToken, new DataAPIClientOptions().logRequests());
        System.out.println("Connected to AstraDB");

        // Initialize the database.
        Database sourceDb = sourceClient.getDatabase(astraApiEndpoint);
        System.out.println(JsonUtils.marshall(sourceDb.getCollection("users").getDefinition()));
        System.out.println("Connected to source Database.");
        Database destinationDb1 = destinationClient.getDatabase(demo_annotation_db);
        Database destinationDb2 = destinationClient.getDatabase(db_aws_test);
        System.out.println("Connected to destination Database.");
        
        //System.out.println(sourceDb.getCollection("users").countDocuments(900));

        long top = System.currentTimeMillis();
        sourceDb.listCollectionNames().forEach(System.out::println);
        //destinationDb1.getCollection("users").findAll().stream().toList();
        //destinationDb2.getCollection("users").findAll().stream().toList();
        sourceDb.getCollection("speakers").findAll().stream().toList();
        System.out.println("Time to read all docs: " + (System.currentTimeMillis() - top) + " ms");

        AtomicInteger counter = new AtomicInteger(0);

        // LOAD DATA INTO COLLECTION
        //        sourceDb.getCollection("users")
        //           .findAll().stream()
        //           .forEach(doc -> {
        //               System.out.println(counter.getAndIncrement());
        //               destinationDb2.getCollection("users").insertOne(doc);
        //           });

    }
}
