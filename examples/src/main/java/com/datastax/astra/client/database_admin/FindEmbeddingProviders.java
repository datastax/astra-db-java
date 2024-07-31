package com.datastax.astra.client.database_admin;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.EmbeddingProvider;
import com.datastax.astra.client.model.FindEmbeddingProvidersResult;

import java.util.Map;

public class FindEmbeddingProviders {
    public static void main(String[] args) {
        Database db = new Database(
                "https://8f915e5a-ea58-4576-bd00-9828984a1023-us-east1.apps.astra.datastax.com",
                "AstraCS:iLPiNPxSSIdefoRdkTWCfWXt:2b360d096e0e6cb732371925ffcc6485541ff78067759a2a1130390e231c2c7a");
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
