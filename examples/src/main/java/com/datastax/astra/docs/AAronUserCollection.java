package com.datastax.astra.docs;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.databases.Database;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AAronUserCollection {

    @Data
    @NoArgsConstructor
    public static class AaronUserPojo {
        private String userId;
        private String lastname;
        private String firstname;
        private String hashedPassword;
        private List<String> roles = new ArrayList<>();
        private String email;
    }

    public static void main(String[] args) {
        // Connection
        String astraToken = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
        //String astraApiEndpoint = System.getenv("ASTRA_DB_API_ENDPOINT");
        String astraApiEndpoint = "https://4d3d14ac-5e95-4121-a38f-de3f1491f1ea-us-east-2.apps.astra.datastax.com";

        // Initialize the client.
        DataAPIClient client = new DataAPIClient(astraToken);
        System.out.println("Connected to AstraDB");
        Database db = client.getDatabase(astraApiEndpoint);
        System.out.println("Connected to Database.");

        Collection<AaronUserPojo> userCollection = db
                .getCollection("aaron_users", AaronUserPojo.class);
        userCollection.deleteAll();

        AaronUserPojo user  = new AaronUserPojo();
        user.setUserId( "250575bb-3ff6-4c58-886c-04e4d29c1fae");
        user.setLastname("Ploetz");
        user.setFirstname("Aaron");
        user.setHashedPassword("$2a$10$IKE8Ph5TephKrVQHrUdwGe7s/5HF4.i.EleVjJpGcBklCZoLHoQCa");
        user.setRoles(Collections.singletonList("USER"));
        user.setEmail("aaronploetz3@gmail.com");
        userCollection.insertOne(user);

        userCollection
                .findOne(Filters.eq("email", "aaronploetz3@gmail.com"))
                .map(AaronUserPojo::getRoles)
                .ifPresent(System.out::println);


//        // Create a collection. The default similarity metric is cosine.
//        Collection<Document> userCollection = db.createCollection("aaron_users");
//        System.out.println("Created a collection");
//
//        userCollection.insertOne(new Document()
//                .append("user_id", "250575bb-3ff6-4c58-886c-04e4d29c1fae")
//                .append("lastname", "Ploetz")
//                .append("firstname", "Aaron")
//                .append("hashed_password", "$2a$10$IKE8Ph5TephKrVQHrUdwGe7s/5HF4.i.EleVjJpGcBklCZoLHoQCa")
//                .append("roles", Collections.singletonList("USER"))
//                .append("email", "aaronploetz2@gmail.com"));
//
//
//
//        Optional<Document> doc = userCollection
//                .findOne(Filters.eq("email", "aaronploetz2@gmail.com"));
//        List<String> roles = doc.orElse(null).getList("roles", String.class);
//        System.out.println("User roles: " + roles);

    }
}
