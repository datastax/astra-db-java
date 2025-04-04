package com.datastax.astra.client.database_admin;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.datastax.astra.client.core.vectorize.EmbeddingProvider;
import com.datastax.astra.client.databases.Database;

import java.util.Map;

public class FindEmbeddingProviders {
    public static void main(String[] args) {
        Database db = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT");
        DatabaseAdmin dbAdmin = db.getDatabaseAdmin();

        // was actually a new object not the initial
        // RATIONAL: as you can come from AstraDBAdmin potentially you not always have a database object created
        Database db1 = dbAdmin.getDatabase();
        Database db2 = dbAdmin.getDatabase("keyspace2");


        FindEmbeddingProvidersResult fepr = db.getDatabaseAdmin().findEmbeddingProviders();

        Map<String, EmbeddingProvider> providers = fepr.getEmbeddingProviders();
        for (Map.Entry<String, EmbeddingProvider> entry : providers.entrySet()) {
            System.out.println("\nProvider: " + entry.getKey());
            EmbeddingProvider provider = entry.getValue();
            provider.getHeaderAuthentication().ifPresent(headerAuthentication -> {
                System.out.println("+ Authentication Header");
                headerAuthentication.getTokens().forEach(token -> {
                    System.out.println("  +"
                            + " Token accepted=" + token.getAccepted()
                            + ", forwarded=" + token.getForwarded());
                });
            });
            System.out.println("+ Models:");
            for(EmbeddingProvider.Model model : provider.getModels()) {
                System.out.println("  + Name=" +
                        model.getName() + ((model.getVectorDimension() != null) ? ", dimension=" + model.getVectorDimension() : ""));
            }
        }
    }
}
